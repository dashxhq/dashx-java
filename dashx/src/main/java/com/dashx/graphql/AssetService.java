package com.dashx.graphql;

import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

import com.dashx.graphql.generated.client.AssetGraphQLQuery;
import com.dashx.graphql.generated.client.AssetProjectionRoot;
import com.dashx.graphql.generated.client.AssetsListGraphQLQuery;
import com.dashx.graphql.generated.types.Asset;

import com.dashx.DashXGraphQLClient;
import com.dashx.graphql.utils.Projections;

public class AssetService {
    private final DashXGraphQLClient client;

    public AssetService(DashXGraphQLClient client) {
        this.client = client;
    }

    public Mono<Asset> getAsset(String id) {
        AssetGraphQLQuery query = AssetGraphQLQuery.newRequest().id(id).build();

        AssetProjectionRoot<?, ?> projection = Projections.fullAssetProjection();

        GraphQLQueryRequest request = new GraphQLQueryRequest(query, projection);

        return client.execute(request.serialize()).map(response -> {
            return response.extractValueAsObject("asset", Asset.class);
        });
    }

    public Mono<List<Asset>> listAssets(Map<String, Object> filter, List<Map<String, Object>> order,
            Integer limit, Integer page) {
        AssetProjectionRoot<?, ?> projection = Projections.fullAssetProjection();

        GraphQLQueryRequest request = new GraphQLQueryRequest(AssetsListGraphQLQuery.newRequest()
                .filter(filter).order(order).limit(limit).page(page).build(), projection);

        return client.execute(request.serialize()).map(response -> {
            Asset[] assetsArray = response.extractValueAsObject("assetsList", Asset[].class);
            return List.of(assetsArray);
        });
    }
}
