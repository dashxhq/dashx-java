package com.dashx.graphql;

import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

import com.dashx.graphql.generated.types.SearchRecordsInput;
import com.dashx.DashXGraphQLClient;

/**
 * Service class for content record operations.
 * Handles searching and querying of content records through the DashX GraphQL API.
 * Records represent content items in your DashX content management system.
 */
public class RecordService {
    private final DashXGraphQLClient client;

    /**
     * Constructs a new RecordService with the specified GraphQL client.
     *
     * @param client the GraphQL client to use for executing queries and mutations
     */
    public RecordService(DashXGraphQLClient client) {
        this.client = client;
    }

    /**
     * Searches for content records based on the provided search criteria.
     * Supports filtering, sorting, pagination, localization, and field selection.
     *
     * @param input the search input containing resource name, filters, ordering, pagination,
     *              language preferences, and field inclusions/exclusions
     * @return a Mono that emits a list of records as maps, where each map represents a record
     *         with its fields and values
     */
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
