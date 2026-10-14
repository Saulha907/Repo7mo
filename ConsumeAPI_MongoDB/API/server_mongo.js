
require('dotenv').config({
  path: require('path').join(__dirname, '.env'),
});


const dns = require('node:dns');
dns.setServers(['8.8.8.8', '1.1.1.1']);

const express = require('express');
const { MongoClient } = require('mongodb');
const cors = require('cors');

const app = express();

app.use(cors());

const uri = process.env.MONGODB_URI;

if (!uri) {
  console.error('Falta configurar MONGODB_URI');
  process.exit(1);
}

const client = new MongoClient(uri);

async function main() {
  await client.connect();

  console.log('Conexión a MongoDB establecida');

  const db = client.db('sample_mflix');

  const movies = db.collection('movies');

  app.get('/movies', async (req, res) => {
    try {
      const data = await movies
        .find(
          {},
          {
            projection: {
              poster: 1,
              title: 1,
              fullplot: 1,
            },
          }
        )
        .limit(60)
        .toArray();

      res.json(data);
    } catch (error) {
      console.error('Error al consultar películas:', error);

      res.status(500).json({
        error: 'No se pudieron obtener las películas',
      });
    }
  });

  app.listen(4000, '0.0.0.0', () => {
    console.log('Servidor iniciado en http://localhost:4000');
  });
}

main().catch((error) => {
  console.error('Error al iniciar el servidor:', error.message);
  process.exit(1);
});