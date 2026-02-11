# Run Notification Service - finds Java and runs Maven (PowerShell)
$ErrorActionPreference = "Stop"
$projectRoot = $PSScriptRoot

$pathsToCheck = @(
    $env:JAVA_HOME,
    "C:\Program Files\Java\jdk-21",
    "C:\Program Files\Java\jdk-17",
    "C:\Program Files\Eclipse Adoptium\jdk-21*",
    "C:\Program Files\Eclipse Adoptium\jdk-17*",
    "C:\Program Files\Microsoft\jdk-21*",
    "C:\Program Files\Microsoft\jdk-17*"
)

$javaHome = $null
foreach ($p in $pathsToCheck) {
    if (-not $p) { continue }
    $resolved = $null
    if ($p -like "**") {
        $dirs = Get-Item $p -ErrorAction SilentlyContinue
        if ($dirs) { $resolved = @($dirs)[0].FullName }
    } else {
        $resolved = $p
    }
    if ($resolved -and (Test-Path "$resolved\bin\java.exe")) {
        $javaHome = $resolved
        break
    }
}

if (-not $javaHome) {
    Write-Host "No JDK found. Set JAVA_HOME or install JDK 17+ from https://adoptium.net/" -ForegroundColor Yellow
    exit 1
}

$env:JAVA_HOME = $javaHome
Write-Host "Using JAVA_HOME: $javaHome" -ForegroundColor Green
Set-Location $projectRoot
& "$projectRoot\mvnw.cmd" spring-boot:run "-pl" "notification-service"
