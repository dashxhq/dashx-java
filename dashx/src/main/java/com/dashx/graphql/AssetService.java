package com.dashx.graphql;

import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

import com.dashx.graphql.generated.types.Asset;
import com.dashx.DashXGraphQLClient;

/**
 * Service class for asset management operations.
 * Handles retrieval and listing of digital assets (files, images, videos) through the DashX GraphQL API.
 */
public class AssetService {
    private final DashXGraphQLClient client;
    private final String fullAssetProjection;

    /**
     * Constructs a new AssetService with the specified GraphQL client.
     *
     * @param client the GraphQL client to use for executing queries and mutations
     */
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

    /**
     * Retrieves a single asset by its ID.
     *
     * @param id the unique identifier of the asset to retrieve
     * @return a Mono that emits the Asset object with all its details
     */
    public Mono<Asset> getAsset(String id) {
        String query =
                "query GetAsset($id: String!) { asset(id: $id) " + this.fullAssetProjection + " }";
        Map<String, Object> variables = Map.of("id", id);

        return client.execute(query, variables)
                .map(response -> response.extractValueAsObject("asset", Asset.class));
    }

    /**
     * Lists assets with optional filtering, ordering, and pagination.
     *
     * @param filter optional filter criteria to narrow down results (e.g., by resource ID, status)
     * @param order optional ordering criteria to sort the results
     * @param limit optional maximum number of results to return per page
     * @param page optional page number for pagination (0-indexed)
     * @return a Mono that emits a list of Asset objects matching the criteria
     */
    public Mono<List<Asset>> listAssets(Map<String, Object> filter, List<Map<String, Object>> order,
            Integer limit, Integer page) {
        String query =
                "query ListAssets($filter: JSON, $order: [JSON], $limit: Int, $page: Int) { assetsList(filter: $filter, order: $order, limit: $limit, page: $page) "
                        + this.fullAssetProjection + " }";

        Map<String, Object> variables =
                Map.of("filter", filter, "order", order, "limit", limit, "page", page);

        return client.execute(query, variables).map(response -> {
            Asset[] assetsArray = response.extractValueAsObject("assetsList", Asset[].class);
            return assetsArray != null ? List.of(assetsArray) : List.of();
        });
    }
}
