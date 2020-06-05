@setlocal enableDelayedExpansion
@if not defined DEBUG (@echo off)
@if defined DEBUG%~n0 (@echo !DEBUG%~n0!)
if defined DEBUG%~n0OUTPUT (set "OUTPUT=") else (set "OUTPUT=1>nul")
if defined DEBUG%~n0ERROR (set "ERROR=") else (set "ERROR=2>nul")

:vars
set "OPTIONS=-h --help -? -n:1 -i:1 -a:1"
set "REQVARS=INIFILE AREA"
set "REQKEYS=PACKAGE"

:options
for /f "usebackq tokens=*" %%O in (`call "%~dp0normalize" -- %OPTIONS%`) do (set "OPTIONS=%%O")
>nul call "%~dp0parse" -e "%OPTIONS%" %* || (echo argument:error & exit /b %ERRORLEVEL%)
for /f "usebackq tokens=*" %%L in (`call "%~dp0parse" -e "%OPTIONS%" %*`) do (call :%%L || exit /b !ERRORLEVEL!)
for /f "usebackq tokens=*" %%N in (`call "%~dp0empty" -- %REQVARS%`) do (echo missing variable %%N & exit /b 1)
goto :inifile

:-h
:--help
:-?
@echo launch.bat [-h / --help / -?] [-n name] -i inifile -a area
@echo launch the app
@echo   -h / --help / -? = display this help
@echo   -n name = jar full file name
@echo     created relative to package option
@echo     [default=^%area^%]
@echo   -i inifile = ini file to read from
@echo   -a area = ini area to get options
exit /b 1
:-n
set "NAME=%~1" & goto :EOF
:-i
set "INIFILE=%~1" & goto :EOF
:-a
set "AREA=%~1" & goto :EOF

:inifile
for /f "usebackq tokens=*" %%V in (`call "%~dp0iniget" "%INIFILE%" "%AREA%"`) do (call set "%%V")
for /f "usebackq tokens=*" %%N in (`call "%~dp0empty" %REQKEYS%`) do (echo missing %INIFILE%[%AREA%].%%N & exit /b 1)

:launch
if not defined NAME (set NAME=%AREA%)
set JARFILE=%PACKAGE%\%NAME%.jar
java -jar %JARFILE%
exit /b %ERRORLEVEL%
