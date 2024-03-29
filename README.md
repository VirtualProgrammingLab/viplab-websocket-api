# WebSocket API [![Java Tests](https://github.com/VirtualProgrammingLab/viplab-websocket-api/workflows/Java%20Tests/badge.svg)](https://github.com/VirtualProgrammingLab/viplab-websocket-api/actions?query=workflow%3A%22Java+Tests%22)
ViPLab WebSocket API.
This is used in the ViPLab frontend to get an interactive session with the ViPLab service.

## Documentation
Documentation is in the [docs](docs) directory.
Architectural Decision are documented as Markdown Architectural Decision Records in [docs/adr](docs/adr).

The schemata of the messages are defined in [schema](schema).

## How to run
This project is build with maven and uses Quarkus.io as runtime server.
Build the docker image.
```
mvn clean test package
```
To configure the image-name or version tag, or if it should be build can be set using the following command-line.
```
mvn clean test package -Dquarkus.container-image.build=true -Dquarkus.container-image.group=viplab -Dquarkus.container-image.name=websocket-api -Dquarkus.container-image.tag=latest
```

> The native build currently does not work, because we use reflection in our code.

To run this application, the `jwks.json` is required with all public keys.
Mount or copy this file into the container and use the `JWKS_FILE` environment variable to specifiy the location.

```
docker run --rm -it -p 8080:8080 -e JWKS_FILE=/config/jwks.json -v "${PWD}/jwks.json:/config/jwks.json" websocket-api
```

### Generate Json Web Keys
Generate keys with [json-web-key-generator](https://github.com/Legion2/json-web-key-generator).
It's available as [docker image](https://github.com/users/Legion2/packages/container/package/json-web-key-generator) in GitHub Container Registry.
New keys can be generated with the command:
```
docker run --rm ghcr.io/legion2/json-web-key-generator jwk-generator -t RSA -s 2048 -S -p -i mykeyid
```
The private key should be stored in a file named `jwks.private.json` and the public key in a file named `jwks.json`.

### Configuration
The configuration can be done using environment variables or a configuration file.
The configuration file must be located at `$PWD/config/application.properties`.
Environment variables names are following the conversion rules of [Eclipse MicroProfile](https://github.com/eclipse/microprofile-config/blob/master/spec/src/main/asciidoc/configsources.asciidoc#default-configsources).
Configuration properties:
| Name                                           | Type      | description                                                                                                                                                              |
|------------------------------------------------|-----------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `viplab.validation.configuration.mustValidate` | `boolean` | If true all Computation configurations must be validated, else configurations without a validator are not validated                                                      |
| `viplab.jwt.jwks.file`                         | `Path`    | Path to the `jwks.json` with all public keys.                                                                                                                            |
| `mp.messaging.outgoing.computations.address`   | `String`  | Address of the computation exchange on the AMQP Broker                                                                                                                   |
| `amqp-*`                                       |           | Configuration of the AMQP Broker information, see [SmallRye Reactive Messaging AMQP connector](https://smallrye.io/smallrye-reactive-messaging/#_interacting_using_amqp) |

## Development
You can generate test data by running:
```
mvn -pl websocket-api-impl -Dtest=de.uni_stuttgart.tik.viplab.websocket_api.GenerateJWTTest test
```
This will print the `authenticate` and `create-computation` json messages to the console.
These messages can send to the websocket.

The generated messages are signed with the internal test Json Web Key from `websocket-api-impl/src/test/resources/test-jwks.private.json`.

### Add new environments/languages

To add support for a new environment add the required classes into the [extensions](extensions/) project.
The classes must be annotated with `@Environment("<your environment here>")` and be registered as SPI implementation.
To do this use the `@AutoService(<interface class here>)` annotation.

The following types can be implemented:
* [ConfigurationValidator](internal-extension-api/src/main/java/de/uni_stuttgart/tik/viplab/websocket_api/validation/ConfigurationValidator.java)
* [ConfigurationTemplateRenderer](internal-extension-api/src/main/java/de/uni_stuttgart/tik/viplab/websocket_api/transformation/ConfigurationTemplateRenderer.java)
