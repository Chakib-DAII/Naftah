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
echo خطأ: لم يتم تعيين JAVA_HOME ولا يمكن العثور على أمر 'java' في المسار PATH الخاص بك.
echo.
echo يرجى تعيين متغير JAVA_HOME في بيئتك ليشير إلى
echo موقع تثبيت Java لديك.
goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe
if exist "%JAVA_EXE%" goto runNaftah
echo.
echo خطأ: JAVA_HOME مضبوط على مسار غير صالح: %JAVA_HOME%
echo.
echo يرجى تعيين متغير JAVA_HOME في بيئتك ليشير إلى
echo موقع تثبيت Java الصحيح.
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

@rem Load all .vmoptions files in NAFTAH_HOME

@rem Enable delayed expansion
setlocal enabledelayedexpansion

set VM_OPTS=

for %%F in ("%NAFTAH_HOME%\*.vmoptions") do (
    if exist "%%F" (
        for /f "usebackq tokens=* delims=" %%L in ("%%F") do (

            rem Skip full-line comments
            echo %%L | findstr /b "#" >nul
            if errorlevel 1 (

                rem Strip inline comments: everything after first #
                set "LINE=%%L"
                for /f "tokens=1 delims=#" %%C in ("!LINE!") do (
                    set "CLEAN=%%C"
                )

                rem Trim clean string
                for /f "tokens=* delims= " %%T in ("!CLEAN!") do set "CLEAN=%%T"
                for /l %%I in (1,1,100) do if "!CLEAN:~-1!"==" " set "CLEAN=!CLEAN:~0,-1!"

                rem Append cleaned option (skip empty lines)
                if not "!CLEAN!"=="" (
                    set "VM_OPTS=!VM_OPTS! !CLEAN!"
                )
            )
        )
    )
)

@rem Append JVM options to JAVA_OPTS
set JAVA_OPTS=%JAVA_OPTS% !VM_OPTS! --add-modules=jdk.incubator.vector ^
--add-opens=java.base/java.lang=ALL-UNNAMED ^
--add-opens=java.base/java.lang.invoke=ALL-UNNAMED ^
--add-opens=java.base/java.lang.reflect=ALL-UNNAMED ^
--add-opens=java.base/sun.invoke.util=ALL-UNNAMED ^
--add-opens=java.base/java.util=ALL-UNNAMED ^
--add-opens=java.base/java.util.concurrent=ALL-UNNAMED ^
--add-opens=java.base/java.util.stream=ALL-UNNAMED ^
--add-opens=java.base/java.time=ALL-UNNAMED ^
--add-opens=java.base/java.io=ALL-UNNAMED ^
--add-opens=java.base/java.net=ALL-UNNAMED ^
--add-opens=java.base/sun.net=ALL-UNNAMED ^
--add-opens=java.base/sun.net.www.protocol.jar=ALL-UNNAMED ^
--add-opens=java.base/sun.net.www.protocol.file=ALL-UNNAMED ^
--add-opens=java.base/java.util.zip=ALL-UNNAMED ^
--add-opens=java.base/java.util.jar=ALL-UNNAMED ^
--add-opens=java.base/java.security=ALL-UNNAMED ^
--add-opens=java.base/sun.security.util=ALL-UNNAMED ^
--add-opens=java.base/sun.security.x509=ALL-UNNAMED ^
--add-opens=java.base/java.nio=ALL-UNNAMED ^
--add-opens=java.base/java.nio.channels=ALL-UNNAMED ^
--add-opens=java.base/java.nio.channels.spi=ALL-UNNAMED ^
--add-opens=java.base/java.nio.charset=ALL-UNNAMED ^
--add-opens=java.base/java.nio.file=ALL-UNNAMED ^
--add-opens=java.base/java.nio.file.spi=ALL-UNNAMED ^
--add-opens=java.base/java.nio.file.attribute=ALL-UNNAMED ^
--add-opens=java.base/sun.nio=ALL-UNNAMED ^
--add-opens=java.base/sun.nio.ch=ALL-UNNAMED ^
--add-opens=java.base/sun.nio.fs=ALL-UNNAMED ^
--add-opens=java.base/sun.nio.cs=ALL-UNNAMED ^
--add-opens=java.base/sun.reflect.annotation=ALL-UNNAMED ^
--add-opens=java.base/sun.reflect.misc=ALL-UNNAMED ^
--add-opens=java.base/java.lang.ref=ALL-UNNAMED ^
--add-opens=java.base/jdk.internal.ref=ALL-UNNAMED ^
--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED ^
--add-opens=java.base/jdk.internal.vm=ALL-UNNAMED ^
--add-opens=java.base/jdk.internal.logger=ALL-UNNAMED ^
--add-opens=java.base/jdk.internal.event=ALL-UNNAMED ^
--add-opens=java.base/jdk.internal.loader=ALL-UNNAMED ^
--add-opens=java.base/jdk.internal.module=ALL-UNNAMED ^
--add-opens=java.xml/com.sun.org.apache.xerces.internal.parsers=ALL-UNNAMED ^
--add-opens=java.xml/com.sun.org.apache.xerces.internal.dom=ALL-UNNAMED ^
--add-opens=java.xml/com.sun.org.apache.xerces.internal.jaxp=ALL-UNNAMED ^
--add-opens=java.xml/com.sun.org.apache.xalan.internal.xsltc.trax=ALL-UNNAMED ^
--add-opens=java.desktop/java.awt=ALL-UNNAMED ^
--add-opens=java.desktop/sun.awt=ALL-UNNAMED ^
--add-opens=java.desktop/javax.swing=ALL-UNNAMED ^
--add-opens=java.desktop/javax.swing.plaf.basic=ALL-UNNAMED ^
--add-opens=java.desktop/sun.java2d=ALL-UNNAMED

endlocal & set "JAVA_OPTS=%JAVA_OPTS%"

@if /I not "%DEBUG%" == "true" goto executeNoDebug

:executeDebug
:: Check if -d already exists in CMD_LINE_ARGS
echo %CMD_LINE_ARGS% | findstr /i "\-d" >nul

if errorlevel 1 (
    set CMD_LINE_ARGS=%CMD_LINE_ARGS% -d
)

"%JAVA_EXE%" %JAVA_OPTS% -cp "%CLASSPATH%" -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5006 -Dfile.encoding=UTF-8 org.daiitech.naftah.Naftah %CMD_LINE_ARGS%
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

