package com.dashx.graphql;

import com.dashx.DashXGraphQLClient;
import com.dashx.graphql.generated.types.Account;
import com.dashx.graphql.generated.types.IdentifyAccountInput;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

/**
 * Service class for account-related GraphQL operations.
 * Handles user identification and account lookups through the DashX GraphQL API.
 */
public class AccountService {
    private final DashXGraphQLClient client;
    private final String fullAccountProjection;

    /**
     * Constructs a new AccountService with the specified GraphQL client.
     *
     * @param client the GraphQL client to use for executing queries and mutations
     */
    public AccountService(DashXGraphQLClient client) {
        this.client = client;
        this.fullAccountProjection = """
                {
                    id
                    environmentId
                    kind
                    email
                    phone
                    fullName
                    name
                    firstName
                    lastName
                    avatar
                    timeZone
                    uid
                    anonymousUid
                    createdAt
                    updatedAt
                }
                """;
    }

    /**
     * Identifies or updates an account with the provided information.
     * This operation creates a new account if one doesn't exist, or updates an existing account.
     *
     * @param input the account identification input containing user details such as uid, email, name, etc.
     * @return a Mono that emits the identified or updated Account object
     */
    public Mono<Account> identifyAccount(IdentifyAccountInput input) {
        String query =
                "mutation IdentifyAccount($input: IdentifyAccountInput!) { identifyAccount(input: $input) "
                        + this.fullAccountProjection + " }";

        Map<String, Object> variables = Map.of("input", input);

        return client.execute(query, variables)
                .map(response -> response.extractValueAsObject("identifyAccount", Account.class));
    }

    /**
     * Lists accounts. Covers workspace members and service accounts (live environment) and
     * users/visitors (target environment). Besides column filters such as
     * {@code {"email": {"eq": "..."}}}, the server accepts {@code {"groupId": {"eq": "<uuid>"}}}
     * (or {@code "in"}) to restrict results to a group's members.
     *
     * @param filter optional filter criteria
     * @param order optional ordering criteria, e.g. {@code {"createdAt": "asc"}}
     * @param limit optional page size (the server defaults to 100)
     * @param page optional 1-indexed page number
     * @return a Mono that emits the matching accounts
     */
    public Mono<List<Account>> listAccounts(
            Map<String, Object> filter,
            List<Map<String, Object>> order,
            Integer limit,
            Integer page) {
        String query =
                "query ListAccounts($filter: JSON, $order: [JSON!], $limit: Int, $page: Int) "
                        + "{ accountsList(filter: $filter, order: $order, limit: $limit, page: $page) "
                        + this.fullAccountProjection + " }";

        Map<String, Object> variables = new HashMap<>();
        if (filter != null) variables.put("filter", filter);
        if (order != null) variables.put("order", order);
        if (limit != null) variables.put("limit", limit);
        if (page != null) variables.put("page", page);

        return client.execute(query, variables).map(response -> {
            Account[] accounts = response.extractValueAsObject("accountsList", Account[].class);
            return accounts != null ? List.of(accounts) : List.of();
        });
    }
}
