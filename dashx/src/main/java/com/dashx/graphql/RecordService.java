package com.dashx.graphql;

import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

import com.dashx.graphql.generated.client.SearchRecordsGraphQLQuery;
import com.dashx.graphql.generated.types.SearchRecordsInput;
import com.dashx.DashXGraphQLClient;

public class RecordService {
    private final DashXGraphQLClient client;

    public RecordService(DashXGraphQLClient client) {
        this.client = client;
    }

    public Mono<List<Map<String, Object>>> searchRecords(SearchRecordsInput input) {
        SearchRecordsGraphQLQuery query =
                SearchRecordsGraphQLQuery.newRequest().input(input).build();

        GraphQLQueryRequest request = new GraphQLQueryRequest(query);

        return client.execute(request.serialize()).map(response -> {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> results =
                    response.extractValueAsObject("searchRecords", List.class);
            return results;
        });
    }
}
