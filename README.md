# WebSocket API
ViPLab WebSocket API.
This is used in the ViPLab frontend to get an interactive session with the ViPLab service.

## Documentation
Documentation is in the [docs](docs) directory.
Architectural Decision are documented as Markdown Architectural Decision Records in [docs/adr](docs/adr).

The schemata of the messages are defined in [schema](schema).

## How to run
This project is build with maven and uses Open Liberty as Java server.

`mvn liberty:start`

## Development
Start a development server with live reload and on demand test execution:
`mvn liberty:dev`

For more information about the development server see [Liberty dev mode](https://github.com/OpenLiberty/ci.maven/blob/master/docs/dev.md).