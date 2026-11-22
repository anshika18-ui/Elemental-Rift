# Elemental Rift - PowerShell Test Runner
Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "    RUNNING ELEMENTAL RIFT OOP TEST SUITE           " -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan

cmd /c build.bat
$classpath = "target/classes;lib/javafx-base.jar;lib/javafx-graphics.jar;lib/javafx-controls.jar"
java -cp $classpath com.elementalrift.test.TestRunner
