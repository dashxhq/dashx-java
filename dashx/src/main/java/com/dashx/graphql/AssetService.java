package com.dashx.graphql;

import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

import com.dashx.graphql.generated.types.Asset;
import com.dashx.DashXGraphQLClient;

public class AssetService {
    private final DashXGraphQLClient client;
    private final String fullAssetProjection;

    public AssetService(DashXGraphQLClient client) {
        this.client = client;
        this.fullAssetProjection = """
                {
                    id
                    workspaceId
                    resourceId
                    attributeId
                    storageProviderId
                    uploaderId
                    data
                    uploadStatus
                    processingStatus
                    name
                    size
                    mimeType
                    uploadStatusReason
                    processingStatusReason
                    url
                    staticVideoUrls
                    staticAudioUrl
                    createdAt
                    updatedAt
                }
                """;
    }

    public Mono<Asset> getAsset(String id) {
        String query =
                "query GetAsset($id: String!) { asset(id: $id) " + this.fullAssetProjection + " }";
        Map<String, Object> variables = Map.of("id", id);

        return client.execute(query, variables)
                .map(response -> response.extractValueAsObject("asset", Asset.class));
    }

    public Mono<List<Asset>> listAssets(Map<String, Object> filter, List<Map<String, Object>> order,
            Integer limit, Integer page) {
        String query =
                "query ListAssets($filter: JSON, $order: [JSON], $limit: Int, $page: Int) { assetsList(filter: $filter, order: $order, limit: $limit, page: $page) "
                        + this.fullAssetProjection + " }";

        Map<String, Object> variables =
                Map.of("filter", filter, "order", order, "limit", limit, "page", page);

        return client.execute(query, variables).map(response -> {
            Asset[] assetsArray = response.extractValueAsObject("assetsList", Asset[].class);
            return List.of(assetsArray);
        });
    }
}
