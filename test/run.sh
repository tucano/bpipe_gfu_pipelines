#!/bin/bash

TESTS=`find . -maxdepth 1 -type d | grep "[A-Za-z]"`

for t in $TESTS;
do
	echo "============== $t ================"
	cd "$t/";
	./runner.sh 1>run.log 2>run.err;
	RESULT=$?
	if [ $RESULT == 0 ]; then
		echo "--> SUCCEEDED";
	else
		echo "--> FAILED";
	fi
	cd ..
done
