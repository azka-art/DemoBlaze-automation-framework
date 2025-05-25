# DemoBlaze User Setup Diagnostic and Fix
Write-Host "Testing user setup for testuser2025..." -ForegroundColor Cyan

# Add the necessary Java classpath
$classpath = @(
    "build/classes/java/main",
    "build/classes/java/test",
    "build/libs/*"
) -join ";"

# First, let's compile if needed
Write-Host "`nCompiling project..." -ForegroundColor Yellow
./gradlew classes testClasses

# Now run the UserDiagnostic if it exists
if (Test-Path "src/test/java/com/demoblaze/utils/UserDiagnostic.java") {
    Write-Host "`nRunning UserDiagnostic..." -ForegroundColor Yellow
    java -cp $classpath com.demoblaze.utils.UserDiagnostic
} else {
    Write-Host "UserDiagnostic not found. Creating manual test..." -ForegroundColor Yellow
}
