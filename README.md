# Privactivity Activity Store

A **multi-module Spring Boot application** for storing and analyzing fitness activities from Garmin FIT files, GPX
files, and TCX files. The application has a **domain-driven modular architecture** with separate storage engines and a
unified Angular frontend.

## Features

- Import and store activities from **Garmin FIT**, **GPX**, and **TCX** file formats
- Query and filter activities by distance, ascent, location, and more
- Export activities in multiple formats (GPX, CSV)
- Analyze metrics: power, heart rate, cadence, elevation
- Interactive map visualization with Leaflet
- GeoJSON support for advanced geospatial queries
- JWT-based authentication with role-based access control
- EclipseStore for high-performance object persistence
- Full REST API for programmatic access

## Quick Start

### Prerequisites

- **JDK 25+**
- **Gradle 9.6+** (included via `gradlew`)
- **Node.js 24+** (Gradle downloads and manages it)
- **Optional**: Docker for container deployment

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-org/privactivity-activity-store.git
   cd privactivity-activity-store
   ```

2. **Build the application:**
   ```bash
   ./gradlew build
   ```

3. **Configure storage:**
   The default directory for temporary application data is `${HOME}/tmp/privactivity`.
   Either create that directory or change it in `privactivity-app/src/main/resources/application-dev.properties`.

   The application reads/writes long-term data and imported data by default in `${PRIVATE_DATA_DIR}`.
   Set this variable or change it in the file above.

   On first startup, the application reads the initial admin password `${HOME}/tmp/privactivity/admin-password.txt` and
   uses it to create the
   default `admin` account in `users.json` if that file does not already exist.

### Download Shapefiles

Optionally download Shapefiles for the countries of your activities.

* For Switzerland go to https://portal.topax.ch/wizard/claim/file (for example
  https://data.geo.admin.ch/ch.swisstopo.swissboundaries3d/swissboundaries3d_2026-01/swissboundaries3d_2026-01_2056_5728.shp.zip)
  and download the latest ".shp.zip" file. Unzip it and copy all
  "HOHEITSGEBIET" files to "privactivty-app/data/shapefiles/switzerland".
* For Germany go to https://gdz.bkg.bund.de and get the latest "Verwaltungsgebiete 1:250 000" (VG250) in UTM32s shape
  format. E.g. from
  e.g. https://gdz.bkg.bund.de/index.php/default/verwaltungsgebiete-1-250-000-stand-01-01-vg250-01-01.html download
  and https://daten.gdz.bkg.bund.de/produkte/vg/vg250_ebenen_0101/aktuell/vg250_01-01.utm32s.shape.ebenen.zip. Unzip it
  and copy the vg250_ebenen_0101 folder to "privactivty-app/data/shapefiles/germany/„

If the shapefiles are not present, there will be a warning and a stacktrace in the startup log but
most of the features will still work.

4. **Run locally:**
   ```bash
   ./bootRun.sh
   ```
   This starts the application in development mode at `http://localhost:8090` with hot-reload enabled. To use hot-reload
   web-frontend:
   ```bash
   ./gradlew ngServe
   ```

### Installation using the Docker image

1. **Build the image:**
   ```bash
   ./build-docker-image.sh
   ```
   This creates the `org.privactivity/activity-store:latest` image in the registry configured by `DOCKER_REGISTRY`
   (`localhost` by default).

2. **Prepare persistent files and directories:**
   The Docker profile expectes application data under `/privactivity`. Create a local directory `/path/to/privactivity`
   for it.

3. **Initial admin password:**
   Create a file `/path/to/privactivity/admin-password.txt` and add a line with the initial password for the admin user.

4. **Generate the JWT key pair:**
   The Docker profile expects PEM-encoded RSA keys at `/privactivity/jwt/app.key` and `/privactivity/jwt/app.pub`.
   Generate them once:
   ```bash
   mkdir -p /path/to/privactivity/jwt
   openssl genpkey -algorithm RSA -out /path/to/privactivity/jwt/app.key -pkeyopt rsa_keygen_bits:2048
   openssl rsa -pubout -in /path/to/privactivity/jwt/app.key -out /path/to/privactivity/jwt/app.pub
   ```

5. **Run the container:**
   ```bash
   docker run --rm \
     -p 8090:8090 \
     -e SPRING_PROFILES_ACTIVE=docker \
     -v /path/to/privactivity:/privactivity \
     localhost/org.privactivity/activity-store:latest
   ```
   The application is then available at `http://localhost:8090`.

## Documentation

- **[DEVELOPMENT.md](./DEVELOPMENT.md)** — Architecture, layering, workflow, and testing conventions
- **[.github/copilot-instructions.md](./.github/copilot-instructions.md)** — AI coding agent guidelines

## Development

### Build & Run

```bash
./bootRun.sh                    # Development server with hot-reload
./gradlew build                 # Full build (all modules + Angular frontend)
./gradlew ngServe               # Start Angular dev server only
```

### Technology Stack

- **Backend**: Spring Boot 4, EclipseStore, JUnit 5
- **Frontend**: Angular 21, Material UI, Leaflet
- **Build**: Gradle multi-project, Node.js (managed by Gradle)
- **Storage**: EclipseStore (object database, not SQL)

### Contributing

Please refer to [DEVELOPMENT.md](./DEVELOPMENT.md) for detailed architecture, layering principles, and coding
conventions.

## License

See LICENSE file for details.