# Master Fix Script for DemoBlaze Framework
Write-Host "🔧 DEMOBLAZE FRAMEWORK MASTER FIX" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan

# Function to write files without BOM
function Write-FileWithoutBOM {
    param([string]$Path, [string]$Content)
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($Path, $Content, $utf8NoBom)
}

# Function to backup files
function Backup-File {
    param([string]$FilePath)
    if (Test-Path $FilePath) {
        $backupPath = $FilePath + ".backup." + (Get-Date -Format "yyyyMMdd_HHmmss")
        Copy-Item $FilePath $backupPath
        Write-Host "📁 Backed up: $FilePath" -ForegroundColor Gray
    }
}

Write-Host "`n🧹 STEP 1: CLEANUP CONFLICTING FILES" -ForegroundColor Yellow

# Remove duplicate GlobalHooks file
$duplicateHooks = "src/test/java/com/demoblaze/stepdefinitions/web/GlobalHooks.java"
if (Test-Path $duplicateHooks) {
    Backup-File $duplicateHooks
    Remove-Item $duplicateHooks -Force
    Write-Host "✅ Removed duplicate GlobalHooks.java" -ForegroundColor Green
}

# Remove WebHooks file 
$webHooks = "src/test/java/com/demoblaze/stepdefinitions/web/WebHooks.java"
if (Test-Path $webHooks) {
    Backup-File $webHooks
    Remove-Item $webHooks -Force
    Write-Host "✅ Removed WebHooks.java" -ForegroundColor Green
}

Write-Host "✅ Cleanup complete!" -ForegroundColor Green
