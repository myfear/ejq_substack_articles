# ai-progress-tracker

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/ai-progress-tracker-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

- REST Jackson ([guide](https://quarkus.io/guides/rest#json-serialisation)): Jackson serialization support for Quarkus REST. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it
- LangChain4j Easy RAG ([guide](https://docs.quarkiverse.io/quarkus-langchain4j/dev/rag-easy-rag.html)): Provides the Easy RAG functionality with LangChain4j
- LangChain4j Ollama ([guide](https://docs.quarkiverse.io/quarkus-langchain4j/dev/index.html)): Provides the basic integration of Ollama with LangChain4j

## Provided Code

### LangChain4j Easy RAG

This code is a very basic sample service to start developing with Quarkus LangChain4j using Easy RAG.

You have to add an extension that provides an embedding model. For that, you can choose from the plethora of extensions like quarkus-langchain4j-openai, quarkus-langchain4j-ollama, or import an in-process embedding model - these have the advantage of not having to send data over the wire.

In `./easy-rag-catalog/` you can find a set of example documents that will be used to create the RAG index which the bot (`src/main/java/org/acme/Bot.java`) will ingest.

On first run, the bot will create the RAG index and store it in `easy-rag-catalog.json` file and reuse it on subsequent runs.
This can be disabled by setting the `quarkus.langchain4j.easy-rag.reuse-embeddings.enabled` property to `false`.

Add it to a Rest endpoint:
```java
    @Inject
    Bot bot;
    
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String chat(String q) {
        return bot.chat(q);
    }
```

In a more complete example, you would have a web interface and use websockets that would provide more interactive experience, see [ChatBot Easy RAG Sample](https://github.com/quarkiverse/quarkus-langchain4j/tree/main/samples/chatbot-easy-rag) for such an example.
### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
