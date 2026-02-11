@echo off
REM Edit the line below to match your JDK install folder, then run this script
REM before running mvnw.cmd (or run: call set-JAVA_HOME.cmd && mvnw.cmd spring-boot:run -pl account-service)

set "JAVA_HOME=C:\Program Files\Java\jdk-17"

REM Common alternatives - uncomment one if needed:
REM set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.9.9-hotspot"
REM set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.9.9"

if not exist "%JAVA_HOME%\bin\java.exe" (
  echo JAVA_HOME is set to: %JAVA_HOME%
  echo but java.exe was not found there. Edit this file with the correct path.
  pause
  exit /b 1
)

echo JAVA_HOME set to %JAVA_HOME%
echo You can now run: mvnw.cmd spring-boot:run -pl account-service
