package com.dashx.graphql;

import java.util.Map;
import reactor.core.publisher.Mono;

import com.dashx.graphql.generated.types.Account;
import com.dashx.graphql.generated.types.IdentifyAccountInput;
import com.dashx.DashXGraphQLClient;

/**
 * Service class for account-related GraphQL operations.
 * Handles user identification and account management operations through the DashX GraphQL API.
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
}
