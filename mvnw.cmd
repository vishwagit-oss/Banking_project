@REM ----------------------------------------------------------------------------
@REM Maven Wrapper batch script - auto-downloads wrapper JAR if missing
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set "MAVEN_PROJECTBASEDIR=%~dp0"
if not "%MAVEN_PROJECTBASEDIR%"=="" set "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"

@REM Find project base dir (containing .mvn)
set "EXEC_DIR=%CD%"
set "WDIR=%EXEC_DIR%"
:findBaseDir
if exist "%WDIR%\.mvn" goto baseDirFound
cd ..
if "%WDIR%"=="%CD%" goto baseDirNotFound
set "WDIR=%CD%"
goto findBaseDir
:baseDirFound
set "MAVEN_PROJECTBASEDIR=%WDIR%"
cd "%EXEC_DIR%"
goto endDetectBaseDir
:baseDirNotFound
set "MAVEN_PROJECTBASEDIR=%EXEC_DIR%"
cd "%EXEC_DIR%"
:endDetectBaseDir

if not "%JAVA_HOME%"=="" goto OkJHome
@REM Auto-detect JAVA_HOME from Java on PATH (PowerShell)
for /f "usebackq tokens=* delims=" %%j in (`powershell -NoProfile -Command "try { $j=(Get-Command java -EA Stop).Source; (Get-Item $j).Directory.Parent.FullName } catch {}"`) do set "JAVA_HOME=%%j"
if not "%JAVA_HOME%"=="" if exist "%JAVA_HOME%\bin\java.exe" goto OkJHome
echo ERROR: JAVA_HOME is not set and Java was not found on PATH. >&2
echo. >&2
echo Set JAVA_HOME for this session, e.g.: >&2
echo   set "JAVA_HOME=C:\Program Files\Java\jdk-17" >&2
echo Or install JDK 17+ from https://adoptium.net/ and add its \bin to PATH. >&2
exit /b 1
:OkJHome
if exist "%JAVA_HOME%\bin\java.exe" goto init
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% >&2
exit /b 1

:init
set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"

@REM Download wrapper JAR if not present
if exist "%WRAPPER_JAR%" goto run
echo Downloading Maven Wrapper...
set "DOWNLOAD_URL=https://repo.maven.apache.org/maven2/io/takari/maven-wrapper/0.5.6/maven-wrapper-0.5.6.jar"
if not exist "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper" mkdir "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper"
powershell -NoProfile -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile('%DOWNLOAD_URL%', '%WRAPPER_JAR%')"
if not exist "%WRAPPER_JAR%" (
  echo Failed to download Maven Wrapper. Check your network. >&2
  exit /b 1
)
:run
"%JAVA_HOME%\bin\java.exe" -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*
exit /b %ERRORLEVEL%
