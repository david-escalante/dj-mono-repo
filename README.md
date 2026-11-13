# Beat-Sync Playlist Manager

Local DJ-style playlist manager: browse a seeded catalog, build playlists, and get **beat-sync** suggestions based on BPM (derived from each CSV row so mixes stay stable).

Stack: **Java 25**, **Spring Boot 4.1.2**, **Spring Data MongoDB**, **React + Material UI**, **Docker Compose**. Target repository: [david-escalante/dj-mono-repo](https://github.com/david-escalante/dj-mono-repo).

## Run locally

You only need Docker. Compose creates MongoDB, the API, and the web UI.

```bash
docker compose up --build
```

- Web: http://localhost:3847
- API: http://localhost:8091
- Health: http://localhost:8091/actuator/health
- MongoDB: `mongodb://localhost:27017/beatsync`

The first API start loads `backend/src/main/resources/data/songs.csv` (~10,000 tracks) into Mongo when the `tracks` collection is empty. Delete the volume to re-seed:

```bash
docker compose down -v
```

## Catalog CSV

Header: `Title,Artist,Album,Genre,Release year,Run time`

Run time is `m:ss`. BPM and energy are **derived** (genre range + a stable hash of title and artist) because the file has no tempo column.

## API

| Method | Path | Use |
| --- | --- | --- |
| GET | `/api/tracks?q=&genre=&page=0&size=25` | Paginated catalog |
| GET | `/api/genres` | Distinct genres |
| GET/POST | `/api/playlists` | List / create |
| GET/PATCH/DELETE | `/api/playlists/{id}` | Playlist |
| POST | `/api/playlists/{id}/items` | `{ "trackId": "..." }` |
| POST | `/api/playlists/{id}/items/{itemId}/move` | `{ "position": 0 }` |
| GET | `/api/playlists/{id}/suggestions` | BPM-ranked next tracks |
| POST | `/api/playlists/{id}/smooth-order` | Reorder for smaller BPM jumps |

## Develop without rebuilding images

Start only Mongo, then run the API and Vite app on the host (JDK 25 + Maven, Node 22):

```bash
docker compose up mongo
cd backend && SPRING_MONGODB_URI=mongodb://localhost:27017/beatsync mvn spring-boot:run
cd frontend && npm install && npm run dev
```

Unit tests (CSV parser, BPM derivation, ranking). Repository tests use Testcontainers `mongo:7` when Docker is available:

```bash
cd backend && mvn -q test
```

Compose file lives at the repo root (`docker compose up --build`). Mongo data is stored in the `beatsync_mongo_data` volume.

The API and nginx proxy connect to Mongo/API through `host.docker.internal` (Compose `host-gateway`) so the stack still works when the Docker bridge cannot route container-to-container traffic. Mongo (`27017`) and the API (`8091`) are published on the host.
