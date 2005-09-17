#!/bin/bash

EXEC="$JAVA_HOME/bin/java -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 -Djava.security.manager -Djava.security.policy=file:lib/booter.policy -jar lib/nanocontainer-booter.jar $@"
echo $EXEC
$EXEC

