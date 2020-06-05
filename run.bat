@setlocal enableDelayedExpansion
@if not defined DEBUG (@echo off)
@if defined DEBUG%~n0 (@echo !DEBUG%~n0!)

cd %~dp0
echo cd:%CD%
set INIFILE=config.ini
set AREA=project
set SCRIPTS=bat

set ARGS=-i %INIFILE% -a %AREA%

@rem set error level https://superuser.com/a/649329
echo creating
call %SCRIPTS%\compile %ARGS% -r -c & set LAST=!ERRORLEVEL!
echo create:%LAST%
if not %LAST% == 0 (pause & exit /b %LAST%)

echo packaging
call %SCRIPTS%\package %ARGS% -c & set LAST=!ERRORLEVEL!
echo package:%LAST%
if not %LAST% == 0 (pause & exit /b %LAST%)

echo launching
call %SCRIPTS%\launch %ARGS% & set LAST=!ERRORLEVEL!
echo launch:%LAST%
if not %LAST% == 0 (pause & exit /b %LAST%)

echo finished
pause
