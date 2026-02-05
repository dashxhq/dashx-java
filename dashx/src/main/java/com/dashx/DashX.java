package com.dashx;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.dashx.graphql.generated.types.Account;
import com.dashx.graphql.generated.types.Asset;
import com.dashx.graphql.generated.types.Issue;
import com.dashx.graphql.generated.types.CreateIssueInput;
import com.dashx.graphql.generated.types.UpsertIssueInput;
import com.dashx.graphql.generated.types.IdentifyAccountInput;
import com.dashx.graphql.generated.types.SearchRecordsInput;
import com.dashx.graphql.generated.types.TrackEventInput;
import com.dashx.graphql.generated.types.TrackEventResponse;

import com.dashx.graphql.AccountService;
import com.dashx.graphql.AssetService;
import com.dashx.graphql.EventService;
import com.dashx.graphql.RecordService;
import com.dashx.graphql.IssueService;
import com.dashx.graphql.utils.SearchRecordsOptions;
import com.dashx.exception.DashXConfigurationException;
import com.dashx.exception.DashXValidationException;

public class DashX {
    private static final Logger logger = LoggerFactory.getLogger(DashX.class);
    private static final ConcurrentHashMap<String, DashX> instances = new ConcurrentHashMap<>();

    private final String instanceName;

    // Setup variables
    private String baseUrl;
    private String publicKey;
    private String privateKey;
    private String targetEnvironment;
    private DashXConfig config;

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
        if (config == null) {
            throw new DashXValidationException("Configuration cannot be null");
        }
        init(config);
    }

    private void init(DashXConfig config) {
        this.config = config;
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
                    headers,
                    config);
        } catch (URISyntaxException | MalformedURLException e) {
            throw new DashXConfigurationException("Invalid URL", e);
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
        if (options == null) {
            CompletableFuture<Account> future = new CompletableFuture<>();

            future.completeExceptionally(new DashXValidationException(
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

        logger.debug("Identifying account with uid: '{}', anonymousUid: '{}'", uid, anonymousUid);
        return accountService.identifyAccount(input).toFuture();
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
        if (event == null || event.trim().isEmpty()) {
            CompletableFuture<TrackEventResponse> future = new CompletableFuture<>();
            future.completeExceptionally(new DashXValidationException(
                    "Event name cannot be null or empty"));
            return future;
        }

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

        logger.debug("Tracking event '{}' for uid: '{}', anonymousUid: '{}'", event, accUid, accAnonUid);
        return eventService.trackEvent(input).toFuture();
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

    /**
     * Lists assets with optional filtering and pagination.
     *
     * @param filter Optional filter criteria
     * @param order Optional ordering criteria
     * @param limit Optional maximum number of results
     * @param page Optional page number for pagination
     * @return A CompletableFuture that will be completed with the list of assets or completed
     * exceptionally if there are GraphQL errors or execution errors.
     */
    public CompletableFuture<List<Asset>> listAssets(Map<String, Object> filter,
            List<Map<String, Object>> order, Integer limit, Integer page) {
        AssetService assetService = new AssetService(this.graphqlClient);

        logger.debug("Listing assets with filter: {}, limit: {}, page: {}", filter, limit, page);
        return assetService.listAssets(filter, order, limit, page).toFuture();
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

    /**
     * Get asset with a given id
     *
     * @param id The id of the asset to get
     * @return A CompletableFuture that will be completed with the asset or completed
     * exceptionally if there are GraphQL errors or execution errors.
     */
    public CompletableFuture<Asset> getAsset(String id) {
        if (id == null || id.trim().isEmpty()) {
            CompletableFuture<Asset> future = new CompletableFuture<>();
            future.completeExceptionally(new DashXValidationException(
                    "Asset ID cannot be null or empty"));
            return future;
        }

        AssetService assetService = new AssetService(this.graphqlClient);

        logger.debug("Getting asset with id: '{}'", id);
        return assetService.getAsset(id).toFuture();
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
        if (resource == null || resource.trim().isEmpty()) {
            CompletableFuture<List<Map<String, Object>>> future = new CompletableFuture<>();
            future.completeExceptionally(new DashXValidationException(
                    "Resource cannot be null or empty"));
            return future;
        }

        RecordService recordService = new RecordService(this.graphqlClient);

        // Use default options if null
        if (options == null) {
            options = SearchRecordsOptions.newBuilder().build();
        }

        SearchRecordsInput input = SearchRecordsInput.newBuilder().resource(resource)
                .filter(options.getFilter()).order(options.getOrder()).limit(options.getLimit())
                .page(options.getPage()).preview(options.getPreview())
                .language(options.getLanguage()).fields(options.getFields())
                .include(options.getInclude()).exclude(options.getExclude()).build();

        logger.debug("Searching records for resource: '{}' with filter: {}", resource, options.getFilter());
        return recordService.searchRecords(input).toFuture();
    }

    public CompletableFuture<List<Map<String, Object>>> searchRecords(String resource) {
        return searchRecords(resource, null);
    }

    /**
     * Creates a new issue.
     *
     * @param input The input data for creating the issue.
     * @return A CompletableFuture that will be completed with the created issue or completed
     *         exceptionally if there are GraphQL errors or execution errors.
     */
    public CompletableFuture<Issue> createIssue(CreateIssueInput input) {
        if (input == null) {
            CompletableFuture<Issue> future = new CompletableFuture<>();
            future.completeExceptionally(new DashXValidationException(
                    "CreateIssueInput cannot be null"));
            return future;
        }

        IssueService issueService = new IssueService(this.graphqlClient);

        logger.debug("Creating issue");
        return issueService.createIssue(input).toFuture();
    }

    /**
     * Creates a new issue or updates an existing one. The specific behavior (create vs. update)
     * might depend on the presence of an idempotency key or an ID in the input.
     *
     * @param input The input data for upserting the issue.
     * @return A CompletableFuture that will be completed with the upserted issue or completed
     *         exceptionally if there are GraphQL errors or execution errors.
     */
    public CompletableFuture<Issue> upsertIssue(UpsertIssueInput input) {
        if (input == null) {
            CompletableFuture<Issue> future = new CompletableFuture<>();
            future.completeExceptionally(new DashXValidationException(
                    "UpsertIssueInput cannot be null"));
            return future;
        }

        IssueService issueService = new IssueService(this.graphqlClient);

        logger.debug("Upserting issue");
        return issueService.upsertIssue(input).toFuture();
    }
}
