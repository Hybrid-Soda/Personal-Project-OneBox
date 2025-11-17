# 1) 베이스 이미지 지엉
FROM eclipse-temurin:17-jdk-jammy
# 2) 작업 디렉토리 지정
WORKDIR /
# 3) 빌드된 JAR 파일을 이미지 내부로 복사
COPY build/libs/oneBox-0.0.1-SNAPSHOT.jar app.jar
# 4) 외부 노출 포트 지정
EXPOSE 8080
# 5) 컨테이너 실행 시 실행될 명령어
ENTRYPOINT ["java", "-jar", "/app.jar"]

# ./gradlew clean build -x test
# docker build -t devnovus/one-box:1.0 .