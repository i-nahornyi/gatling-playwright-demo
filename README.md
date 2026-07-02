# gatling-playwright-demo

### Build
```shell
docker build -t gatling-playwright . --no-cache
```

### Run
```shell
docker run --rm -v gatling_result:/gatling_test/target/gatling \
                      gatling-playwright \
                      com.demo.simulation.DemoSimulation
```