package com.dashx;

import static org.junit.jupiter.api.Assertions.*;

import com.dashx.exception.DashXGraphQLException;
import com.dashx.exception.DashXValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Member tokens are minted by the API, which resolves the member, so these run the real client
 * against a local stub server and assert the request that leaves the SDK.
 */
class DashXMemberTokenTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private HttpServer server;
    private final List<JsonNode> requests = Collections.synchronizedList(new ArrayList<>());
    private volatile Function<JsonNode, String> responder = request -> "{\"data\":{}}";
    private DashX dashx;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            JsonNode body = MAPPER.readTree(exchange.getRequestBody().readAllBytes());
            requests.add(body);
            byte[] response = responder.apply(body).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();

        DashX.resetInstances();
        dashx = DashX.getInstance("test-member-token");
        dashx.configure(
            new DashXConfig.Builder()
                .baseUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/graphql")
                .publicKey("test-public-key")
                .privateKey("test-private-key")
                .targetEnvironment("test")
                .build()
        );
    }

    @AfterEach
    void tearDown() {
        DashX.resetInstances();
        server.stop(0);
    }

    @Test
    void asksTheApiForAMemberTokenAndReturnsIt() throws Exception {
        responder = request ->
            "{\"data\":{\"generateIdentityToken\":{\"token\":\"member-token\"}}}";

        String token = dashx.generateMemberToken("  agent@example.com ").get(5, TimeUnit.SECONDS);

        assertEquals("member-token", token);
        assertEquals(1, requests.size());
        JsonNode request = requests.get(0);
        assertTrue(request.get("query").asText().contains("generateIdentityToken(input: $input)"));
        JsonNode input = request.get("variables").get("input");
        assertEquals("MEMBER", input.get("kind").asText());
        assertEquals("agent@example.com", input.get("uid").asText());
        assertEquals("test-public-key", input.get("publicKey").asText());
        assertEquals("test-private-key", input.get("privateKey").asText());
    }

    @Test
    void surfacesTheApiRefusingTheMember() {
        responder = request ->
            "{\"errors\":[{\"message\":\"Several enabled members have this email. "
                + "Pass the member's account id instead.\"}],\"data\":null}";

        ExecutionException error = assertThrows(
            ExecutionException.class,
            () -> dashx.generateMemberToken("agent@example.com").get(5, TimeUnit.SECONDS)
        );

        assertInstanceOf(DashXGraphQLException.class, error.getCause());
        assertTrue(error.getCause().getMessage().contains("account id"));
    }

    @Test
    void rejectsABlankMemberWithoutCallingTheApi() {
        ExecutionException error = assertThrows(
            ExecutionException.class,
            () -> dashx.generateMemberToken(" ").get(5, TimeUnit.SECONDS)
        );

        assertInstanceOf(DashXValidationException.class, error.getCause());
        assertTrue(requests.isEmpty());
    }
}
