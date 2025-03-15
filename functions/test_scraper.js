const axios = require("axios");
const cheerio = require("cheerio");

(async () => {
    try {
        const url = "https://fmvoley.com/clasificaciones-y-resultados/municipales_lm_alcorcon_juvenil_1%C2%AA_division_1%C2%AA_fase_unica";
        
        const response = await axios.get(url, {
            headers: {
                "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
            }
        });

        const html = response.data;
        const $ = cheerio.load(html);

        let clasificacion = [];

        $("table tr").each((index, element) => {
            if (index === 0) return; // Saltar la cabecera

            const cols = $(element).find("td").map((i, el) => $(el).text().trim()).get();
            if (cols.length > 0) {
                clasificacion.push({
                    Pos: parseInt(cols[0]),
                    Equipo: cols[1],
                    Pts: parseInt(cols[2]),
                    Jug: parseInt(cols[3]),
                    Gan: parseInt(cols[4]),
                    Ga3: parseInt(cols[5]),
                    Ga2: parseInt(cols[6]),
                    Per: parseInt(cols[7]),
                    Per1: parseInt(cols[8]),
                    Per0: parseInt(cols[9]),
                    Emp: parseInt(cols[10]),
                    NP: parseInt(cols[11]),
                    JFav: parseInt(cols[12]),
                    JCon: parseInt(cols[13]),
                    PFav: parseInt(cols[14]),
                    PCont: parseInt(cols[15]),
                    Sanc: parseInt(cols[16])
                });
            }
        });

        console.log(JSON.stringify(clasificacion, null, 4));

    } catch (error) {
        console.error("❌ Error al obtener la clasificación:", error);
    }
})();
