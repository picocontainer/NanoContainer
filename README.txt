To build JRubyContainerBuilder:

- m1: copy lib/jruby-0.9.0.jar to ~/.maven/repository/org.jruby/jars/jruby-0.9.0.jar
- m2: 
mvn install:install-file -Dfile=lib/jruby-0.9.0.jar -DgroupId=org.jruby -DartifactId=jruby -Dversion=0.9.0 -Dpackaging=jar


