@echo off
REM ==============================================================================
REM Verif Profile Verification System - Quick Launch Script
REM ==============================================================================
echo ==============================================================================
echo  Launching Verif Profile Verification System...
echo ==============================================================================

set "JAVA_HOME=C:\Users\DELL\tools\jdk-17.0.12+7"
set "PATH=C:\Users\DELL\tools\jdk-17.0.12+7\bin;C:\Users\DELL\tools\apache-maven-3.9.6\bin;%PATH%"

if "%1"=="--mysql" (
    echo Starting with dedicated MySQL profile...
    mvn spring-boot:run -Dspring-boot.run.profiles=mysql
) else (
    echo Starting with default profile: Embedded H2 / MySQL auto-compatible...
    mvn spring-boot:run
)
