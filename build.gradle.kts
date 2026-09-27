plugins {
    java
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com"
version = "0.0.1-SNAPSHOT"
description = "rest1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

// Docker Engine 29 부터 옛 방식으로 붙는 Testcontainers(Boot 3.5.5 기본값 1.21.3)를 거절한다
// ("Could not find a valid Docker environment"). 1.21.4 가 이를 고친 버전이라 이것만 올린다.
extra["testcontainers.version"] = "1.21.4"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    testImplementation("org.springframework.security:spring-security-test")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    runtimeOnly("com.h2database:h2")                                   // dev 프로필(bootRun)은 그대로 H2
    runtimeOnly("com.mysql:mysql-connector-j")                          // 테스트가 붙을 MySQL 드라이버
    testImplementation("org.springframework.boot:spring-boot-testcontainers") // @ServiceConnection
    testImplementation("org.testcontainers:mysql")                      // MySQLContainer (버전은 Boot BOM이 관리, 위 extra 로 1.21.4)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")

    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
