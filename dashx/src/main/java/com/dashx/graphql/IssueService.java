package com.dashx.graphql;

import com.dashx.DashXGraphQLClient;
import com.dashx.graphql.generated.types.CreateIssueInput;
import com.dashx.graphql.generated.types.Issue;
import com.dashx.graphql.generated.types.UpsertIssueInput;
import java.util.Map;
import reactor.core.publisher.Mono;

public class IssueService {
    private final DashXGraphQLClient client;
    private final String fullIssueProjection;

    public IssueService(DashXGraphQLClient client) {
        this.client = client;
        this.fullIssueProjection = """
                {
                    id
                    workspaceId
                    issueStatusId
                    createdById
                    environmentId
                    spaceId
                    parentId
                    assigneeId
                    title
                    description
                    position
                    properties
                    createdAt
                    updatedAt
                    issueTypeId
                    dueAt
                    number
                    idempotencyKey
                    priority
                }
                """;
    }

    public Mono<Issue> createIssue(CreateIssueInput input) {
        String query =
                "mutation CreateIssue($input: CreateIssueInput!) { createIssue(input: $input) "
                        + this.fullIssueProjection + " }";

        Map<String, Object> variables = Map.of("input", input);

        return client.execute(query, variables)
                .map(response -> response.extractValueAsObject("createIssue", Issue.class));
    }

    public Mono<Issue> upsertIssue(UpsertIssueInput input) {
        String query =
                "mutation UpsertIssue($input: UpsertIssueInput!) { upsertIssue(input: $input) "
                        + this.fullIssueProjection + " }";

        Map<String, Object> variables = Map.of("input", input);

        return client.execute(query, variables)
                .map(response -> response.extractValueAsObject("upsertIssue", Issue.class));
    }
}
