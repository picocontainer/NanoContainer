#!/bin/bash

# NanoContainer Booter script v @VER@
# www.nanocontainer.org

EXEC="$JAVA_HOME/bin/java -Djava.security.manager -Djava.security.policy=file:lib/booter.policy -jar lib/nanocontainer-booter-@VER@.jar $@"
echo $EXEC
$EXEC

