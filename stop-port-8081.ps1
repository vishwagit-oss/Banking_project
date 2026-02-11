# Stop whatever is using port 8081 (so you can restart Account Service)
$port = 8081
$found = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
if ($found) {
    $found | ForEach-Object { Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue }
    Write-Host "Stopped process(es) on port $port. You can now run .\run-account-service.ps1 again." -ForegroundColor Green
} else {
    Write-Host "Nothing is using port $port." -ForegroundColor Yellow
}
