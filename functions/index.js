const functions = require("firebase-functions");
const admin = require("firebase-admin");
const axios = require("axios");

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
