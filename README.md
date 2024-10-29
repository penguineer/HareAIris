# HareAIris

> HareAIris is a software bridge connecting RabbitMQ to the OpenAI API. It is designed to help developers integrate
> message-driven applications with advanced AI capabilities.

The name "HareAIris" is a creative blend of "AI," "Iris," and "Hare." In mythology, Iris is the Greek goddess of the
rainbow and a messenger of the gods, symbolizing communication and connection. The "Hare" element represents speed and
agility, attributes often associated with rabbits. Together, "HareAIris" signifies a swift and efficient messenger that
bridges the gap between RabbitMQ and OpenAI, embodying the project's core functionality.

## Configuration

Configuration is done using environment variables:

* `PORT`: Port for the HTTP endpoint (default `8080`, only change when running locally!)
* `RMQ_HOST`: Host for RabbitMQ (default `localhost`)
* `RMQ_PORT`: Port for RabbitMQ (default `5672`)
* `RMQ_USER`: Username for RabbitMQ (default `guest`)
* `RMQ_PASSWORD`: Password for RabbitMQ (default `guest`)
* `RMQ_VHOST`: Virtual host for RabbitMQ (default `/`)
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
