param([switch]$NoBrowser)
$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$runtime = Join-Path $root 'runtime'
New-Item -ItemType Directory -Force -Path $runtime | Out-Null

function Test-ListeningPort([int]$Port) {
    foreach ($line in (netstat -ano -p tcp)) {
        if ($line -match ":$Port\s+.*LISTENING") { return $true }
    }
    return $false
}

function Wait-ListeningPort([int]$Port, [int]$TimeoutSeconds) {
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-ListeningPort $Port) { return $true }
        Start-Sleep -Milliseconds 500
    }
    return $false
}

function Save-PortPid([int]$Port, [string]$File) {
    foreach ($line in (netstat -ano -p tcp)) {
        if ($line -match ":$Port\s+.*LISTENING\s+(\d+)\s*$") {
            Set-Content -LiteralPath $File -Value $Matches[1] -Encoding ASCII
            return
        }
    }
}

$mysqlHome = 'C:\devtools\mysql-8.0.40-winx64'
$mysqlExe = Join-Path $mysqlHome 'bin\mysqld.exe'
$mysqlIni = Join-Path $mysqlHome 'my.ini'
$javaExe = 'C:\devtools\jdk8\bin\java.exe'
$jar = Join-Path $root 'backend\target\psy-test.jar'
$nodeExe = 'C:\devtools\nodejs\node.exe'
$vueCli = Join-Path $root 'frontend\node_modules\vite\bin\vite.js'

if (-not (Test-ListeningPort 3306)) {
    Write-Host 'Starting MySQL...'
    if (-not (Test-Path -LiteralPath $mysqlExe)) { throw "MySQL not found: $mysqlExe" }
    Start-Process -FilePath $mysqlExe -ArgumentList "--defaults-file=$mysqlIni" -WorkingDirectory $mysqlHome -WindowStyle Hidden | Out-Null
}
if (-not (Wait-ListeningPort 3306 30)) { throw 'MySQL failed to start on port 3306.' }
Save-PortPid 3306 (Join-Path $runtime 'mysql.pid')

if (-not (Test-ListeningPort 8081)) {
    Write-Host 'Starting backend on port 8081...'
    if (-not (Test-Path -LiteralPath $jar)) { throw "Backend jar not found: $jar" }
    Start-Process -FilePath $javaExe -ArgumentList @('-jar', $jar) -WorkingDirectory (Join-Path $root 'backend') -RedirectStandardOutput (Join-Path $runtime 'backend.out.log') -RedirectStandardError (Join-Path $runtime 'backend.err.log') -WindowStyle Hidden | Out-Null
}
if (-not (Wait-ListeningPort 8081 60)) { throw 'Backend failed to start. Check runtime\backend.out.log.' }
Save-PortPid 8081 (Join-Path $runtime 'backend.pid')

if (-not (Test-ListeningPort 8088)) {
    Write-Host 'Starting frontend on port 8088...'
    if (-not (Test-Path -LiteralPath $vueCli)) { throw "Frontend dependencies not found. Run npm ci in frontend first." }
    Start-Process -FilePath $nodeExe -ArgumentList @($vueCli, '--host', '127.0.0.1', '--port', '8088') -WorkingDirectory (Join-Path $root 'frontend') -RedirectStandardOutput (Join-Path $runtime 'frontend.out.log') -RedirectStandardError (Join-Path $runtime 'frontend.err.log') -WindowStyle Hidden | Out-Null
}
if (-not (Wait-ListeningPort 8088 90)) { throw 'Frontend failed to start. Check runtime\frontend.out.log.' }
Save-PortPid 8088 (Join-Path $runtime 'frontend.pid')

Write-Host ''
Write-Host 'Psych test system is running:' -ForegroundColor Green
Write-Host '  http://localhost:8088/'
Write-Host '  Admin: admin / 123456' -ForegroundColor Yellow
Write-Host ''
if (-not $NoBrowser) { Start-Process 'http://localhost:8088/' }



