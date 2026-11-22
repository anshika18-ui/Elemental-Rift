@echo off
setlocal
echo ====================================================
echo    RUNNING ELEMENTAL RIFT OOP TEST SUITE
echo ====================================================

call build.bat
if %ERRORLEVEL% neq 0 exit /b %ERRORLEVEL%

java -cp "target\classes;lib\javafx-base.jar;lib\javafx-graphics.jar;lib\javafx-controls.jar" com.elementalrift.test.TestRunner
