# HareAIris

> HareAIris is a software bridge connecting RabbitMQ to the OpenAI API. It is designed to help developers integrate
> message-driven applications with advanced AI capabilities.

The name "HareAIris" is a creative blend of "AI," "Iris," and "Hare." In mythology, Iris is the Greek goddess of the
rainbow and a messenger of the gods, symbolizing communication and connection. The "Hare" element represents speed and
agility, attributes often associated with rabbits. Together, "HareAIris" signifies a swift and efficient messenger that
bridges the gap between RabbitMQ and OpenAI, embodying the project's core functionality.

## API

### Communication

The service listens for `ChatRequest` JSON objects on the RabbitMQ queue configured by the `RMQ_CHAT_REQUESTS`
environment variable, defaulting to `chat_requests`. The service will respond with a `ChatResponse` object or a
`ChatError` object in case of an error.

The response will be sent to the default exchange with the routing key defined in the `reply_to` *property* of the
request. This property must be provided in the request.

Errors will be sent to the default exchange with the routing key set in the `error_to` *header*. This header is optional,
but omitting it will prevent the client from receiving error messages and result in a warning printed to the log.

(Please note the difference between properties and headers in RabbitMQ.)

If a `correlation_id` property is set in the request, it will be copied to the response.

The service will acknowledge the message on success or client errors. In case of an internal error, the message
will be re-queued.

### ChatRequest

The `ChatRequest` object represents a request to the OpenAI API. It includes the following fields:

```json
{
  "system-message": "String",
  "prompt": "String",
  "max-tokens": "Integer",
  "temperature": "Double",
  "top-p": "Double",
  "presence-penalty": "Double",
  "frequency-penalty": "Double"
}
```

### ChatResponse

The `ChatResponse` object represents a response from the OpenAI API. It includes the following fields:

```json
{
  "response": "String",
  "input-tokens": "int",
  "output-tokens": "int"
}
```

### ChatError

The `ChatError` object represents an error response from the OpenAI API. It includes the following fields:

```json
{
  "code": "int",
  "message": "String"
}
```

### Monitoring

The service provides a health check endpoint at HTTP `/actuators/health` that returns a `200 OK` status code if the
service is running.

## Configuration

Configuration is done using environment variables:

* `PORT`: Port for the HTTP endpoint (default `8080`, only change when running locally!)
* `RMQ_HOST`: Host for RabbitMQ (default `localhost`)
* `RMQ_PORT`: Port for RabbitMQ (default `5672`)
* `RMQ_USER`: Username for RabbitMQ (default `guest`)
* `RMQ_PASSWORD`: Password for RabbitMQ (default `guest`)
* `RMQ_VHOST`: Virtual host for RabbitMQ (default `/`)
* `RMQ_CHAT_REQUESTS`: RabbitMQ queue for chat requests (default `chat_requests`)
* `OPENAI_API_KEY`: API key for accessing the OpenAI API
* `OPENAI_ENDPOINT`: Base URL for the OpenAI API (default `https://api.openai.com/v1`)

## Build

The build is split into two stages:

1. Packaging with [Maven](https://maven.apache.org/)
2. Building the Docker container

This means that the [Dockerfile](Dockerfile) expects one (and only one) JAR file in the target directory.
Build as follows:

```bash
mvn --batch-mode --update-snapshots clean package
docker build .
```

The whole process is coded in the [docker-publish workflow](.github/workflows/docker-build.yml) and only needs to be
executed manually for local builds.

## Run with Docker

With the configuration stored in a file `.env`, the service can be run as follows:

```bash
docker run --rm \
           -p 8080:8080 \
           --env-file .env \
           mrtux/hareairis
```

The service does not store any state and therefore needs no mount points or other persistence.

Please make sure to pin the container to a specific version in a production environment.

## Development

Version numbers are determined with [jgitver](https://jgitver.github.io/).
If you encounter a project version `0` there is an issue with the jgitver generator.

For local execution the configuration can be provided in a `.env` file and made available using `dotenv`:

```bash
dotenv ./mvnw mn:run
```

Note that `.env` is part of the `.gitignore` and can be safely stored in the local working copy.

## Maintainers

* Stefan Haun ([@penguineer](https://github.com/penguineer))

If you like my work, please consider [sponsoring me](https://github.com/sponsors/penguineer), as this helps me to spend
more time on open source projects.

## Contributing

PRs are welcome!

If possible, please stick to the following guidelines:

* Keep PRs reasonably small and their scope limited to a feature or module within the code.
* If a large change is planned, it is best to open a feature request issue first, then link subsequent PRs to this
  issue, so that the PRs move the code towards the intended feature.

## License

[MIT](LICENSE.txt) © 2024 Stefan Haun and contributors
