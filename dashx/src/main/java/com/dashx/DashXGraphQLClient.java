package com.dashx;

import com.dashx.exception.DashXGraphQLException;
import com.netflix.graphql.dgs.client.GraphQLError;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.MonoGraphQLClient;
import com.netflix.graphql.dgs.client.WebClientGraphQLClient;
import io.netty.channel.ChannelOption;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

/**
 * GraphQL client for executing queries and mutations against the DashX GraphQL API.
 * Handles HTTP client configuration, connection pooling, timeouts, and error handling.
 * Uses Spring WebFlux's reactive WebClient under the hood for non-blocking I/O.
 */
public class DashXGraphQLClient {
    private final WebClientGraphQLClient webClientGraphQLClient;

    /**
     * Constructs a new DashXGraphQLClient with the specified configuration.
     * Sets up connection pooling, timeouts, and HTTP client with optimal settings for the DashX API.
     *
     * @param url the base URL of the DashX GraphQL API endpoint
     * @param headers HTTP headers to include with every request (e.g., authentication keys, environment)
     * @param config configuration object containing timeout and connection pool settings
     */
    public DashXGraphQLClient(URL url, MultiValueMap<String, String> headers, DashXConfig config) {
        // Use config values or defaults (all timeout values are in milliseconds)
        int connectionTimeout = config != null && config.getConnectionTimeout() != null
                ? config.getConnectionTimeout() : 10000;
        int responseTimeout = config != null && config.getResponseTimeout() != null
                ? config.getResponseTimeout() : 30000;
        int maxConnections = config != null && config.getMaxConnections() != null
                ? config.getMaxConnections() : 500;
        int maxIdleTime = config != null && config.getMaxIdleTime() != null
                ? config.getMaxIdleTime() : 20000;

        // Configure connection pool
        ConnectionProvider connectionProvider = ConnectionProvider.builder("dashx-pool")
                .maxConnections(maxConnections)
                .maxIdleTime(Duration.ofMillis(maxIdleTime))
                .maxLifeTime(Duration.ofSeconds(60))
                .pendingAcquireTimeout(Duration.ofSeconds(60))
                .evictInBackground(Duration.ofSeconds(120))
                .build();

        // Configure HTTP client with timeouts and keep-alive
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
                .responseTimeout(Duration.ofMillis(responseTimeout))
                .keepAlive(true);

        // Build WebClient with configured HTTP client
        WebClient webClient = WebClient.builder()
                .baseUrl(url.toString())
                .defaultHeaders(httpHeaders -> httpHeaders.addAll(headers))
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

        this.webClientGraphQLClient = MonoGraphQLClient.createWithWebClient(webClient);
    }

    /**
     * Executes a GraphQL query or mutation and returns the response.
     * If the response contains GraphQL errors, the returned Mono will emit a DashXGraphQLException.
     *
     * @param query the GraphQL query or mutation string
     * @param variables the variables to pass to the query/mutation, can be empty
     * @return a Mono that emits the GraphQLResponse on success, or an error if GraphQL errors occurred
     * @throws DashXGraphQLException if the GraphQL response contains errors
     */
    public Mono<GraphQLResponse> execute(String query, Map<String, ?> variables) {
        return this.webClientGraphQLClient.reactiveExecuteQuery(query, variables)
                .flatMap(response -> {
                    List<GraphQLError> errors = response.getErrors();

                    if (errors != null && !errors.isEmpty()) {
                        return Mono.error(new DashXGraphQLException(errors));
                    }

                    return Mono.just(response);
                });
    }
}
