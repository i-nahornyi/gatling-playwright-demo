FROM mcr.microsoft.com/playwright/java:v1.62.0-noble

WORKDIR gatling_test

EXPOSE 6080

COPY docker/entrypoint.sh ./entrypoint.sh
COPY pom.xml .
COPY src ./src

RUN chmod +x ./entrypoint.sh
RUN mvn clean test-compile
RUN mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install --with-deps"


ENTRYPOINT ["./entrypoint.sh"]