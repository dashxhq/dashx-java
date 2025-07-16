package com.dashx.graphql;

import java.util.Map;
import reactor.core.publisher.Mono;

import com.dashx.graphql.generated.types.TrackEventResponse;
import com.dashx.graphql.generated.types.TrackEventInput;
import com.dashx.DashXGraphQLClient;

public class EventService {
    private final DashXGraphQLClient client;
    private final String fullTrackEventProjection;

    public EventService(DashXGraphQLClient client) {
        this.client = client;
        this.fullTrackEventProjection = """
                {
                    success
                }
                """;
    }

    public Mono<TrackEventResponse> trackEvent(TrackEventInput input) {
        String query = "mutation TrackEvent($input: TrackEventInput!) { trackEvent(input: $input) "
                + this.fullTrackEventProjection + " }";
        Map<String, Object> variables = Map.of("input", input);

        return client.execute(query, variables).map(
                response -> response.extractValueAsObject("trackEvent", TrackEventResponse.class));
    }
}
