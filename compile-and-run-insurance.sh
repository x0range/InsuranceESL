#!/bin/bash

find ./src -name "*.java" -print| xargs javac -cp "/usr/share/java/*"
if [[ $? -eq 0 ]]; then
	cd src/
	find . -name "*.class" -print | xargs jar cvf InsuranceESL.jar
	find . -name "*.class" -print | xargs rm
	mv InsuranceESL.jar ../
	cd ..
	java -cp ./*:/usr/share/java/* insurance.Insurance
fi 
