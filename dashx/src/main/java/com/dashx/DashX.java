package com.dashx;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.dashx.graphql.generated.types.Account;
import com.dashx.graphql.generated.types.Asset;
import com.dashx.graphql.generated.types.IdentifyAccountInput;
import com.dashx.graphql.generated.types.SearchRecordsInput;
import com.dashx.graphql.generated.types.TrackEventInput;
import com.dashx.graphql.generated.types.TrackEventResponse;

import com.dashx.graphql.AccountService;
import com.dashx.graphql.AssetService;
import com.dashx.graphql.EventService;
import com.dashx.graphql.RecordService;
import com.dashx.graphql.utils.SearchRecordsOptions;

public class DashX {
    private static final Logger logger = LoggerFactory.getLogger(DashX.class);
    private static final HashMap<String, DashX> instances = new HashMap<>();

    private final String instanceName;

    // Setup variables
    private String baseUrl;
    private String publicKey;
    private String privateKey;
    private String targetEnvironment;

    // Account variables
    private String accountAnonymousUid;
    private String accountUid;

    private DashXGraphQLClient graphqlClient;

    private DashX(String instanceName) {
        this.instanceName = instanceName;
        this.graphqlClient = getGraphqlClient();
    }

    public static DashX getInstance() {
        return getInstance("default");
    }

    public static DashX getInstance(String instanceName) {
        if (!instances.containsKey(instanceName)) {
            instances.put(instanceName, new DashX(instanceName));
        }
        return instances.get(instanceName);
    }

    public void configure(DashXConfig config) {
        init(config);
    }

    private void init(DashXConfig config) {
        this.baseUrl = config.getBaseUrl();
        this.publicKey = config.getPublicKey();
        this.privateKey = config.getPrivateKey();
        this.targetEnvironment = config.getTargetEnvironment();

        createGraphqlClient();
    }

    private void createGraphqlClient() {
        graphqlClient = getGraphqlClient();
    }

    private DashXGraphQLClient getGraphqlClient() {
        try {
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            if (publicKey != null) {
                headers.add("X-Public-Key", publicKey);
            }
            if (privateKey != null) {
                headers.add("X-Private-Key", privateKey);
            }
            if (targetEnvironment != null) {
                headers.add("X-Target-Environment", targetEnvironment);
            }

            return new DashXGraphQLClient(
                    new URI(baseUrl != null ? baseUrl : "https://api.dashx.com/graphql").toURL(),
                    headers);
        } catch (URISyntaxException | MalformedURLException e) {
            throw new RuntimeException("Invalid URL", e);
        }
    }

    private String generateAccountAnonymousUid() {
        return UUID.randomUUID().toString();
    }

    public String getInstanceName() {
        return instanceName;
    }

    /**
     * Identifies a user with the provided options.
     *
     * @param options User identification options
     * @return A CompletableFuture that will be completed with the identification result or
     *         completed exceptionally if there are GraphQL errors or execution errors.
     */
    public CompletableFuture<Account> identify(Map<String, Object> options) {
        CompletableFuture<Account> future = new CompletableFuture<>();

        if (options == null) {
            future.completeExceptionally(new RuntimeException(
                    "'identify' cannot be called with null, please pass options of type 'object'."));

            return future;
        }

        String uid = options.containsKey(Constants.UserAttributes.UID)
                ? (String) options.get(Constants.UserAttributes.UID)
                : this.accountUid;

        String anonymousUid;

        if (options.containsKey(Constants.UserAttributes.ANONYMOUS_UID)) {
            anonymousUid = (String) options.get(Constants.UserAttributes.ANONYMOUS_UID);
        } else if (this.accountAnonymousUid != null) {
            anonymousUid = this.accountAnonymousUid;
        } else if (uid == null) {
            anonymousUid = generateAccountAnonymousUid();
        } else {
            anonymousUid = null;
        }

        AccountService accountService = new AccountService(this.graphqlClient);

        IdentifyAccountInput input =
                IdentifyAccountInput.newBuilder().uid(uid).anonymousUid(anonymousUid)
                        .email((String) options.get(Constants.UserAttributes.EMAIL))
                        .phone((String) options.get(Constants.UserAttributes.PHONE))
                        .name((String) options.get(Constants.UserAttributes.NAME))
                        .firstName((String) options.get(Constants.UserAttributes.FIRST_NAME))
                        .lastName((String) options.get(Constants.UserAttributes.LAST_NAME)).build();

        future = accountService.identifyAccount(input).toFuture().exceptionally(error -> {
            logger.error("Error identifying account:", error);

            return null;
        });

        return future;
    }

