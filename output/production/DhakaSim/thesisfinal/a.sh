#!/bin/bash

for i in {1..100}
do 
	javac DhakaSim.java
	java DhakaSim
	sleep 10s
done
