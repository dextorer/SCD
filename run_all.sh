#!/bin/sh

echo "Building core..\n"


echo "Starting core..\n"
./obj/soccer-main

echo "Starting field client..\n"
java -jar ../GUISoccerField/
