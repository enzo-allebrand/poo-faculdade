package br.edu.setrem.financeiro.web;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySink;
import io.smallrye.reactive.messaging.memory.InMemorySource;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class FinanceiroApiTest {

    @Inject
    @Any
    InMemoryConnector connector;

    private void drenarFilaKafkaEAguardar(String cookie, int quantidadeEsperada) throws InterruptedException {
        InMemorySink<String> sink = connector.sink("transacoes-out");
        InMemorySource<String> source = connector.source("transacoes-in");
        var mensagens = new java.util.ArrayList<>(sink.received());
        sink.clear();
        for (var msg : mensagens) {
            source.send(msg.getPayload());
        }

        long limite = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < limite) {
            List<?> lista = given()
                    .cookie("access_token", cookie)
                    .when()
                    .get("/Transacao/findAll")
                    .then()
                    .statusCode(200)
                    .extract().as(List.class);
            if (lista.size() >= quantidadeEsperada) {
                return;
            }
            Thread.sleep(50);
        }
        fail("Consumidor Kafka não persistiu as " + quantidadeEsperada + " transações dentro do tempo limite.");
    }

    @Test
    void endpointsProtegidosExigemAutenticacao() {
        given().when().get("/Dashboard").then().statusCode(401);
        given().when().get("/Conta/findAll").then().statusCode(401);
        given().when().get("/Categoria/findAll").then().statusCode(401);
        given().when().get("/Transacao/findAll").then().statusCode(401);
        given().when().get("/auth/me").then().statusCode(401);
    }

    @Test
    void fluxoCompletoCleanArchitectureComJwtHttpOnlyEKafka() throws InterruptedException {
        // 1. Registro com cookie HttpOnly
        Response regRes = given()
                .contentType(ContentType.JSON)
                .body(Map.of("nome", "Enzo Teste", "email", "enzo@setrem.edu.br", "senha", "senha123"))
                .when()
                .post("/auth/register")
                .then()
                .statusCode(201)
                .body("nome", equalTo("Enzo Teste"))
                .body("email", equalTo("enzo@setrem.edu.br"))
                .extract().response();

        String cookie = regRes.getCookie("access_token");
        assertNotNull(cookie);
        assertTrue(regRes.getHeader("Set-Cookie").contains("HttpOnly"));

        // E-mail duplicado retorna 409
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("nome", "Outro", "email", "enzo@setrem.edu.br", "senha", "senha123"))
                .when()
                .post("/auth/register")
                .then()
                .statusCode(409);

        // Login inválido retorna 401
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "enzo@setrem.edu.br", "senha", "errada"))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401);

        // Login válido
        String authCookie = given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "enzo@setrem.edu.br", "senha", "senha123"))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract().cookie("access_token");

        given()
                .cookie("access_token", authCookie)
                .when()
                .get("/auth/me")
                .then()
                .statusCode(200)
                .body("nome", equalTo("Enzo Teste"));

        // 2. Criar duas contas
        Number conta1Id = given()
                .cookie("access_token", authCookie)
                .contentType(ContentType.JSON)
                .body(Map.of("descricao", "Nubank", "saldoInicial", 1000))
                .when()
                .post("/Conta")
                .then()
                .statusCode(201)
                .extract().path("id");

        Number conta2Id = given()
                .cookie("access_token", authCookie)
                .contentType(ContentType.JSON)
                .body(Map.of("descricao", "Carteira", "saldoInicial", 200))
                .when()
                .post("/Conta")
                .then()
                .statusCode(201)
                .extract().path("id");

        // 3. Criar categoria personalizada e verificar categorias padrão
        Number catId = given()
                .cookie("access_token", authCookie)
                .contentType(ContentType.JSON)
                .body(Map.of("descricao", "Mercado", "tipo", "D"))
                .when()
                .post("/Categoria")
                .then()
                .statusCode(201)
                .extract().path("id");

        List<Map<String, Object>> categorias = given()
                .cookie("access_token", authCookie)
                .when()
                .get("/Categoria/findAll")
                .then()
                .statusCode(200)
                .extract().as(List.class);
        assertTrue(categorias.size() >= 7);

        Number catSalarioId = (Number) categorias.stream()
                .filter(c -> "Salário".equals(c.get("descricao")))
                .findFirst()
                .orElseThrow()
                .get("id");

        // 4. Lançar transações assíncronas via Kafka (202 Accepted)
        given()
                .cookie("access_token", authCookie)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "descricao", "Salário Mensal",
                        "valor", 3000,
                        "tipo", "R",
                        "contaId", conta1Id.longValue(),
                        "categoriaId", catSalarioId.longValue()
                ))
                .when()
                .post("/Transacao")
                .then()
                .statusCode(202)
                .body("status", equalTo("ENFILEIRADO"));

        given()
                .cookie("access_token", authCookie)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "descricao", "Supermercado",
                        "valor", 150,
                        "tipo", "D",
                        "contaId", conta2Id.longValue(),
                        "categoriaId", catId.longValue()
                ))
                .when()
                .post("/Transacao")
                .then()
                .statusCode(202)
                .body("status", equalTo("ENFILEIRADO"));

        drenarFilaKafkaEAguardar(authCookie, 2);

        // 5. Filtrar transações por conta e categoria
        given()
                .cookie("access_token", authCookie)
                .queryParam("contaId", conta2Id.longValue())
                .queryParam("categoriaId", catId.longValue())
                .when()
                .get("/Transacao/findAll")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].descricao", equalTo("Supermercado"));

        // Integridade referencial ao tentar excluir conta/categoria em uso
        given().cookie("access_token", authCookie).when().delete("/Conta/" + conta1Id).then().statusCode(409);
        given().cookie("access_token", authCookie).when().delete("/Categoria/" + catId).then().statusCode(409);

        // 6. Transferência assíncrona via Kafka (202 Accepted)
        given()
                .cookie("access_token", authCookie)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "contaOrigemId", conta1Id.longValue(),
                        "contaDestinoId", conta2Id.longValue(),
                        "valor", 500,
                        "descricao", "Saque para carteira"
                ))
                .when()
                .post("/Transacao/transferencia")
                .then()
                .statusCode(202)
                .body("status", equalTo("ENFILEIRADO"));

        drenarFilaKafkaEAguardar(authCookie, 4);

        // 7. Validar Dashboard (Nubank = 3500, Carteira = 550, Geral = 4050)
        given()
                .cookie("access_token", authCookie)
                .when()
                .get("/Dashboard")
                .then()
                .statusCode(200)
                .body("saldoGeral", comparesEqualTo(4050.0f))
                .body("contas.find { it.descricao == 'Nubank' }.saldoAtual", comparesEqualTo(3500.0f))
                .body("contas.find { it.descricao == 'Carteira' }.saldoAtual", comparesEqualTo(550.0f));

        // 8. Isolamento entre usuários
        String outroCookie = given()
                .contentType(ContentType.JSON)
                .body(Map.of("nome", "Outro Usuário", "email", "outro@setrem.edu.br", "senha", "senha123"))
                .when()
                .post("/auth/register")
                .then()
                .statusCode(201)
                .extract().cookie("access_token");

        given()
                .cookie("access_token", outroCookie)
                .when()
                .get("/Conta/findAll")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));

        // 9. Excluir uma perna da transferência remove as duas pernas
        Number transfSaidaId = given()
                .cookie("access_token", authCookie)
                .queryParam("contaId", conta1Id.longValue())
                .queryParam("tipo", "D")
                .when()
                .get("/Transacao/findAll")
                .then()
                .statusCode(200)
                .extract().path("[0].id");

        given()
                .cookie("access_token", authCookie)
                .when()
                .delete("/Transacao/" + transfSaidaId)
                .then()
                .statusCode(204);

        given()
                .cookie("access_token", authCookie)
                .when()
                .get("/Dashboard")
                .then()
                .statusCode(200)
                .body("contas.find { it.descricao == 'Nubank' }.saldoAtual", comparesEqualTo(4000.0f))
                .body("contas.find { it.descricao == 'Carteira' }.saldoAtual", comparesEqualTo(50.0f));

        // 10. Logout
        Response logoutRes = given()
                .cookie("access_token", authCookie)
                .when()
                .post("/auth/logout")
                .then()
                .statusCode(204)
                .extract().response();

        assertTrue(logoutRes.getHeader("Set-Cookie").contains("Max-Age=0"));
    }
}
