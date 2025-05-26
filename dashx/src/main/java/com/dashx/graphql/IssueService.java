package com.dashx.graphql;

import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import reactor.core.publisher.Mono;

import com.dashx.DashXGraphQLClient;
import com.dashx.graphql.generated.client.CreateIssueGraphQLQuery;
import com.dashx.graphql.generated.client.IssueProjectionRoot;
import com.dashx.graphql.generated.client.UpsertIssueGraphQLQuery;
import com.dashx.graphql.generated.types.CreateIssueInput;
import com.dashx.graphql.generated.types.Issue;
import com.dashx.graphql.generated.types.UpsertIssueInput;
import com.dashx.graphql.utils.Projections;

public class IssueService {
    private final DashXGraphQLClient client;

    public IssueService(DashXGraphQLClient client) {
        this.client = client;
    }

    public Mono<Issue> createIssue(CreateIssueInput input) {
        CreateIssueGraphQLQuery query = CreateIssueGraphQLQuery.newRequest().input(input).build();

        IssueProjectionRoot<?, ?> projection = Projections.fullIssueProjection();

        GraphQLQueryRequest request = new GraphQLQueryRequest(query, projection);

        return client.execute(request.serialize()).map(response -> {
            return response.extractValueAsObject("createIssue", Issue.class);
        });
    }

    public Mono<Issue> upsertIssue(UpsertIssueInput input) {
        UpsertIssueGraphQLQuery query = UpsertIssueGraphQLQuery.newRequest().input(input).build();

        IssueProjectionRoot<?, ?> projection = Projections.fullIssueProjection();

        GraphQLQueryRequest request = new GraphQLQueryRequest(query, projection);

        return client.execute(request.serialize()).map(response -> {
            return response.extractValueAsObject("upsertIssue", Issue.class);
        });
    }
}
