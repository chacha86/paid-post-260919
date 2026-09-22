package com.rest1.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;

import java.util.Map;

// 테스트가 쓸 MySQL. 이 빈이 등록되면 Spring Boot 가
//  1) 도커 엔진에 mysql:8.4.6 컨테이너를 띄우고 (없으면 이미지를 내려받고)
//  2) @ServiceConnection 으로 그 컨테이너의 url/계정을 DataSource 에 꽂는다.
// 학생이 로컬 3306 에 MySQL 을 설치하거나 application-test.yml 에 접속 정보를 적을 필요가 없다.
// 스프링 테스트 컨텍스트가 하나면 컨테이너도 하나다. (컨텍스트가 다르면 컨테이너도 따로 뜬다)
@TestConfiguration(proxyBeanMethods = false)
public class TestMySqlConfig {

    @Bean
    @ServiceConnection
    MySQLContainer<?> mysqlContainer() {
        // 버전을 고정한다. latest 로 두면 락·데드락 동작이 달라져 본문의 실측값과 어긋날 수 있다.
        return new MySQLContainer<>("mysql:8.4.6")
                // 데이터 디렉터리를 디스크 대신 메모리에 둔다. 테스트라 남길 이유가 없고,
                // MySQL 이 처음 뜰 때 하는 초기화가 대부분 디스크 작업이라 기동이 17초에서 6초로 줄어든다.
                .withTmpFs(Map.of("/var/lib/mysql", "rw"));
    }
}
