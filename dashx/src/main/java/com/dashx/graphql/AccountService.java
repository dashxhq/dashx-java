package com.dashx.graphql;

import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import reactor.core.publisher.Mono;

import com.dashx.graphql.generated.client.IdentifyAccountGraphQLQuery;
import com.dashx.graphql.generated.client.IdentifyAccountProjectionRoot;
import com.dashx.graphql.generated.types.Account;
import com.dashx.graphql.generated.types.IdentifyAccountInput;

import com.dashx.DashXGraphQLClient;
import com.dashx.graphql.utils.Projections;

public class AccountService {
    private final DashXGraphQLClient client;

    public AccountService(DashXGraphQLClient client) {
        this.client = client;
    }

    public Mono<Account> identifyAccount(IdentifyAccountInput input) {
        IdentifyAccountGraphQLQuery query =
                IdentifyAccountGraphQLQuery.newRequest().input(input).build();

        IdentifyAccountProjectionRoot<?, ?> projection = Projections.fullAccountProjection();

        GraphQLQueryRequest request = new GraphQLQueryRequest(query, projection);

        return client.execute(request.serialize()).map(response -> {
            return response.extractValueAsObject("identifyAccount", Account.class);
        });
    }
}
