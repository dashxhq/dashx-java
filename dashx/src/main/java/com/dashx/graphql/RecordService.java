package com.dashx.graphql;

import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import reactor.core.publisher.Mono;

import com.dashx.graphql.generated.client.SearchRecordsGraphQLQuery;
import com.dashx.graphql.generated.types.SearchRecordsInput;
import java.util.List;
import org.json.JSONObject;
import com.dashx.DashXGraphQLClient;

public class RecordService {
    private final DashXGraphQLClient client;

    public RecordService(DashXGraphQLClient client) {
        this.client = client;
    }

    public Mono<List<JSONObject>> searchRecords(SearchRecordsInput input) {
        SearchRecordsGraphQLQuery query =
                SearchRecordsGraphQLQuery.newRequest().input(input).build();

        GraphQLQueryRequest request = new GraphQLQueryRequest(query);

        return client.execute(request.serialize()).map(response -> {
            JSONObject[] records =
                    response.extractValueAsObject("searchRecords", JSONObject[].class);
            return List.of(records);
        });
    }
}
