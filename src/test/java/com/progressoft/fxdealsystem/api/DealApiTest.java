package com.progressoft.fxdealsystem.api;

import com.progressoft.fxdealsystem.dto.DealRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Deal API REST Assured Tests")
class DealApiTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/deals";   // 🔥 CORRECTION ESSENTIELLE
    }

    @Test
    @Order(1)
    @DisplayName("API Test 1: Should import a valid deal successfully")
    void testImportDeal_Success() {
        DealRequest request = new DealRequest(
                "API_DEAL_001",
                "USD",
                "EUR",
                LocalDateTime.of(2024, 1, 15, 10, 30),
                new BigDecimal("1000.50")
        );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post()
                .then()
                .statusCode(201)
                .body("dealUniqueId", equalTo("API_DEAL_001"))
                .body("status", equalTo("SUCCESS"))
                .body("message", equalTo("Deal imported successfully"))
                .body("id", notNullValue());
    }

    @Test
    @Order(2)
    void testImportDeal_Duplicate() {
        DealRequest request = new DealRequest(
                "API_DEAL_001",
                "USD",
                "EUR",
                LocalDateTime.now(),
                new BigDecimal("1000.00")
        );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post()
                .then()
                .statusCode(409)
                .body("error", equalTo("Duplicate Deal"))
                .body("message", containsString("already exists"));
    }

    @Test
    @Order(3)
    void testImportDeal_MissingDealId() {
        Map<String, Object> request = new HashMap<>();
        request.put("fromCurrencyIsoCode", "USD");
        request.put("toCurrencyIsoCode", "EUR");
        request.put("dealTimestamp", "2024-01-15T10:30:00");
        request.put("dealAmount", 1000.50);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post()
                .then()
                .statusCode(400)
                .body("error", equalTo("Validation Failed"))
                .body("messages.dealUniqueId", containsString("required"));
    }

    @Test
    @Order(4)
    void testImportDeal_InvalidCurrencyCode() {
        DealRequest request = new DealRequest(
                "API_DEAL_002",
                "US",
                "EUR",
                LocalDateTime.now(),
                new BigDecimal("1000.00")
        );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post()
                .then()
                .statusCode(400)
                .body("error", equalTo("Validation Failed"));
    }

    @Test
    @Order(5)
    void testImportDeal_NegativeAmount() {
        DealRequest request = new DealRequest(
                "API_DEAL_003",
                "USD",
                "EUR",
                LocalDateTime.now(),
                new BigDecimal("-100.00")
        );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post()
                .then()
                .statusCode(400)
                .body("error", equalTo("Validation Failed"))
                .body("messages.dealAmount", containsString("greater than 0"));
    }

    @Test
    @Order(6)
    void testImportDeal_SameCurrencies() {
        DealRequest request = new DealRequest(
                "API_DEAL_004",
                "USD",
                "USD",
                LocalDateTime.now(),
                new BigDecimal("1000.00")
        );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post()
                .then()
                .statusCode(400)
                .body("error", equalTo("Invalid Deal"))
                .body("message", containsString("must be different"));
    }

    @Test
    @Order(7)
    void testImportDeal_FutureTimestamp() {
        DealRequest request = new DealRequest(
                "API_DEAL_005",
                "USD",
                "EUR",
                LocalDateTime.now().plusDays(1),
                new BigDecimal("1000.00")
        );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post()
                .then()
                .statusCode(400)
                .body("error", equalTo("Validation Failed"))
                .body("messages.dealTimestamp", containsString("cannot be in the future"));
    }

    @Test
    @Order(8)
    void testImportDeals_BulkPartialSuccess() {
        DealRequest valid = new DealRequest(
                "API_BULK_001",
                "USD",
                "EUR",
                LocalDateTime.now(),
                new BigDecimal("1000.00")
        );

        DealRequest duplicate = new DealRequest(
                "API_DEAL_001",
                "GBP",
                "JPY",
                LocalDateTime.now(),
                new BigDecimal("2000.00")
        );

        given()
                .contentType(ContentType.JSON)
                .body(Arrays.asList(valid, duplicate))
                .when()
                .post("/bulk")
                .then()
                .statusCode(201)
                .body("size()", equalTo(2))
                .body("[0].status", equalTo("SUCCESS"))
                .body("[1].status", equalTo("FAILED"));
    }

    @Test
    @Order(9)
    void testGetAllDeals() {
        given()
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    @Order(10)
    void testGetDealByUniqueId() {
        given()
                .when()
                .get("/API_DEAL_001")
                .then()
                .statusCode(200)
                .body("dealUniqueId", equalTo("API_DEAL_001"))
                .body("fromCurrencyIsoCode", equalTo("USD"))
                .body("toCurrencyIsoCode", equalTo("EUR"));
    }

    @Test
    @Order(11)
    void testGetDealByUniqueId_NotFound() {
        given()
                .when()
                .get("/NONEXISTENT_DEAL")
                .then()
                .statusCode(400)
                .body("error", equalTo("Invalid Deal"))
                .body("message", containsString("Deal not found"));
    }

    @Test
    @Order(12)
    void testHealthEndpoint() {
        given()
                .when()
                .get("/health")
                .then()
                .statusCode(200)
                .body(containsString("running"));
    }

    @Test
    @Order(13)
    void testImportDeal_NonExistentCurrency() {
        DealRequest request = new DealRequest(
                "API_DEAL_INVALID",
                "XXX",
                "EUR",
                LocalDateTime.now(),
                new BigDecimal("1000.00")
        );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post()
                .then()
                .statusCode(400)
                .body("message", containsString("Invalid currency ISO code"));
    }

    @Test
    @Order(14)
    void testImportDeal_LowercaseCurrencyCodes() {
        DealRequest request = new DealRequest(
                "API_DEAL_LOWERCASE",
                "usd",
                "eur",
                LocalDateTime.now(),
                new BigDecimal("1000.00")
        );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post()
                .then()
                .statusCode(201)
                .body("status", equalTo("SUCCESS"));
    }

    @Test
    @Order(15)
    void testImportDeal_ZeroAmount() {
        DealRequest request = new DealRequest(
                "API_DEAL_ZERO",
                "USD",
                "EUR",
                LocalDateTime.now(),
                BigDecimal.ZERO
        );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post()
                .then()
                .statusCode(400)
                .body("messages.dealAmount", containsString("greater than 0"));
    }
}
