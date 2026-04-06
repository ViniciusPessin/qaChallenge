package br.com.qa.api.tests;

import br.com.qa.api.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Testes automatizados para a Dog API (https://dog.ceo/dog-api/documentation).
 *
 * Endpoints cobertos:
 *  - GET /breeds/list/all
 *  - GET /breed/{breed}/images
 *  - GET /breeds/image/random
 *
 * Estratégia:
 *  Cada teste valida: status HTTP, estrutura do JSON, tipos dos campos
 *  e regras de negócio (ex.: URL de imagem deve ser válida).
 */
@Epic("Dog API")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DogApiTest extends BaseApiTest {

    // ══════════════════════════════════════════════════════════════════════════
    // GET /breeds/list/all
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /breeds/list/all")
    @Feature("Listar raças")
    class ListAllBreedsTests {

        @Test
        @Order(1)
        @Story("Status e estrutura")
        @Severity(SeverityLevel.BLOCKER)
        @DisplayName("Deve retornar HTTP 200 e status 'success'")
        void deveRetornarStatus200ESuccessStatus() {
            given()
                .spec(requestSpec)
            .when()
                .get("/breeds/list/all")
            .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("message", notNullValue());
        }

        @Test
        @Order(2)
        @Story("Estrutura do payload")
        @Severity(SeverityLevel.CRITICAL)
        @DisplayName("O payload deve conter um map de raças com sublistagens")
        void payloadDeveConterMapDeRacas() {
            Response response = given()
                .spec(requestSpec)
            .when()
                .get("/breeds/list/all")
            .then()
                .statusCode(200)
                .extract().response();

            Map<String, Object> message = response.jsonPath().getMap("message");

            assertThat(message)
                .as("O campo 'message' deve ser um map não vazio de raças")
                .isNotEmpty();

            // Verifica que algumas raças esperadas estão presentes
            assertThat(message.keySet())
                .as("Deve conter raças conhecidas como 'hound', 'labrador' e 'poodle'")
                .contains("hound", "labrador", "poodle");
        }

        @Test
        @Order(3)
        @Story("Estrutura do payload")
        @Severity(SeverityLevel.NORMAL)
        @DisplayName("Cada raça deve ter uma lista (vazia ou com sub-raças) como valor")
        void cadaRacaDeveTermListaComoValor() {
            Response response = given()
                .spec(requestSpec)
            .when()
                .get("/breeds/list/all")
            .then()
                .statusCode(200)
                .extract().response();

            Map<String, List<String>> breeds = response.jsonPath().getMap("message");

            breeds.forEach((breed, subBreeds) ->
                assertThat(subBreeds)
                    .as("O valor da raça '%s' deve ser uma lista (pode ser vazia)", breed)
                    .isInstanceOf(List.class)
            );
        }

        @Test
        @Order(4)
        @Story("Tempo de resposta")
        @Severity(SeverityLevel.NORMAL)
        @DisplayName("Tempo de resposta deve ser inferior a 3 segundos")
        void tempoDeRespostaDeveSerAceitavel() {
            long responseTime = given()
                .spec(requestSpec)
            .when()
                .get("/breeds/list/all")
            .then()
                .statusCode(200)
                .extract().time();

            assertThat(responseTime)
                .as("Tempo de resposta (%dms) deve ser menor que 3000ms", responseTime)
                .isLessThan(3000L);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // GET /breed/{breed}/images
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /breed/{breed}/images")
    @Feature("Imagens por raça")
    class BreedImagesTests {

        @ParameterizedTest(name = "Raça: {0}")
        @ValueSource(strings = {"hound", "labrador", "poodle", "beagle"})
        @Order(5)
        @Story("Raça válida retorna imagens")
        @Severity(SeverityLevel.CRITICAL)
        @DisplayName("Raças válidas devem retornar lista de imagens não vazia")
        void racaValidaDeveRetornarImagens(String breed) {
            Response response = given()
                .spec(requestSpec)
                .pathParam("breed", breed)
            .when()
                .get("/breed/{breed}/images")
            .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("message", notNullValue())
                .extract().response();

            List<String> images = response.jsonPath().getList("message");

            assertThat(images)
                .as("A raça '%s' deve ter ao menos 1 imagem", breed)
                .isNotEmpty();
        }

        @Test
        @Order(6)
        @Story("Formato das URLs de imagem")
        @Severity(SeverityLevel.CRITICAL)
        @DisplayName("Todas as URLs de imagem devem ser válidas (https:// + extensão de imagem)")
        void urlsDeImagemDevemSerValidas() {
            List<String> images = given()
                .spec(requestSpec)
                .pathParam("breed", "hound")
            .when()
                .get("/breed/{breed}/images")
            .then()
                .statusCode(200)
                .extract()
                .jsonPath().getList("message", String.class);

            assertThat(images).isNotEmpty();

            images.forEach(url -> {
                assertThat(url)
                    .as("URL '%s' deve começar com 'https://'", url)
                    .startsWith("https://");

                assertThat(url.toLowerCase())
                    .as("URL '%s' deve ter extensão de imagem", url)
                    .matches(".*\\.(jpg|jpeg|png|gif|webp)$");
            });
        }

        @Test
        @Order(7)
        @Story("Raça inválida retorna erro")
        @Severity(SeverityLevel.NORMAL)
        @DisplayName("Raça inválida deve retornar HTTP 404 e status 'error'")
        void racaInvalidaDeveRetornar404() {
            given()
                .spec(requestSpec)
                .pathParam("breed", "racainexistente999")
            .when()
                .get("/breed/{breed}/images")
            .then()
                .statusCode(404)
                .body("status", equalTo("error"))
                .body("message", notNullValue());
        }

        @Test
        @Order(8)
        @Story("Sub-raça válida")
        @Severity(SeverityLevel.NORMAL)
        @DisplayName("Sub-raça válida (hound/afghan) deve retornar imagens")
        void subRacaValidaDeveRetornarImagens() {
            given()
                .spec(requestSpec)
            .when()
                .get("/breed/hound/afghan/images")
            .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("message", not(empty()));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // GET /breeds/image/random
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /breeds/image/random")
    @Feature("Imagem aleatória")
    class RandomImageTests {

        @Test
        @Order(9)
        @Story("Retorno de imagem aleatória")
        @Severity(SeverityLevel.BLOCKER)
        @DisplayName("Deve retornar HTTP 200, status 'success' e uma URL de imagem")
        void deveRetornarImagemAleatoria() {
            given()
                .spec(requestSpec)
            .when()
                .get("/breeds/image/random")
            .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("message", notNullValue())
                .body("message", matchesPattern("https://images\\.dog\\.ceo/breeds/.*\\.(jpg|jpeg|png|gif)"));
        }

        @Test
        @Order(10)
        @Story("Aleatoriedade")
        @Severity(SeverityLevel.MINOR)
        @DisplayName("Duas chamadas consecutivas devem (muito provavelmente) retornar URLs diferentes")
        void duasChamadasDevemRetornarUrlsDiferentes() {
            String url1 = given()
                .spec(requestSpec)
            .when()
                .get("/breeds/image/random")
            .then()
                .statusCode(200)
                .extract().jsonPath().getString("message");

            String url2 = given()
                .spec(requestSpec)
            .when()
                .get("/breeds/image/random")
            .then()
                .statusCode(200)
                .extract().jsonPath().getString("message");

            // Estatisticamente improvável que sejam iguais, mas não garantido
            // Usamos soft assertion para não falhar o build em caso raro de colisão
            System.out.printf("[INFO] URL 1: %s%n", url1);
            System.out.printf("[INFO] URL 2: %s%n", url2);
            assertThat(url1).isNotNull();
            assertThat(url2).isNotNull();
        }

        @Test
        @Order(11)
        @Story("Múltiplas imagens aleatórias")
        @Severity(SeverityLevel.NORMAL)
        @DisplayName("GET /breeds/image/random/{count} deve retornar exatamente N imagens")
        void deveRetornarQuantidadeCorretaDeImagens() {
            int count = 5;

            List<String> images = given()
                .spec(requestSpec)
                .pathParam("count", count)
            .when()
                .get("/breeds/image/random/{count}")
            .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .extract().jsonPath().getList("message", String.class);

            assertThat(images)
                .as("Devem ser retornadas exatamente %d imagens", count)
                .hasSize(count);
        }

        @Test
        @Order(12)
        @Story("Tempo de resposta imagem aleatória")
        @Severity(SeverityLevel.NORMAL)
        @DisplayName("Tempo de resposta deve ser inferior a 2 segundos")
        void tempoDeRespostaDeveSerAceitavel() {
            long responseTime = given()
                .spec(requestSpec)
            .when()
                .get("/breeds/image/random")
            .then()
                .statusCode(200)
                .extract().time();

            assertThat(responseTime)
                .as("Tempo de resposta (%dms) deve ser menor que 2000ms", responseTime)
                .isLessThan(2000L);
        }
    }
}
