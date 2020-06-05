@setlocal enableDelayedExpansion
@if not defined DEBUG (@echo off)
@if defined DEBUG%~n0 (@echo !DEBUG%~n0!)

set "HELP=%~1"
if /i "%HELP%" == "-h" (goto :help)
if /i "%HELP%" == "--help" (goto :help)
if /i "%HELP%" == "-?" (goto :help)
goto :split

:help
@echo empty.bat [-h / --help / -?] [--] names...
@echo print empty / undefined variables
@echo exits with the number of undefined variables
@echo   [-h / --help / -?] = display this help
@echo   names... = variable names to check
exit /b 0

:split
if /i [%1] == [--] (shift /1)

:check
set _NUM_FOUND=0
:LOOP
	if "%~1" == "" (goto :LOOP.end)
	if not defined %~1 (set /a _NUM_FOUND+=1 & echo/%~1)
	shift /1
	goto :LOOP
:LOOP.end
exit /b %_NUM_FOUND%
