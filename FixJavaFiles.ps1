# Fix corrupted Java files
Write-Host "Fixing corrupted Java files..." -ForegroundColor Yellow

Get-ChildItem -Path "src" -Filter "*.java" -Recurse | ForEach-Object {
    $filePath = $_.FullName
    $fileName = $_.Name
    
    try {
        # Read the file
        $content = Get-Content $filePath -Raw
        
        # Fix the corrupted package declaration
        $content = $content -replace "^ackage\s+", "package "
        
        # Remove BOM if present
        if ($content.StartsWith([char]0xFEFF)) {
            $content = $content.Substring(1)
        }
        
        # Write back without BOM
        $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
        [System.IO.File]::WriteAllText($filePath, $content, $utf8NoBom)
        
        Write-Host "  Fixed: $fileName" -ForegroundColor Green
    } catch {
        Write-Host "  Error fixing: $fileName - $_" -ForegroundColor Red
    }
}

Write-Host "`n✓ Java files fixed!" -ForegroundColor Green

# Now verify with a build
Write-Host "`nVerifying fix with build..." -ForegroundColor Yellow
./gradlew clean build -x test
