#!/bin/bash

EXEC="$JAVA_HOME/bin/java -Djava.security.manager -Djava.security.policy=file:lib/booter.policy -jar lib/nanocontainer-booter.jar $@"
echo $EXEC
$EXEC

