@echo off
REM SPDX-License-Identifier: Apache-2.0
REM Copyright © The Naftah Project Authors

@if /I not "%DEBUG%" == "true" goto callNaftahShell
@echo on

:callNaftahShell
call "%~dp0naftah-shell.bat" shell %*