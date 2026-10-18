@echo off

REM SPDX-License-Identifier: Apache-2.0
REM Copyright © The Naftah Project Authors

@if /I not "%DEBUG%" == "true" goto setUtf8
@echo on

:setUtf8
chcp 65001 > nul

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

powershell.exe -NoProfile -ExecutionPolicy Bypass ^
-Command "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; & '%~dp0install-naftah.ps1'"

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
