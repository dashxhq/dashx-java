package com.dashx.graphql;

import com.dashx.DashXGraphQLClient;
import com.dashx.graphql.generated.types.CreateIssueInput;
import com.dashx.graphql.generated.types.Issue;
import com.dashx.graphql.generated.types.UpsertIssueInput;
import java.util.Map;
import reactor.core.publisher.Mono;

/**
 * Service class for issue management operations.
 * Handles creation and upserting of issues through the DashX GraphQL API.
 * Issues can represent tickets, tasks, bugs, or any trackable work items in your system.
 */
public class IssueService {
    private final DashXGraphQLClient client;
    private final String fullIssueProjection;

    /**
     * Constructs a new IssueService with the specified GraphQL client.
     *
     * @param client the GraphQL client to use for executing queries and mutations
     */
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
                    groupId
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

    /**
     * Creates a new issue with the specified details.
     *
     * @param input the issue creation input containing title, description, assignee, priority, and other details
     * @return a Mono that emits the newly created Issue object with all its fields populated
     */
    public Mono<Issue> createIssue(CreateIssueInput input) {
        String query =
                "mutation CreateIssue($input: CreateIssueInput!) { createIssue(input: $input) "
                        + this.fullIssueProjection + " }";

        Map<String, Object> variables = Map.of("input", input);

        return client.execute(query, variables)
                .map(response -> response.extractValueAsObject("createIssue", Issue.class));
    }

    /**
     * Creates a new issue or updates an existing one based on the idempotency key.
     * If an issue with the same idempotency key exists, it will be updated; otherwise, a new issue is created.
     *
     * @param input the issue upsert input containing all issue details and an optional idempotency key
     * @return a Mono that emits the created or updated Issue object with all its fields populated
     */
    public Mono<Issue> upsertIssue(UpsertIssueInput input) {
        String query =
                "mutation UpsertIssue($input: UpsertIssueInput!) { upsertIssue(input: $input) "
                        + this.fullIssueProjection + " }";

        Map<String, Object> variables = Map.of("input", input);

        return client.execute(query, variables)
                .map(response -> response.extractValueAsObject("upsertIssue", Issue.class));
    }
}
