# api-perf-resilience

# API Performance and Resilience with Quarkus
Add caching, rate limiting, and fault tolerance to your REST APIs.

This project demonstrates:
- HTTP caching headers, strong ETags, and conditional requests.
- Rate limiting with the Quarkiverse Bucket4j extension.
- Resilience patterns using MicroProfile Fault Tolerance.

Full article on Substack <https://www.the-main-thread.com/p/quarkus-api-performance-caching-rate-limiting-fault-tolerance>


## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```


##Caching and ETags

First GET returns 200, an ETag, and Cache-Control headers:

```shell script
curl -i http://localhost:8080/products/1
```

##Use the ETag in If-None-Match to perform a conditional GET:

```shell script
ETAG=$(curl -sI http://localhost:8080/products/1 | awk -F': ' 'tolower($1)=="etag"{print $2}' | tr -d '\r')
curl -i -H "If-None-Match: $ETAG" http://localhost:8080/products/1
```

##Change the resource to force a new ETag:

```shell script
curl -i -X PUT http://localhost:8080/products/1/stock/41
```

##Rate limiting

Test rate limiting (expect HTTP 429 after exceeding limits):

```shell script
for i in $(seq 1 15); do
  curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/limited-products/1
done
```

##Fault tolerance

Trigger retries, timeouts, and circuit breaker behavior:

```shell script
for i in $(seq 1 20); do
  printf "%02d: " $i
  curl -s http://localhost:8080/prices/2; echo
done
```
