@setlocal enableDelayedExpansion
@if not defined DEBUG (@echo off)
@if defined DEBUG%~n0 (@echo !DEBUG%~n0!)
if defined DEBUG%~n0OUTPUT (set "OUTPUT= ") else (set "OUTPUT=1>nul")
if defined DEBUG%~n0ERROR (set "ERROR= ") else (set "ERROR=2>nul")

:vars
set "OPTIONS=-h --help -? -r -c -i:1 -a:1"
set "REQVARS=ACTION INIFILE AREA"
set "REQKEYS=TEMPORARY LIBRARY COMPILE SOURCE"
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
@echo compile.bat [-h / --help / -?] [-r] [-c] -i inifile -a area
@echo compile .java files to .class files
@echo   -h / --help / -? = display this help
@echo   -r = remove class files
@echo   -c = create class files
@echo   -i inifile = ini file to read from
@echo   -a area = ini area to get options
exit /b 1
:-r
set "ACTION=%ACTION%remove " & goto :EOF
:-c
set "ACTION=%ACTION%create " & goto :EOF
:-i
set "INIFILE=%~1" & goto :EOF
:-a
set "AREA=%~1" & goto :EOF

:inifile
for /f "usebackq tokens=*" %%V in (`call "%~dp0iniget" "%INIFILE%" "%AREA%"`) do (call set "%%V")
for /f "usebackq tokens=*" %%N in (`call "%~dp0empty" %REQKEYS%`) do (echo missing %INIFILE%[%AREA%].%%N & exit /b 1)

:action
set FILES=%TEMPORARY%\files.txt
for %%A in (%ACTION%) do (call :%%A || (echo error:action:%%A:!ERRORLEVEL! & exit /b !ERRORLEVEL!))
goto :EOF

:create
@rem create temporary file with java files
@rem empty file # https://stackoverflow.com/a/1702790
%ERROR% mkdir %TEMPORARY%
>%FILES% <nul (set /p "_=")
for /f "usebackq tokens=*" %%F in (`dir /s /b %SOURCE%\*.java`) do (>>%FILES% echo/%%F)
@rem create variable with all folders inside %LIBRARY%
@rem idea # https://stackoverflow.com/a/10221436
set "FROM=%CD%"
if not defined INCLUDELIBRARY (
	set "INCLUDELIBRARY= "
	for /r %LIBRARY% %%L in (.) do (pushd %%L & set "HERE=!CD:*%FROM%\=!" & set "INCLUDELIBRARY=!INCLUDELIBRARY!!HERE!\;!HERE!\*;" & popd)
	set INCLUDELIBRARY="!INCLUDELIBRARY!")
%OUTPUT% echo/%INCLUDELIBRARY%
@rem compile the java files
%ERROR% mkdir %COMPILE%
javac -cp %INCLUDELIBRARY% -d %COMPILE% -sourcepath %SOURCE% @%FILES%
exit /b %ERRORLEVEL%

:remove
%ERROR% rmdir /s /q %COMPILE%
%ERROR% del /f %SOURCESLIST%
%ERROR% rmdir /q %TEMPORARY%
exit /b 0
