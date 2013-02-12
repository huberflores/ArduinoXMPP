#!/bin/sh

java -cp target/lib/h2-1.3.165.jar org.h2.tools.Shell -url jdbc:h2:DB/arduinoDB -user sa
