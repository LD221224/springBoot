# 계층화 전

# JDK를 기반으로 컨테이너를 생성한다.
FROM adoptopenjdk/openjdk11:latest

# 패턴 매칭을 사용해서 애플리케이션 JAR 파일을 이미지 빌드에 사용할 인자로 지정한다.
ARG JAR_FILE=target/*.jar

# JAR 파일을 복사해 컨테이너 내부에 app.jar로 이름을 바꿔 붙여넣는다.
COPY ${JAR_FILE} app.jar

# 컨테이너 안에서 java-jar/app.jar 명령을 수행하게 해서 JAR 파일을 실행한다.
ENTRYPOINT ["java","-jar","/app.jar"]