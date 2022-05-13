# 계층화 적용 Dockerfile

# 빌더로 사용할 컨테이너를 생성한다.
FROM adoptopenjdk/openjdk11:latest as builder
WORKDIR application
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
# extract 명령으로 레이어를 추출한다.
RUN java -Djarmode=layertools -jar application.jar extract

# 두 번째 컨테이너를 만든다.
FROM adoptopenjdk/openjdk11:latest
WORKDIR application
# 빌더 컨테이너에서 추출한 여러 레이어를 두 번째 컨테이너에 복사한다.
# COPY 명령에 도커의 계층 캐시 알고리즘이 적용된다.
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
#COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
# 스프링 부트의 커스텀 런처로 애플리케이션을 실행한다.
# 이 런처는 애플리케이션 시작 시 불필요한 JAR 파일 압축 해제를 하지 않아 효율적이다.
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]