package com.progressoft.fxdealsystem.repository;

import com.progressoft.fxdealsystem.model.Deal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class DealRepositoryIntegrationTest {

    @Container
    static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("fxdb")
                    .withUsername("root")
                    .withPassword("root");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update"); // 👉 IMPORTANT
    }

    @Autowired
    private DealRepository dealRepository;

    @Test
    @DisplayName("Should persist and retrieve a Deal successfully with Testcontainers MySQL")
    void testSaveAndFind() {

        Deal deal = new Deal();
        deal.setDealUniqueId("MYSQL_INT_001");
        deal.setFromCurrencyIsoCode("USD");
        deal.setToCurrencyIsoCode("EUR");
        deal.setDealTimestamp(LocalDateTime.now());
        deal.setDealAmount(new BigDecimal("1500.25"));

        Deal saved = dealRepository.save(deal);
        assertThat(saved.getId()).isNotNull();

        var found = dealRepository.findByDealUniqueId("MYSQL_INT_001");

        assertThat(found.get().getDealAmount())
                .isEqualByComparingTo("1500.25");

        ;
    }
}
