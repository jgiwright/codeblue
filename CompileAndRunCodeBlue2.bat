@echo off
echo Compiling Java files...
javac CodeBlue2.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Running CodeBlue2...
java CodeBlue2
pause