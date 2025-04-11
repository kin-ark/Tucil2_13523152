@echo off
setlocal EnableDelayedExpansion

rem === Configuration ===
set SRC=src\com\kinan\imgcompressor
set BIN=bin
set LIB=lib
set MAIN_CLASS=com.kinan.imgcompressor.App
set JAR_NAME=QuadtreeImageCompressor.jar

rem === Clean output ===
if exist "%BIN%" (
    echo Cleaning bin...
    rmdir /s /q "%BIN%"
)
mkdir "%BIN%"

rem === Build quoted file list ===
echo Gathering .java files...
set "JAVA_FILES="

for /R "%SRC%" %%f in (*.java) do (
    set "JAVA_FILES=!JAVA_FILES! "%%f""
)

rem === Compile ===
echo Compiling Java files...
javac -d "%BIN%" -cp "%LIB%\*;%BIN%" !JAVA_FILES!

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed.
    exit /b %ERRORLEVEL%
)

rem === Build JAR ===
echo Creating JAR...
cd "%BIN%"
jar cfm "%JAR_NAME%" ..\manifest.txt -C . .
cd ..

echo Build complete: %JAR_NAME%
pause
endlocal