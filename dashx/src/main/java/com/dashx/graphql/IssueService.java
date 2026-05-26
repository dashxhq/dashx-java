package com.dashx.graphql;

import com.dashx.DashXGraphQLClient;
import com.dashx.graphql.generated.types.AggregateResponse;
import com.dashx.graphql.generated.types.CreateIssueInput;
import com.dashx.graphql.generated.types.Issue;
import com.dashx.graphql.generated.types.UpsertIssueInput;
import java.util.HashMap;
import java.util.List;
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
                    requestedById
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

    /**
     * Lists issues with optional filtering, ordering, and pagination.
     *
     * @param filter optional filter criteria to narrow down results
     * @param order optional ordering criteria to sort the results
     * @param limit optional maximum number of results to return per page
     * @param page optional page number for pagination
     * @param targetEnvironment optional target environment identifier to scope the query
     * @return a Mono that emits a list of Issue objects matching the criteria
     */
    public Mono<List<Issue>> listIssues(Map<String, Object> filter,
            List<Map<String, Object>> order, Integer limit, Integer page, String targetEnvironment) {
        String query =
                "query ListIssues($filter: JSON, $order: [JSON!], $limit: Int, $page: Int, $targetEnvironment: String) { issuesList(filter: $filter, order: $order, limit: $limit, page: $page, targetEnvironment: $targetEnvironment) "
                        + this.fullIssueProjection + " }";

        Map<String, Object> variables = new HashMap<>();
        if (filter != null) variables.put("filter", filter);
        if (order != null) variables.put("order", order);
        if (limit != null) variables.put("limit", limit);
        if (page != null) variables.put("page", page);
        if (targetEnvironment != null) variables.put("targetEnvironment", targetEnvironment);

        return client.execute(query, variables).map(response -> {
            Issue[] issuesArray = response.extractValueAsObject("issuesList", Issue[].class);
            return issuesArray != null ? List.of(issuesArray) : List.of();
        });
    }

    /**
     * Counts issues matching the provided filter.
     *
     * @param filter optional filter criteria to narrow down the count
     * @param targetEnvironment optional target environment identifier to scope the query
     * @return a Mono that emits an AggregateResponse containing the count of matching issues
     */
    public Mono<AggregateResponse> aggregateIssues(Map<String, Object> filter,
            String targetEnvironment) {
        String query =
                "query AggregateIssues($filter: JSON, $targetEnvironment: String) { issuesAggregate(filter: $filter, targetEnvironment: $targetEnvironment) { count } }";

        Map<String, Object> variables = new HashMap<>();
        if (filter != null) variables.put("filter", filter);
        if (targetEnvironment != null) variables.put("targetEnvironment", targetEnvironment);

        return client.execute(query, variables).map(response -> response
                .extractValueAsObject("issuesAggregate", AggregateResponse.class));
    }
}
