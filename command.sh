#!/bin/bash
cd src/thesisfinal
javac -encoding UTF-8 *.java
cd ../..
java -cp src thesisfinal.DhakaSim gauss 1000 1 1 1 1 1
