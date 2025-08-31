# quarkus-mutation-demo

Mutation Testing in Quarkus: Go Beyond Code Coverage
Learn how to expose weak tests, strengthen your assertions, and build enterprise-grade confidence with PIT in Java applications.

Substack article: <https://www.the-main-thread.com/p/mutation-testing-quarkus-java-tutorial>

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the tests

```shell script
./mvnw -q test
```

## Running PIT 

```shell script
./mvnw -q org.pitest:pitest-maven:mutationCoverage
```

## Looking at the results

```shell script
open target/pit-reports/index.html
```