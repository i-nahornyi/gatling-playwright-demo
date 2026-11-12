#!/bin/sh
set -x

echo "Entrypoint started"
echo "simulationClass: $1"

xvfb-run mvn exec:java -Dexec.classpathScope=test -Dexec.mainClass="com.demo.Runner" -Dgatling.simulationClass="$1"