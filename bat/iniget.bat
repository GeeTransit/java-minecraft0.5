@setlocal enableDelayedExpansion
@if not defined DEBUG (@echo off)
@if defined DEBUG%~n0 (@echo !DEBUG%~n0!)
rem script # https://stackoverflow.com/a/2866328

set "HELP=%~1"
if /i "%HELP%" == "-h" (goto :help)
if /i "%HELP%" == "--help" (goto :help)
if /i "%HELP%" == "-?" (goto :help)
goto :split

:help
echo iniget.bat [-h / --help / -?] [--] inifile area [key]
echo return [area].key
echo   [-h / --help / -?] = display this help
echo   inifile = inifile to read from
echo   area = area to find keys
echo   key = key to search for (print all keys if blank)
exit /b 0

:split
if /i [%1] == [--] (shift /1)

:vars
set "file=%~1"
set "area=[%~2]"
if [%3] == [] (set "key=") else (set "key=%~3")

:get
set "currarea="
for /f "usebackq delims=" %%a in ("!file!") do (
    set "ln=%%a"
    if "x!ln:~0,1!"=="x[" (
        set "currarea=!ln!"
    ) else (
		if /i "x!area!"=="x!currarea!" (
			for /f "tokens=1,* delims==" %%b in ("!ln!") do (
				set "currkey=%%b"
				set "currval=%%c"
				set match=0
				if /i "x!key!"=="x" (set match=1)
				if /i "x!key!"=="x!currkey!" (set match=1)
				if !match! EQU 1 (
					echo !currkey!=!currval!
				)
			)
		)
	)
)
