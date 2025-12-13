# tflite-agent-macos

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

**Option 1: Use the wrapper script (recommended)**
```shell script
./run-dev.sh
```

**Option 2: Set environment variable manually**
```shell script
export DYLD_LIBRARY_PATH=/opt/homebrew/opt/libtensorflow/lib:$DYLD_LIBRARY_PATH
./mvnw quarkus:dev
```

**Option 3: Add to your shell profile (persistent)**
Add this to your `~/.zshrc` (or `~/.bash_profile` if using bash):
```bash
export DYLD_LIBRARY_PATH=/opt/homebrew/opt/libtensorflow/lib:$DYLD_LIBRARY_PATH
```

Then reload and run:
```bash
source ~/.zshrc
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

> **_NOTE:_**  The TensorFlow native library must be available. If installed via Homebrew (`brew install libtensorflow`), it's located at `/opt/homebrew/opt/libtensorflow/lib/`. The library path is configured in `pom.xml` for tests, but for dev mode you need to set `DYLD_LIBRARY_PATH` as shown above.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using:

```shell script
export DYLD_LIBRARY_PATH=/opt/homebrew/opt/libtensorflow/lib:$DYLD_LIBRARY_PATH
java -jar target/quarkus-app/quarkus-run.jar
```

Or:

```shell script
java -Djava.library.path=/opt/homebrew/opt/libtensorflow/lib -jar target/quarkus-app/quarkus-run.jar
```

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

You can then execute your native executable with: `./target/tflite-agent-macos-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Model Setup

This application uses TensorFlow SavedModel format. If you have a TFLite model (`.tflite` file), you need to convert it to SavedModel format.

### Converting TFLite to SavedModel

A conversion script is provided:

```bash
python3 convert_tflite_to_savedmodel.py models/mnist.tflite models/mnist
```

**Requirements:**
- Python 3
- TensorFlow 2.x installed: `pip install tensorflow`

The script will:
1. Load your TFLite model
2. Create a SavedModel wrapper
3. Save it to the specified directory

After conversion, the SavedModel will be in `models/mnist/` and can be used by the application.

## Related Guides

- REST Jackson ([guide](https://quarkus.io/guides/rest#json-serialisation)): Jackson serialization support for Quarkus REST. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
