@echo off
setlocal

powershell.exe -NoProfile -ExecutionPolicy Bypass ^
-File "%~dp0install-naftah.ps1"

if errorlevel 1 (
echo.
echo فشل تثبيت نفطه.
pause
exit /b 1
)

echo.
echo اكتمل تثبيت نفطه بنجاح.
pause
exit /b 0