    /**
     * Tracks an event for a user.
     *
     * @param event The event name (event type)
     * @param uid Optional user ID
     * @param data Optional event data
     * @return A CompletableFuture that will be completed with the tracking result or completed
     *         exceptionally if there are GraphQL errors or execution errors.
     */
    public CompletableFuture<TrackEventResponse> track(String event, String uid,
            Map<String, Object> data) {
        CompletableFuture<TrackEventResponse> future = new CompletableFuture<>();

        // Use the passed uid or else use the identified uid,
        // and if that's null too, use the anonymous uid if present,
        // and if that's null too, generate a random uuid.
        // Also, make sure to pass anonymous uid as null if a uid is present.
        String accUid = uid != null ? uid : accountUid;
        String accAnonUid = accountAnonymousUid;

        if (accUid == null) {
            if (accAnonUid == null) {
                accAnonUid = generateAccountAnonymousUid();
            }
        } else {
            accAnonUid = null;
        }

        EventService eventService = new EventService(this.graphqlClient);

        TrackEventInput input = TrackEventInput.newBuilder().event(event).accountUid(accUid)
                .accountAnonymousUid(accAnonUid).data(data).build();

        future = eventService.trackEvent(input).toFuture().exceptionally(error -> {
            logger.error("Error tracking event:", error);

            return null;
        });

        return future;
    }

    public CompletableFuture<TrackEventResponse> track(String event, Map<String, Object> data) {
        return track(event, null, data);
    }

    public CompletableFuture<TrackEventResponse> track(String event, String uid) {
        return track(event, uid, null);
    }

    public CompletableFuture<TrackEventResponse> track(String event) {
        return track(event, null, null);
    }

    // /**
    // * Lists assets with optional filtering and pagination.
    // *
    // * @param filter Optional filter criteria
    // * @param order Optional ordering criteria
    // * @param limit Optional maximum number of results
    // * @param page Optional page number for pagination
    // * @return A CompletableFuture that will be completed with the list of assets or completed
    // * exceptionally if there are GraphQL errors or execution errors.
    // */
    public CompletableFuture<List<Asset>> listAssets(Map<String, Object> filter,
            List<Map<String, Object>> order, Integer limit, Integer page) {
        CompletableFuture<List<Asset>> future = new CompletableFuture<>();

        AssetService assetService = new AssetService(this.graphqlClient);

        future = assetService.listAssets(filter, order, limit, page).toFuture()
                .exceptionally(error -> {
                    logger.error("Error listing assets:", error);

                    return null;
                });

        return future;
    }

    public CompletableFuture<List<Asset>> listAssets(Map<String, Object> filter) {
        return listAssets(filter, null, null, null);
    }

    public CompletableFuture<List<Asset>> listAssets(Map<String, Object> filter,
            List<Map<String, Object>> order) {
        return listAssets(filter, order, null, null);
    }

    public CompletableFuture<List<Asset>> listAssets() {
        return listAssets(null, null, null, null);
    }

    // /**
    // * Get asset with a given id
    // *
    // * @param id The id of the asset to get
    // * @return A CompletableFuture that will be completed with the asset or completed
    // * exceptionally if there are GraphQL errors or execution errors.
    // */
    public CompletableFuture<Asset> getAsset(String id) {
        CompletableFuture<Asset> future = new CompletableFuture<>();

        AssetService assetService = new AssetService(this.graphqlClient);

        future = assetService.getAsset(id).toFuture().exceptionally(error -> {
            logger.error("Error listing assets:", error);

            return null;
        });

        return future;
    }

    /**
     * Searches records for a given resource with optional search parameters.
     *
     * @param resource The resource identifier to search (e.g., "users", "products")
     * @param options Optional search parameters like filters, sorting, pagination
     * @return A CompletableFuture that will be completed with the search results or completed
     *         exceptionally if there are GraphQL errors or execution errors.
     */
    public CompletableFuture<List<Map<String, Object>>> searchRecords(String resource,
            SearchRecordsOptions options) {
        CompletableFuture<List<Map<String, Object>>> future = new CompletableFuture<>();

        RecordService recordService = new RecordService(this.graphqlClient);

        SearchRecordsInput input = SearchRecordsInput.newBuilder().resource(resource)
                .filter(options.getFilter()).order(options.getOrder()).limit(options.getLimit())
                .page(options.getPage()).preview(options.getPreview())
                .language(options.getLanguage()).fields(options.getFields())
                .include(options.getInclude()).exclude(options.getExclude()).build();

        future = recordService.searchRecords(input).toFuture().exceptionally(error -> {
            logger.error("Error searching records:", error);

            return null;
        });

        return future;
    }

    public CompletableFuture<List<Map<String, Object>>> searchRecords(String resource) {
        return searchRecords(resource, null);
    }
}
