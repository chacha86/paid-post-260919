package com.rest1;

import com.rest1.support.TestMySqlConfig;
import org.springframework.boot.SpringApplication;

// 개발용 실행 진입점: 테스트와 같은 MySQL 컨테이너를 띄운 채로 서버를 켠다.
//   ./gradlew.bat bootTestRun
// bootRun(dev 프로필, H2 파일 DB)과 달리, 이 서버가 보는 DB 는 방금 뜬 빈 MySQL 컨테이너다.
// 서버를 끄면(Ctrl+C) 컨테이너도 사라지고 그 안의 데이터도 같이 사라진다.
public class TestRest1Application {

    public static void main(String[] args) {
        SpringApplication.from(Rest1Application::main)
                .with(TestMySqlConfig.class)      // MySQL 컨테이너 + @ServiceConnection
                .withAdditionalProfiles("test")   // dev 프로필의 H2 접속 정보를 쓰지 않도록
                .run(args);
    }
}
