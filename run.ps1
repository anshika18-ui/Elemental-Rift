# Elemental Rift - PowerShell Runner
Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "       LAUNCHING ELEMENTAL RIFT ARENA...           " -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan

$classFile = "target/classes/com/elementalrift/Launcher.class"
if (-not (Test-Path $classFile)) {
    Write-Host "Building project first..." -ForegroundColor Yellow
    cmd /c build.bat
}

$classpath = "target/classes;lib/javafx-base.jar;lib/javafx-graphics.jar;lib/javafx-controls.jar"
java -cp $classpath com.elementalrift.Launcher
