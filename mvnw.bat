@echo off
REM ==============================================================================
REM Verif Application Maven Wrapper Helper (Windows)
REM Automatically locates local JDK 17 and Maven
REM ==============================================================================

setlocal

if exist "C:\Users\DELL\tools\jdk-17.0.12+7\bin\java.exe" (
    set "JAVA_HOME=C:\Users\DELL\tools\jdk-17.0.12+7"
    set "PATH=C:\Users\DELL\tools\jdk-17.0.12+7\bin;C:\Users\DELL\tools\apache-maven-3.9.6\bin;%PATH%"
)

call mvn %*
endlocal
