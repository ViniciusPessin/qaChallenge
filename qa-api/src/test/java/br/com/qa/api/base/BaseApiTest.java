package br.com.qa.api.base;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;

/**
 * Classe base para todos os testes de API.
 * Configura a spec base do RestAssured com: URL base, headers, log e Allure filter.
 */
public abstract class BaseApiTest {

    protected static RequestSpecification requestSpec;

    protected static final String BASE_URL  = "https://dog.ceo";
    protected static final String BASE_PATH = "/api";

    @BeforeAll
    static void setUpRestAssured() {
        requestSpec = new RequestSpecBuilder()
            .setBaseUri(BASE_URL)
            .setBasePath(BASE_PATH)
            .setContentType(ContentType.JSON)
            .setAccept(ContentType.JSON)
            .addFilter(new AllureRestAssured())          // Captura req/resp no relatório Allure
            .log(LogDetail.ALL)                          // Log completo no console
            .build();

        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
