const functions = require("firebase-functions");
const admin = require("firebase-admin");
const axios = require("axios");
const pdf = require("pdf-parse");
const https = require("https");
const cheerio = require("cheerio");

admin.initializeApp();

exports.sendNotificationOnAnuncioUpdate = functions.firestore.onDocumentUpdated(
    "anuncios/anuncio_principal",
    async (event) => {
        const newData = event.data.after.data();
        const oldData = event.data.before.data();

        if (newData.titulo !== oldData.titulo || newData.contenido !== oldData.contenido) {
            const payload = {
                notification: {
                    title: newData.titulo,
                    body: newData.contenido,
                    click_action: "FLUTTER_NOTIFICATION_CLICK"
                },
                data: {
                    tipo: "anuncios",
                    id: "anuncio_principal",
                    sonido: "whistle_sound"
                }
            };

            const tokensSnapshot = await admin.firestore().collection("tokens").get();
            const tokens = tokensSnapshot.docs.map(doc => doc.data().token);

            if (tokens.length > 0) {
                const messaging = admin.messaging();
                await messaging.sendEachForMulticast({
                    tokens: tokens,
                    notification: {
                        title: newData.titulo,
                        body: newData.contenido,
                    }
                });
                console.log("📢 Notificación enviada a dispositivos:", tokens);
            }
        }

        return null;
    }
);

exports.getClasificacion = functions.https.onRequest(async (req, res) => {
    const grupoId = req.query.grupoId;  // Obtener grupoId desde la URL

    if (!grupoId) {
        return res.status(400).json({ error: "Falta el parámetro 'grupoId'" });
    }

    try {
        // Construir la URL con el grupoId dinámico
        const apiUrl = `https://fmvoley.com/api/competiciones/getClasificacionesFederapp?divisionId=-1&circuitoId=-1&faseId=-1&grupoId=${grupoId}`;

        const response = await axios.get(apiUrl, {
            headers: {
                "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
            }
        });

        const ligaNombre = response.data.competicion + " (" + response.data.division + ")"

        const clasificacion = response.data.clasificacion.map(equipo => ({
                    id: equipo.id,
                    equipo: equipo.nombre.trim(),
                    puntos: parseInt(equipo.puntos),
                    jugados: parseInt(equipo.jugados),
                    ganados: parseInt(equipo.ganados),
                    perdidos: parseInt(equipo.perdidos),
                    sets_a_favor: parseInt(equipo.sets_a_favor),
                    sets_en_contra: parseInt(equipo.sets_en_contra),
                    puntos_a_favor: parseInt(equipo.puntos_a_favor),
                    puntos_en_contra: parseInt(equipo.puntos_en_contra),
                    ligaNombre: ligaNombre
                }));

        res.json(clasificacion);

    } catch (error) {
        console.error("❌ Error al obtener la clasificación:", error);
        res.status(500).json({ error: "Error al obtener los datos de la API" });
    }
});

const db = admin.firestore();

exports.getLigas = functions.https.onRequest(async (req, res) => {
    try {
        const doc = await db.collection("anuncios").doc("categorias").get();

        if (!doc.exists) {
            return res.status(404).json({ error: "No hay datos de ligas en Firestore" });
        }

        const data = doc.data();

        if (!data.categoria || !data.ids || data.categoria.length !== data.ids.length) {
            return res.status(400).json({ error: "Estructura incorrecta en Firestore" });
        }

        // Unir los arrays en una sola lista de objetos
        const ligas = data.categoria.map((nombre, index) => ({
            nombre: nombre,
            grupoId: data.ids[index]
        }));

        res.json(ligas);
    } catch (error) {
        console.error("❌ Error al obtener las ligas:", error);
        res.status(500).json({ error: "Error al obtener las ligas desde Firestore" });
    }
});

