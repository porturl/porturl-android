# porturl Android App

## Monitoring & Observability (Grafana Cloud)

This application uses the **OpenTelemetry Android Agent** for Real User Monitoring (RUM). It automatically captures crashes, ANRs, screen transitions, and network traces.

### Configuration

Monitoring is disabled by default if the authentication token is missing. To enable it, you must provide your Grafana Cloud OTLP credentials.

#### Using `local.properties` (Recommended for Local Dev)
Add the following to your `local.properties` file:
```properties
OTLP_ENDPOINT=https://otlp-gateway-prod-us-east-0.grafana.net/otlp
OTLP_AUTH=Basic <your_base64_auth_token>
```

#### Using Environment Variables (Recommended for CI/CD)
The build script also checks for these project properties:
- `OTLP_ENDPOINT`
- `OTLP_AUTH`

Example build command:
```bash
./gradlew assembleRelease -POTLP_AUTH="Basic <your_token>"
```

### Features
- **Privacy First:** Users can opt out of telemetry entirely via the app settings.
- **Distributed Tracing:** Traces are automatically propagated to the backend using W3C Trace Context.
- **Crash Reporting:** Uncaught exceptions and ANRs are sent to Grafana Cloud.
- **Auto-Instrumentation:** Screen changes and OkHttp network calls are automatically instrumented.
