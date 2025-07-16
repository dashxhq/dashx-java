package com.dashx;

import com.netflix.graphql.dgs.client.GraphQLError;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.MonoGraphQLClient;
import com.netflix.graphql.dgs.client.WebClientGraphQLClient;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class DashXGraphQLClient {
    private final WebClientGraphQLClient webClientGraphQLClient;

    public DashXGraphQLClient(URL url, MultiValueMap<String, String> headers) {
        WebClient webClient = WebClient.builder().baseUrl(url.toString())
                .defaultHeaders(httpHeaders -> httpHeaders.addAll(headers)).build();

        this.webClientGraphQLClient = MonoGraphQLClient.createWithWebClient(webClient);
    }

    public Mono<GraphQLResponse> execute(String query, Map<String, ?> variables)
            throws RuntimeException {
        return this.webClientGraphQLClient.reactiveExecuteQuery(query, variables)
                .doOnNext(response -> {
                    List<GraphQLError> errors = response.getErrors();

                    if (errors != null && !errors.isEmpty()) {
                        throw new RuntimeException(errors.toString());
                    }
                });
    }
}
