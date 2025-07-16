package com.dashx.graphql;

import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

import com.dashx.graphql.generated.types.SearchRecordsInput;
import com.dashx.DashXGraphQLClient;

public class RecordService {
    private final DashXGraphQLClient client;

    public RecordService(DashXGraphQLClient client) {
        this.client = client;
    }

    public Mono<List<Map<String, Object>>> searchRecords(SearchRecordsInput input) {
        String query =
                "query SearchRecords($input: SearchRecordsInput!) { searchRecords(input: $input) }";

        Map<String, Object> variables = Map.of("input", input);

        return client.execute(query, variables).map(response -> {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> results =
                    response.extractValueAsObject("searchRecords", List.class);

            return results;
        });
    }
}
