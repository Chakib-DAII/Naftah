@echo off

REM SPDX-License-Identifier: Apache-2.0
REM Copyright © The Naftah Project Authors

@if /I not "%DEBUG%" == "true" goto checkPowerShell
@echo on

:checkPowerShell
setlocal
chcp 65001 > nul

echo Checking PowerShell version...

REM Check whether PowerShell 7 is available
where pwsh.exe >nul 2>&1

if not errorlevel 1 goto runInstaller

REM PowerShell 7 is not available; check winget
echo PowerShell 7 was not found. Attempting installation...

where winget.exe >nul 2>&1

if errorlevel 1 (
    echo.
    echo PowerShell 7 and winget are unavailable.
    echo Please install PowerShell 7 manually.
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
    echo Failed to install PowerShell 7.
    pause
    exit /b 1
)

:runInstaller
REM Locate PowerShell 7
set "PWSH=%ProgramFiles%\PowerShell\7\pwsh.exe"

if exist "%PWSH%" goto executeInstaller

REM Fall back to PATH lookup
for /f "delims=" %%P in ('where pwsh.exe 2^>nul') do (
    set "PWSH=%%P"
    goto executeInstaller
)

echo.
echo PowerShell 7 was installed but could not be located.
echo Please open a new terminal and try again.
pause
exit /b 1

:executeInstaller
echo Running Naftah installer with PowerShell 7...

"%PWSH%" -NoProfile -ExecutionPolicy Bypass ^
    -Command "[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new(); & '%~dp0install-naftah.ps1'; exit $LASTEXITCODE"

if errorlevel 1 (
    echo.
    echo Failed to install Naftah.
    pause
    exit /b 1
)

echo.
echo Naftah was installed successfully.
pause
exit /b 0