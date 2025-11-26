REM ===== 1. Build =====
call gradlew.bat clean build -x test
IF %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Gradle build failed.
    exit /b 1
)


REM ===== 2. Build Docker Image =====
docker build -t devnovus/one-box:1.0 .
IF %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Docker image build failed.
    exit /b 1
)

REM ===== 3. Update compose image version =====
docker compose down
docker compose up -d

REM ===== 4. Done =====
echo Deploy complete.