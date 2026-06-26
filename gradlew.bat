@echo off
setlocal
set DIR=%~dp0
set JAVA_EXE=%JAVA_HOME%\bin\java.exe
if defined JAVA_HOME if exist "%JAVA_EXE%" goto foundJavaHome
where java >nul 2>nul
if %ERRORLEVEL% equ 0 (
  set JAVA_EXE=java
  goto foundJava
)
echo Could not find Java executable. Verify JAVA_HOME is set or java is on PATH.
endlocal
exit /b 1

:foundJavaHome
if not exist "%JAVA_EXE%" (
  echo JAVA_HOME is set to an invalid directory: %JAVA_HOME%
  endlocal
  exit /b 1
)

:foundJava
"%JAVA_EXE%" -jar "%DIR%gradle\wrapper\gradle-wrapper.jar" %*
endlocal
