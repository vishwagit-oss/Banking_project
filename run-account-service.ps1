# Run Account Service - finds Java and runs Maven (PowerShell)
$ErrorActionPreference = "Stop"
$projectRoot = $PSScriptRoot

# Common JDK locations (check in order)
$pathsToCheck = @(
    $env:JAVA_HOME,
    "C:\Program Files\Java\jdk-21",
    "C:\Program Files\Java\jdk-17",
    "C:\Program Files\Java\jdk-11",
    "C:\Program Files\Eclipse Adoptium\jdk-21*",
    "C:\Program Files\Eclipse Adoptium\jdk-17*",
    "C:\Program Files\Microsoft\jdk-21*",
    "C:\Program Files\Microsoft\jdk-17*",
    "C:\Program Files\Zulu\zulu-*"
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
    Write-Host "No JDK found. Install one, then run this script again." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Install with winget (run in PowerShell as Administrator):" -ForegroundColor Cyan
    Write-Host '  winget install EclipseAdoptium.Temurin.17.JDK --accept-package-agreements' -ForegroundColor White
    Write-Host ""
    Write-Host "Or download from: https://adoptium.net/" -ForegroundColor Cyan
    Write-Host "After install, close and reopen PowerShell, or set:" -ForegroundColor Cyan
    Write-Host '  $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot"' -ForegroundColor White
    exit 1
}

$env:JAVA_HOME = $javaHome
Write-Host "Using JAVA_HOME: $javaHome" -ForegroundColor Green
Set-Location $projectRoot
& "$projectRoot\mvnw.cmd" spring-boot:run "-pl" "account-service"
