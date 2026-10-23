@echo off
rem Compiles the game into .\out and starts it. Extra arguments go to the game,
rem e.g.  run.bat --seed 2002   or   run.bat --echo < test-runs\stage1-victory.txt
setlocal enabledelayedexpansion
cd /d "%~dp0"
if exist out rmdir /s /q out
rem Quote each path (the folder may contain spaces) and use forward slashes for javac.
(for /r src %%f in (*.java) do (
    set "p=%%f"
    echo "!p:\=/!"
)) > sources.txt
javac -d out @sources.txt
set "status=%errorlevel%"
del sources.txt
if not "%status%"=="0" exit /b %status%
java -cp out restaurant.app.Main %*
