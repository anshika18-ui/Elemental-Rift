@echo off
setlocal
echo ====================================================
echo        ELEMENTAL RIFT - BUILD SCRIPT
echo ====================================================

if not exist "target\classes" mkdir target\classes

echo [1/3] Gathering Java source files...
powershell -NoProfile -Command "Get-ChildItem -Path 'src/main/java' -Filter '*.java' -Recurse | ForEach-Object { '\"' + $_.FullName.Replace('\', '/') + '\"' } | Out-File -Encoding ASCII sources.txt"

echo [2/3] Compiling Java classes with JavaFX libraries...
javac -d target/classes -cp "lib/javafx-base.jar;lib/javafx-graphics.jar;lib/javafx-controls.jar" @sources.txt
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Compilation failed!
    if exist sources.txt del sources.txt
    exit /b %ERRORLEVEL%
)
if exist sources.txt del sources.txt

echo [3/3] Copying application resources (CSS)...
xcopy /s /y /q src\main\resources\* target\classes\ >nul 2>nul

echo.
echo ====================================================
echo  BUILD SUCCESSFUL! Classes in target/classes
echo ====================================================
