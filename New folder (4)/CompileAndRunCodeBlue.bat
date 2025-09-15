@echo off
echo Compiling Java files...
javac *.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Running CodeBlue...
java Simple3DGame
pause