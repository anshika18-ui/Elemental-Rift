@echo off
setlocal
where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
    mvn %*
    exit /b %ERRORLEVEL%
)

if exist "D:\netbeans\java\maven\bin\mvn.cmd" (
    call "D:\netbeans\java\maven\bin\mvn.cmd" %*
    exit /b %ERRORLEVEL%
)

echo [ERROR] Maven not found in PATH or at D:\netbeans\java\maven\bin\mvn.cmd.
exit /b 1