exports.getClasificacionMostoles = functions.https.onRequest(async (req, res) => {
    const grupoId = req.query.grupoId;

    if (grupoId !== "mostoles_senior1" && grupoId !== "mostoles_senior2") {
        return res.status(400).json({ error: "grupoId no válido para esta función" });
    }

    try {
        // 1. Cargar la página HTML
        const pageUrl = "https://www.mostoles.es/MostolesDeporte/es/campeonatos-municipales-deporte-infantil/voleibol/clasificaciones-resultados/clasificaciones-resultados-campeonatos-municipales";
        const html = await axios.get(pageUrl);
        const $ = cheerio.load(html.data);

        const pdfLinks = [];
        $("a").each((_, el) => {
            const text = $(el).text().toLowerCase();
            const href = $(el).attr("href");
            if (href && href.endsWith(".pdf") && text.includes("voleibol senior")) {
                pdfLinks.push({
                    url: "https://www.mostoles.es" + href,
                    texto: text
                });
            }
        });

        if (pdfLinks.length === 0) {
            return res.status(404).json({ error: "No se encontraron PDFs en la web de Móstoles" });
        }

        const pdfMatch = pdfLinks.find(link =>
            grupoId === "mostoles_senior1"
                ? link.texto.includes("1ª div")
                : link.texto.includes("2ª div")
        );

        if (!pdfMatch) {
            return res.status(404).json({ error: `No se encontró PDF para el grupo ${grupoId}` });
        }

        const pdfUrl = pdfMatch.url;

        const pdfBuffer = await new Promise((resolve, reject) => {
            https.get(pdfUrl, (response) => {
                const data = [];
                response.on("data", (chunk) => data.push(chunk));
                response.on("end", () => resolve(Buffer.concat(data)));
                response.on("error", reject);
            });
        });

        console.log(`📄 PDF descargado (${grupoId}):`, pdfUrl);

        const data = await pdf(pdfBuffer);
        const texto = data.text;

        // 2. Extraer la tabla completa de clasificación
        const clasificacionRegex = /Pos\s+Equipo[\s\S]+?(?=JORNADA\s+\d|PISCINA|Próx Jornada|$)/i;
        const match = texto.match(clasificacionRegex);
        const clasificacionRaw = match ? match[0].trim() : null;

        if (!clasificacionRaw) {
            return res.status(500).json({ error: "No se pudo extraer la clasificación del PDF" });
        }

        console.log("📋 Texto tabla clasificación cruda:");
        console.log(clasificacionRaw);

        const lineas = clasificacionRaw.split("\n").filter(linea => /^\d+º?\s/.test(linea));

        const ligaNombre =
            grupoId === "mostoles_senior1"
                ? "Liga Municipal de Móstoles (Senior 1ª División)"
                : "Liga Municipal de Móstoles (Senior 2ª División)";

        const clasificacion = lineas.map(linea => {
            const regex = /^(\d+)º?\s+(.+?)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+([-\d]+)\s+([\d,]+)\s+(\d+)\s+(\d+)\s+([-\d]+)\s+([\d,]+)\s+(\d+)\s+([=+\-0-9]+)/;
            const match = linea.match(regex);

            if (!match) return null;

            return {
                id: match[1],
                equipo: match[2],
                puntos: parseInt(match[3]),
                jugados: parseInt(match[4]),
                ganados: parseInt(match[5]) + parseInt(match[6]),
                perdidos: parseInt(match[7]) + parseInt(match[8]),
                sets_a_favor: parseInt(match[10]),
                sets_en_contra: parseInt(match[11]),
                puntos_a_favor: parseInt(match[14]),
                puntos_en_contra: parseInt(match[15]),
                ligaNombre: ligaNombre
            };
        }).filter(e => e !== null);


        res.json(clasificacion);

    } catch (err) {
        console.error("❌ Error al procesar PDF de Móstoles:", err);
        res.status(500).json({ error: "Error al procesar PDF de Móstoles" });
    }
});
