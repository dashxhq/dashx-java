package com.dashx.graphql;

import java.util.Map;
import reactor.core.publisher.Mono;

import com.dashx.graphql.generated.types.TrackEventResponse;
import com.dashx.graphql.generated.types.TrackEventInput;
import com.dashx.DashXGraphQLClient;

/**
 * Service class for event tracking operations.
 * Handles tracking of user events and analytics through the DashX GraphQL API.
 */
public class EventService {
    private final DashXGraphQLClient client;
    private final String fullTrackEventProjection;

    /**
     * Constructs a new EventService with the specified GraphQL client.
     *
     * @param client the GraphQL client to use for executing queries and mutations
     */
    public EventService(DashXGraphQLClient client) {
        this.client = client;
        this.fullTrackEventProjection = """
                {
                    success
                }
                """;
    }

    /**
     * Tracks an event with the specified details.
     * Events are used for analytics, triggering workflows, and monitoring user behavior.
     *
     * @param input the event tracking input containing event name, account identifiers, and event data
     * @return a Mono that emits a TrackEventResponse indicating success or failure
     */
    public Mono<TrackEventResponse> trackEvent(TrackEventInput input) {
        String query = "mutation TrackEvent($input: TrackEventInput!) { trackEvent(input: $input) "
                + this.fullTrackEventProjection + " }";
        Map<String, Object> variables = Map.of("input", input);

        return client.execute(query, variables).map(
                response -> response.extractValueAsObject("trackEvent", TrackEventResponse.class));
    }
}
