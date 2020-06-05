@setlocal enableDelayedExpansion
@if not defined DEBUG (@echo off)
@if defined DEBUG%~n0 (@echo !DEBUG%~n0!)
if defined DEBUG%~n0OUTPUT (set "OUTPUT=") else (set "OUTPUT=1>nul")
if defined DEBUG%~n0ERROR (set "ERROR=") else (set "ERROR=2>nul")
set CODEPOS=1
set CODEMORE=2
@rem sources
@rem   default args # https://stackoverflow.com/a/8162578
@rem   for /f # https://ss64.com/nt/for_f.html
@rem   shifting # https://ss64.com/nt/shift.html
@rem   strings # https://en.wikibooks.org/wiki/Windows_Batch_Scripting#String_processing
@rem   input # https://stackoverflow.com/a/14574661

set "HELP=%~1"
if /i "%HELP%" == "-h"     (goto :help)
if /i "%HELP%" == "--help" (goto :help)
if /i "%HELP%" == "-?"     (goto :help)
goto :state

:help
@echo parse.bat [-h / --help / -?] [-p / -a name / -s name / -e] [--] normalized args...
@echo print collected arguments
@echo   [-h / --help / -?] = display this help
@echo   action for positional arguments [-p / -a name / / -s name / -e]
@echo     -p = print to stderr ^&2
@echo     -a name = store in `name`
@echo     -s name = same as `-s name`
@echo       only after `--` argument
@echo       exit with error 1 if before `--`
@echo     -e = exit with error 1
@echo   normalized = format for arguments
@echo   args... = arguments to be parsed
@echo each matching argument is collected with their arguments
@echo   -x:0 = -x
@echo   -x:3 = -x 1 2 3
@echo   -x:* = -x 1 2 3 ...
@echo a special `--` argument splits named arguments with positional arguments
@echo   with `"-x:1" -a POS`, `-x 1 -- -x 1 2 3 4 5`
@echo   only `-x 1` is printed, `-x 1 2 3` is not printed
@echo   instead, `echo ^%POS^%` would print `-x 1 2 3 4 5`
@echo debug
@echo   if DEBUGPARSEERROR is defined, debug info will be printed to stderr ^&2
exit /b 0

:state
set "FIRST=%~1"
set "POS="
set "STATE=p"
if /i "%FIRST%" == "-p" (set "STATE=p" & shift /1)
if /i "%FIRST%" == "-a" (set "STATE=a" & shift /1 & shift /1 & set "SAVE=%~2")
if /i "%FIRST%" == "-s" (set "STATE=s" & shift /1 & shift /1 & set "SAVE=%~2")
if /i "%FIRST%" == "-e" (set "STATE=e" & shift /1)

:split
if /i [%1] == [--] (shift /1)

:normalized
set "NORMALIZED=%~1"
%ERROR% >&2 echo normalized=%NORMALIZED%
shift /1

:args
	if [%1] == []   (goto :args.end)
	if %STATE% == s (if [%1] == [--] (goto :pos))
	if [%1] == [--] (shift /1 & goto :pos)

	:find
	set "CHECK=!NORMALIZED:*%~1:=!"
	if not "%CHECK%" == "%NORMALIZED%" (goto :parse)
	
	:error
	call :error.%STATE% %1 || exit /b !ERRORLEVEL!
	shift /1
	goto :args
	
	:error.p
		%ERROR% >&2 echo %~nx0:error:ignore:pos:%1
		goto :EOF
	:error.a
		%ERROR% >&2 echo pos:%1
		set "POS=!POS!%1 "
		goto :EOF
	:error.s
	:error.e
		>&2 echo %~nx0:error:exit:pos:%1
		exit /b %CODEPOS%

	:parse
		for /f "usebackq tokens=1" %%N in ('%CHECK%') do (set "NUM=%%N")
		%ERROR% >&2 echo get:%NUM%
		if not %NUM% == * (set /a "NUM+=1")
		set "ARGS="
		:collect
			rem check argument
			if [%1] == [] (if %NUM% == * (goto :collect.end))
			if [%1] == [] (>&2 echo %~nx0:error:exit:more:%NUM% & exit /b %CODEMORE%)
			rem add argument
			set "ARGS=!ARGS!%1 "
			%ERROR% >&2 echo num:%NUM%
			%ERROR% >&2 echo add:%1
			rem next argument
			shift /1
			if %NUM% == * (goto :collect)
			set /a "NUM-=1"
			if %NUM% LEQ 0 (goto :collect.end)
			goto :collect
		:collect.end
		%ERROR% >&2 echo end:!ARGS!
		echo:!ARGS!
		goto args
	
	:pos
	if [%1] == [] (goto :args.end)
	call :error.%STATE% %1 || exit /b !ERRORLEVEL!
	shift /1
	goto :pos
:args.end

%ERROR% >&2 echo pos:%POS%
if %STATE% == a (endlocal & set %SAVE%=%POS%)
