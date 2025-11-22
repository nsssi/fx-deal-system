@echo off
REM FX Deal System - Build Script for Windows

if "%1"=="" goto help
if "%1"=="help" goto help
if "%1"=="build" goto build
if "%1"=="test" goto test
if "%1"=="coverage" goto coverage
if "%1"=="up" goto up
if "%1"=="down" goto down
if "%1"=="clean" goto clean
if "%1"=="logs" goto logs
if "%1"=="restart" goto restart
if "%1"=="all" goto all
goto help

:help
echo FX Deal System - Available Commands
echo ====================================
echo build.bat build       - Build the application
echo build.bat test        - Run all tests
echo build.bat coverage    - Run tests with coverage
echo build.bat up          - Start all services
echo build.bat down        - Stop all services
echo build.bat clean       - Clean build and stop services
echo build.bat logs        - Show application logs
echo build.bat restart     - Restart all services
echo build.bat all         - Build, test, and start
goto end

:build
echo Building application...
call mvn clean package -DskipTests
goto end

:test
echo Running all tests...
call mvn clean test
goto end

:coverage
echo Running tests with coverage...
call mvn clean verify
echo Coverage report: target\site\jacoco\index.html
start target\site\jacoco\index.html
goto end

:up
echo Starting services...
docker-compose up -d
echo Waiting for services to be ready...
timeout /t 15 /nobreak >nul
echo Services are ready!
echo MySQL: localhost:3307
echo App: http://localhost:8080
goto end

:down
echo Stopping services...
docker-compose down
goto end

:clean
echo Cleaning...
call mvn clean
docker-compose down -v
echo Clean complete!
goto end

:logs
docker-compose logs -f app
goto end

:restart
call build.bat down
call build.bat up
goto end

:all
call build.bat clean
call build.bat build
call build.bat test
call build.bat up
echo All done! Application is running at http://localhost:8080
goto end

:end