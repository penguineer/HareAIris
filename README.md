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
* `OPENAI_API_KEY`: API key for accessing the OpenAI API
* `OPENAI_ENDPOINT`: Base URL for the OpenAI API (default `https://api.openai.com/v1`)

## Maintainers

* Stefan Haun ([@penguineer](https://github.com/penguineer))

## Contributing

PRs are welcome!

If possible, please stick to the following guidelines:

* Keep PRs reasonably small and their scope limited to a feature or module within the code.
* If a large change is planned, it is best to open a feature request issue first, then link subsequent PRs to this
  issue, so that the PRs move the code towards the intended feature.

## License

[MIT](LICENSE.txt) © 2024 Stefan Haun and contributors
