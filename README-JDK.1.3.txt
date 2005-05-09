In order to build pico/nano for JDK 1.3, perform the following steps:

1) Uncomment lines around line 110 in project.xml
2) Define your JAVA_HOME to point to a JDK 1.3

Then run:

maven -Djvm.version=1.3

from this folder. It won't build everything, as there are some things that are not JDK 1.3 compliant (and will never be).