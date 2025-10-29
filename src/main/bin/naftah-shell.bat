@echo off

@if /I not "%DEBUG%" == "true" goto setUtf8
@echo on

:setUtf8
chcp 65001 > nul

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

:setNaftahHome
@rem Setup NAFTAH_HOME if not already defined
if defined NAFTAH_HOME goto setJavaHome
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set NAFTAH_HOME=%DIRNAME%\..

:setJavaHome
@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome
set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto runNaftah
echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.
goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe
if exist "%JAVA_EXE%" goto runNaftah
echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.
goto fail

:runNaftah
@rem Get command-line arguments, handling Windows variants

if not "%OS%" == "Windows_NT" goto win9xME_args
if "%@eval[2+2]" == "4" goto 4NT_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*
goto execute

:4NT_args
@rem Get arguments from the 4NT Shell from JP Software
set CMD_LINE_ARGS=%$

:execute
@rem Setup the command line
set CLASSPATH=%NAFTAH_HOME%\lib\*

@rem Append JVM options to JAVA_OPTS
set JAVA_OPTS=%JAVA_OPTS% --add-modules=jdk.incubator.vector --add-modules=jdk.incubator.vector ^
	 --add-opens=java.base/java.lang=ALL-UNNAMED ^
	 --add-opens=java.base/java.lang.reflect=ALL-UNNAMED ^
	 --add-opens=java.base/java.util=ALL-UNNAMED ^
	 --add-opens=java.base/java.time=ALL-UNNAMED ^
	 --add-opens=java.base/java.io=ALL-UNNAMED ^
	 --add-opens=java.base/java.math=ALL-UNNAMED ^
	 --add-opens=java.base/sun.nio.ch=ALL-UNNAMED ^
	 --add-opens=java.base/sun.net.www.protocol.jar=ALL-UNNAMED ^
	 --add-opens=java.desktop/java.awt=ALL-UNNAMED ^
	 --add-opens=java.desktop/sun.awt=ALL-UNNAMED ^
	 --add-opens=java.xml/com.sun.org.apache.xerces.internal.parsers=ALL-UNNAMED

@if /I not "%DEBUG%" == "true" goto executeNoDebug

:executeDebug
:: Check if -d already exists in CMD_LINE_ARGS
echo %CMD_LINE_ARGS% | findstr /i "\-d" >nul

if errorlevel 1 (
    set CMD_LINE_ARGS=%CMD_LINE_ARGS% -d
)

"%JAVA_EXE%" %JAVA_OPTS% -cp "%CLASSPATH%" -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5006 -Dfile.encoding=UTF-8 org.daiitech.naftah.Naftah %CMD_LINE_ARGS%
goto end

:executeNoDebug
"%JAVA_EXE%" %JAVA_OPTS% -cp "%CLASSPATH%" -Dfile.encoding=UTF-8 org.daiitech.naftah.Naftah %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable NAFTAH_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%NAFTAH_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

