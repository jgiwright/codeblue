@echo off
echo Creating output directory...
if not exist bin mkdir bin

echo Cleaning old files...
del /Q bin\*.class 2>nul

echo Compiling Java files...
javac -d bin src\*.java

if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Compilation successful!
echo Running CodeBlue...
java -cp bin CodeBlue
pause