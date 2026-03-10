package com.dashx.graphql;

import java.util.Map;
import reactor.core.publisher.Mono;

import com.dashx.DashXGraphQLClient;
import com.dashx.graphql.generated.types.Broadcast;
import com.dashx.graphql.generated.types.CreateBroadcastInput;

/**
 * Service class for broadcast operations.
 * Handles creation of broadcasts through the DashX GraphQL API.
 */
public class BroadcastService {
    private final DashXGraphQLClient client;
    private final String fullBroadcastProjection;

    public BroadcastService(DashXGraphQLClient client) {
        this.client = client;
        this.fullBroadcastProjection = """
                {
                    id
                    templateSubkind
                    environmentId
                    campaignId
                    integrationId
                    templateId
                    content
                    name
                    data
                    status
                    failureReason
                    queuedMessagesCount
                    sentMessagesCount
                    deliveredMessagesCount
                    failedMessagesCount
                    openedMessagesCount
                    clickedMessagesCount
                    scheduledAt
                    custom
                    createdAt
                    updatedAt
                }
                """;
    }

    /**
     * Creates a new broadcast with the specified details.
     *
     * @param input the broadcast creation input, corresponding to CreateBroadcastInput
     * @return a Mono that emits the newly created Broadcast object with all its fields populated
     */
    public Mono<Broadcast> createBroadcast(CreateBroadcastInput input) {
        String query =
                "mutation CreateBroadcast($input: CreateBroadcastInput!) { createBroadcast(input: $input) "
                        + this.fullBroadcastProjection + " }";

        Map<String, Object> variables = Map.of("input", input);

        return client.execute(query, variables)
                .map(response -> response.extractValueAsObject("createBroadcast", Broadcast.class));
    }
}
