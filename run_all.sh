#!/bin/sh

BASE_DIR=$PWD
OBJ_DIR=Soccer-SCD/obj

echo "Performing cleanup..\n"
rm -rf Soccer-SCD/obj
cd GUISoccerField
ant clean
cd ..

if [ ! -d $OBJ_DIR ]; then
	echo "	Creating obj folder..\n"
	mkdir Soccer-SCD/obj
fi

echo "Building core..\n"
gnatmake -P Soccer-SCD/scd.gpr

echo "Building field GUI..\n"
cd GUISoccerField
ant all
cd ..

echo "Starting core..\n"
gnome-terminal -x ./Soccer-SCD/obj/soccer-main

sleep 1

echo "Starting field client..\n"
gnome-terminal -x java -jar GUISoccerField/out/artifacts/SoccerField_jar/SoccerField.jar
