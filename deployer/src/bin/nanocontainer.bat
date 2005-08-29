@echo off

set NANO_CLASSPATH=
for %%i in ("%~dp0..\lib\*.jar") do call "%~dp0.\\lcp.bat" %%i

%JAVA_HOME%\bin\java -cp %NANO_CLASSPATH% org.nanocontainer.Standalone -c nanocontainer.groovy