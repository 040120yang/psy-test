$ErrorActionPreference = 'SilentlyContinue'
$runtime = Join-Path $PSScriptRoot 'runtime'
foreach ($name in @('frontend', 'backend')) {
    $pidFile = Join-Path $runtime "$name.pid"
    if (Test-Path -LiteralPath $pidFile) {
        $pidValue = [int](Get-Content -LiteralPath $pidFile -Raw)
        $process = Get-Process -Id $pidValue -ErrorAction SilentlyContinue
        if ($process) {
            Stop-Process -Id $pidValue -Force
            Write-Host "Stopped $name (PID $pidValue)."
        }
        Remove-Item -LiteralPath $pidFile -Force
    }
}
Write-Host 'Website stopped. MySQL is kept running for reuse.' -ForegroundColor Green
