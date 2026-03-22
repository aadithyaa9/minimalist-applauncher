@rem Gradle startup script for Windows
@if "%DEBUG%"=="" @echo off
setlocal
set DIRNAME=%~dp0
set APP_HOME=%DIRNAME%
set JAVACMD=java
%JAVACMD% -classpath "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
