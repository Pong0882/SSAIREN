package com.ssairen.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Slf4j
@Component
@Profile("dev")  // dev 프로파일에서만 실행
public class DataLoader {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void loadData() {
        try {
            // 데이터가 이미 존재하는지 확인
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM fire_states",
                Integer.class
            );

            if (count != null && count > 0) {
                log.info("Initial data already exists. Skipping data.sql execution.");
                return;
            }

            // 데이터가 없으면 초기 데이터 삽입
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("data.sql"));
            populator.setContinueOnError(false);
            populator.execute(dataSource);
            log.info("data.sql executed successfully!");
        } catch (Exception e) {
            log.error("Failed to execute data.sql: {}", e.getMessage());
            // 개발 환경이므로 에러 출력만 하고 계속 진행
        }
    }
}
