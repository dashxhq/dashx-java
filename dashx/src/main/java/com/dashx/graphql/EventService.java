package com.dashx.graphql;

import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import reactor.core.publisher.Mono;

import com.dashx.graphql.generated.client.TrackEventGraphQLQuery;
import com.dashx.graphql.generated.client.TrackEventProjectionRoot;
import com.dashx.graphql.generated.types.TrackEventResponse;
import com.dashx.graphql.generated.types.TrackEventInput;
import com.dashx.DashXGraphQLClient;
import com.dashx.graphql.utils.Projections;

public class EventService {
    private final DashXGraphQLClient client;

    public EventService(DashXGraphQLClient client) {
        this.client = client;
    }

    public Mono<TrackEventResponse> trackEvent(TrackEventInput input) {
        TrackEventGraphQLQuery query = TrackEventGraphQLQuery.newRequest().input(input).build();

        TrackEventProjectionRoot<?, ?> projection = Projections.fullTrackEventProjection();

        GraphQLQueryRequest request = new GraphQLQueryRequest(query, projection);

        return client.execute(request.serialize()).map(response -> {
            return response.extractValueAsObject("trackEvent", TrackEventResponse.class);
        });
    }
}
