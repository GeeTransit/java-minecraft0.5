@setlocal enableDelayedExpansion
@if not defined DEBUG (@echo off)
@if defined DEBUG%~n0 (@echo !DEBUG%~n0!)

set "HELP=%~1"
if /i "%HELP%" == "-h" (goto :help)
if /i "%HELP%" == "--help" (goto :help)
if /i "%HELP%" == "-?" (goto :help)
goto :split

:help
@echo normalize.bat [-h / --help / -?] [--] options...
@echo print normalized options
@echo   [-h / --help / -?] = display this help
@echo   options... = options to be normalized
@echo a normalized option has this form
@echo   name:arguments
@echo where
@echo   name = expected argument
@echo   arguments = number of arguments to collect (* to collect rest)
exit /b 0

:split
if /i [%1] == [--] (shift /1)

:loop
	if "%~1" == "" goto loop.end
	for /f "tokens=1,2 delims=:" %%V in ("%~1") do (
		set "NAME=%%V"
		if "%%~W" == "" (set TAKE=0) else (set TAKE=%%~W)
		<nul set /p "_=!NAME!:!TAKE! "
	)
	shift /1
	goto loop
:loop.end
