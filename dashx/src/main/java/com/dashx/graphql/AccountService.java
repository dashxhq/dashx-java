package com.dashx.graphql;

import java.util.Map;
import reactor.core.publisher.Mono;

import com.dashx.graphql.generated.types.Account;
import com.dashx.graphql.generated.types.IdentifyAccountInput;
import com.dashx.DashXGraphQLClient;

public class AccountService {
    private final DashXGraphQLClient client;
    private final String fullAccountProjection;

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

    public Mono<Account> identifyAccount(IdentifyAccountInput input) {
        String query =
                "mutation IdentifyAccount($input: IdentifyAccountInput!) { identifyAccount(input: $input) "
                        + this.fullAccountProjection + " }";

        Map<String, Object> variables = Map.of("input", input);

        return client.execute(query, variables)
                .map(response -> response.extractValueAsObject("identifyAccount", Account.class));
    }
}
