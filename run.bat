@echo off
setlocal
echo ====================================================
echo       LAUNCHING ELEMENTAL RIFT ARENA...
echo ====================================================

if not exist "target\classes\com\elementalrift\Launcher.class" (
    echo Building project first...
    call build.bat
    if %ERRORLEVEL% neq 0 exit /b %ERRORLEVEL%
)

java -cp "target/classes;lib/javafx-base.jar;lib/javafx-graphics.jar;lib/javafx-controls.jar" com.elementalrift.Launcher
