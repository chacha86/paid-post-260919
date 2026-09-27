package com.rest1.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;

import java.util.Map;

// 동시성 실습 전용 MySQL. 일반 테스트용(TestMySqlConfig)과 두 가지가 다르다.
//
//  1) 라벨을 붙인다.
//     재사용은 "컨테이너 설정이 같으면 같은 컨테이너" 로 짝을 찾는다. 설정이 똑같으면
//     일반 테스트와 동시성 테스트가 한 컨테이너를 나눠 쓰게 되고, 여기서 커밋한 데이터가
//     다른 테스트로 새어 나간다(실제로 지갑 잔액을 기대하는 테스트 3개가 깨진다).
//     라벨 한 줄이 "이건 다른 컨테이너다" 를 만든다.
//
//  2) withReuse: 테스트가 끝나도 컨테이너를 지우지 않는다.
//     기본값은 꺼져 있다. 켜려면 홈 폴더에 ~/.testcontainers.properties 를 만들고
//     testcontainers.reuse.enable=true 한 줄을 넣는다. 안 켜면 지금까지와 똑같이 동작한다.
//     켜면 두 번째 실행부터 기동 시간이 사라진다. 다 쓰고 나면 docker rm -f 로 지운다.
@TestConfiguration(proxyBeanMethods = false)
public class RaceMySqlConfig {

    @Bean
    @ServiceConnection
    MySQLContainer<?> raceMysqlContainer() {
        return new MySQLContainer<>("mysql:8.4.6")
                .withTmpFs(Map.of("/var/lib/mysql", "rw"))
                .withLabel("purpose", "race")
                .withReuse(true);
    }
}
