@setlocal enableDelayedExpansion
@if not defined DEBUG (@echo off)
@if defined DEBUG%~n0 (@echo !DEBUG%~n0!)
if defined DEBUG%~n0OUTPUT (set "OUTPUT=") else (set "OUTPUT=1>nul")
if defined DEBUG%~n0ERROR (set "ERROR=") else (set "ERROR=2>nul")

:vars
set "OPTIONS=-h --help -? -r -c -n:1 -i:1 -a:1"
set "REQVARS=ACTION INIFILE AREA"
set "REQKEYS=TEMPORARY LIBRARY RESOURCE PACKAGE COMPILE MAIN"
set "ACTION="

:options
for /f "usebackq tokens=*" %%O in (`call "%~dp0normalize" -- %OPTIONS%`) do (set "OPTIONS=%%O")
>nul call "%~dp0parse" -e "%OPTIONS%" %* || (echo argument:error & exit /b %ERRORLEVEL%)
for /f "usebackq tokens=*" %%L in (`call "%~dp0parse" -e "%OPTIONS%" %*`) do (call :%%L || exit /b !ERRORLEVEL!)
for /f "usebackq tokens=*" %%N in (`call "%~dp0empty" -- %REQVARS%`) do (echo missing variable %%N & exit /b 1)
goto :inifile

:-h
:--help
:-?
@echo package.bat [-h / --help / -?] [-r] [-c] [-n name] -i inifile -a area
@echo package .class files to a .jar file (library included)
@echo   -h / --help / -? = display this help
@echo   -r = remove jar file
@echo   -c = create jar file
@echo   -n name = jar file name (no .jar extension)
@echo     created relative to package option
@echo     [default=^%area^%]
@echo   -i inifile = ini file to read from
@echo   -a area = ini area to get options
exit /b 1
:-r
set "ACTION=%ACTION%remove " & goto :EOF
:-c
set "ACTION=%ACTION%create " & goto :EOF
:-n
set "NAME=%~1" & goto :EOF
:-i
set "INIFILE=%~1" & goto :EOF
:-a
set "AREA=%~1" & goto :EOF

:inifile
for /f "usebackq tokens=*" %%V in (`call "%~dp0iniget" "%INIFILE%" "%AREA%"`) do (call set "%%V")
for /f "usebackq tokens=*" %%N in (`call "%~dp0empty" %REQKEYS%`) do (echo missing %INIFILE%[%AREA%].%%N & exit /b 1)

:action
if not defined NAME (set NAME=%AREA%)
set BATFILE=%PACKAGE%\%NAME%.bat
set JARFILE=%PACKAGE%\%NAME%.jar
set MANIFEST=%TEMPORARY%\manifest.txt
for %%A in (%ACTION%) do (call :%%A || (echo error:action:%%A:!ERRORLEVEL! & exit /b !ERRORLEVEL!))
goto :EOF

:create
@rem create temporary folder and manifest file
%ERROR% mkdir %TEMPORARY%
>%MANIFEST% <nul (set /p "_=")
>>%MANIFEST% echo Main-Class: %MAIN%
@rem package the class files
%ERROR% mkdir %PACKAGE%
set ARGS=
if exist %LIBRARY% (set ARGS=%ARGS%%LIBRARY% )
if exist %RESOURCE% (set ARGS=%ARGS%%RESOURCE% )
%OUTPUT% jar cvfm %JARFILE% %MANIFEST% %ARGS% -C %COMPILE% . || exit /b !ERRORLEVEL!
@rem create batch running file
>%BATFILE% <nul (set /p "_=")
>>%BATFILE% echo java -jar %NAME%.jar
exit /b 0

:remove
%ERROR% del /f %JARFILE%
%ERROR% rmdir /q %PACKAGE%
%ERROR% del /f %MANIFEST%
%ERROR% rmdir /q %TEMPORARY%
exit /b 0
