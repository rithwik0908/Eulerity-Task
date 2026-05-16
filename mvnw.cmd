@echo off
set BASE_DIR=%~dp0
set MAVEN_HOME=%BASE_DIR%\.mvn\wrapper\apache-maven-3.9.11
set ZIP_FILE=%BASE_DIR%\.mvn\wrapper\apache-maven-3.9.11-bin.zip
if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.11/apache-maven-3.9.11-bin.zip -OutFile '%ZIP_FILE%'; Expand-Archive -Path '%ZIP_FILE%' -DestinationPath '%BASE_DIR%\.mvn\wrapper' -Force"
)
"%MAVEN_HOME%\bin\mvn.cmd" %*
