# API

## Definitions

- `API_URL` ::= `host:port`
- `servicePath` ::= `/{service}/{serviceVersion}`
- `serviceUrl` ::= `{API_URL}{servicePath}`
- `endpointPath` ::= `/{endpoint}`
- `endpointUrl` ::= `{serviceUrl}{endpointPath}`

### Example

- `API_URL` = `http://localhost:33321`
- `servicePath` = `/schedulingmanager/v1`
- `serviceUrl` = `http://localhost:33321/schedulingmanager/v1`
- `endpointPath` = `/workflow-schedules`
- `endpointUrl` = `http://localhost:33321/schedulingmanager/v1/workflow-schedules`


## Swagger

- Swagger UI for Service API ::= `{serviceUrl}/swagger-ui`
- Service API specification ::= `{serviceUrl}/swagger.json`

### Example

- Swagger UI for Service API ::= `http://localhost:33321/schedulingmanager/v1/swagger-ui`
- Service API specification ::= `http://localhost:33321/schedulingmanager/v1/swagger.json`
