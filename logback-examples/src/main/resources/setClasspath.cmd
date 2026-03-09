
@echo off
REM This script will add logback jars to your classpath.

set LB_HOME=c:/SET/THIS/PARAMETER/TO/THE/FOLDER/WHERE/YOU/INSTALLED/LOGBACK
REM echo %LB_HOME%

set CLASSPATH=%CLASSPATH%;%LB_HOME%/bjca-footstone-bogback-classic-${project.version}.jar
set CLASSPATH=%CLASSPATH%;%LB_HOME%/bjca-footstone-bogback-core-${project.version}.jar
set CLASSPATH=%CLASSPATH%;%LB_HOME%/logback-examples/bjca-footstone-bogback-examples-${project.version}.jar
set CLASSPATH=%CLASSPATH%;%LB_HOME%/logback-examples/lib/slf4j-api-${slf4j.version}.jar

REM echo %CLASSPATH%
