@echo off

REM SPDX-License-Identifier: Apache-2.0
REM Copyright © The Naftah Project Authors

@if /I not "%DEBUG%" == "true" goto checkPowerShell
@echo on

:checkPowerShell
setlocal
chcp 65001 > nul

echo جارٍ التحقق من إصدار PowerShell...

REM التحقق من توفر PowerShell 7
where pwsh.exe >nul 2>&1

if not errorlevel 1 goto runInstaller

REM PowerShell 7 غير متوفر؛ التحقق من winget
echo لم يتم العثور على PowerShell 7. جارٍ محاولة تثبيته...

where winget.exe >nul 2>&1

if errorlevel 1 (
    echo.
    echo PowerShell 7 وأداة winget غير متوفرين.
    echo يُرجى تثبيت PowerShell 7 يدويًا.
    pause
    exit /b 1
)

winget.exe install ^
    --id Microsoft.PowerShell ^
    --exact ^
    --accept-package-agreements ^
    --accept-source-agreements

if errorlevel 1 (
    echo.
    echo فشل تثبيت PowerShell 7.
    pause
    exit /b 1
)

:runInstaller
REM تحديد مسار PowerShell 7
set "PWSH=%ProgramFiles%\PowerShell\7\pwsh.exe"

if exist "%PWSH%" goto executeInstaller

REM البحث عن PowerShell 7 في PATH
for /f "delims=" %%P in ('where pwsh.exe 2^>nul') do (
    set "PWSH=%%P"
    goto executeInstaller
)

echo.
echo تم تثبيت PowerShell 7، ولكن تعذر العثور على الملف التنفيذي الخاص به.
echo يُرجى فتح نافذة طرفية جديدة ثم إعادة المحاولة.
pause
exit /b 1

:executeInstaller
echo جارٍ تشغيل مثبت Naftah باستخدام PowerShell 7...

"%PWSH%" -NoProfile -ExecutionPolicy Bypass ^
    -Command "[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new(); & '%~dp0install-naftah.ps1'; exit $LASTEXITCODE"

if errorlevel 1 (
    echo.
    echo فشل تثبيت Naftah.
    pause
    exit /b 1
)

echo.
echo اكتمل تثبيت Naftah بنجاح.
pause
exit /b 0