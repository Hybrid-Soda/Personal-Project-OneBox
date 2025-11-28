# 1) 베이스 이미지 지엉
FROM eclipse-temurin:17-jdk-jammy
# 2) 작업 디렉토리 지정
WORKDIR /
# 3) 빌드된 JAR 파일을 이미지 내부로 복사
COPY build/libs/oneBox-0.0.1-SNAPSHOT.jar app.jar
# Heap Dump
ENV JAVA_OPTS="-Xms512m -Xmx768m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/heapdumps -Xlog:gc*:file=/heapdumps/gc.log:time,uptime,level,tags"
# 4) 외부 노출 포트 지정
EXPOSE 8080
# 5) 컨테이너 실행 시 실행될 명령어
ENTRYPOINT ["java", "-jar", "/app.jar"]