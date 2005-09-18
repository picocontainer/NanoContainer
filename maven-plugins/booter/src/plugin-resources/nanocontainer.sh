#!/bin/bash

# NanoContainer Booter script v @booter-version@
# http://www.nanocontainer.org/booter

EXEC="$JAVA_HOME/bin/java -Djava.security.manager -Djava.security.policy=file:lib/booter.policy -jar lib/nanocontainer-booter-@booter-version@.jar $@"
echo $EXEC
$EXEC

