package com.dashx.graphql;

import com.dashx.DashXGraphQLClient;
import com.dashx.graphql.generated.types.Group;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

/**
 * Service class for group-related GraphQL operations.
 */
public class GroupService {
    private final DashXGraphQLClient client;
    private final String fullGroupProjection;

    public GroupService(DashXGraphQLClient client) {
        this.client = client;
        this.fullGroupProjection = """
                {
                    id
                    environmentId
                    workspaceId
                    parentId
                    name
                    position
                    accountKind
                    createdAt
                    updatedAt
                }
                """;
    }

    /**
     * Lists groups with optional filtering, ordering, and pagination.
     *
     * @param filter optional filter criteria, e.g. {@code {"name": {"eq": "Support"}}}
     * @param order optional ordering criteria
     * @param limit optional page size (the server defaults to 100)
     * @param page optional 1-indexed page number
     * @return a Mono that emits the matching groups
     */
    public Mono<List<Group>> listGroups(
            Map<String, Object> filter,
            List<Map<String, Object>> order,
            Integer limit,
            Integer page) {
        String query =
                "query ListGroups($filter: JSON, $order: [JSON!], $limit: Int, $page: Int) "
                        + "{ groupsList(filter: $filter, order: $order, limit: $limit, page: $page) "
                        + this.fullGroupProjection + " }";

        Map<String, Object> variables = new HashMap<>();
        if (filter != null) variables.put("filter", filter);
        if (order != null) variables.put("order", order);
        if (limit != null) variables.put("limit", limit);
        if (page != null) variables.put("page", page);

        return client.execute(query, variables).map(response -> {
            Group[] groups = response.extractValueAsObject("groupsList", Group[].class);
            return groups != null ? List.of(groups) : List.of();
        });
    }
}
