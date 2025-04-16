package com.dashx

import com.dashx.graphql.SearchRecordsOptions
import com.dashx.graphql.generated.*
import com.dashx.graphql.generated.identifyaccount.Account
import com.dashx.graphql.generated.inputs.*
import com.dashx.graphql.generated.trackevent.TrackEventResponse
import com.dashx.graphql.models.Asset
import com.dashx.graphql.toAsset
import com.dashx.graphql.toInput
import com.expediagroup.graphql.client.serialization.GraphQLClientKotlinxSerializer
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import java.net.URI
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.json.JSONObject

private val logger = KotlinLogging.logger {}

class DashX
private constructor(
    val instanceName: String,
) {
    companion object {
        private val instances: MutableMap<String, DashX> = mutableMapOf()

        @JvmStatic fun getInstance(): DashX = getInstance("default")

        @JvmStatic
        fun getInstance(instanceName: String): DashX =
            instances.getOrPut(instanceName) { DashX(instanceName) }
    }

    // Setup variables
    private var baseUrl: String? = null
    private var publicKey: String? = null
    private var privateKey: String? = null
    private var targetEnvironment: String? = null

    // Account variables
    private var accountAnonymousUid: String? = null
    private var accountUid: String? = null

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun configure(config: DashXConfig) {
        init(config)
    }

    private fun init(config: DashXConfig) {
        this.baseUrl = config.baseUrl
        this.publicKey = config.publicKey
        this.privateKey = config.privateKey
        this.targetEnvironment = config.targetEnvironment

        createGraphqlClient()
    }

    private var graphqlClient = getGraphqlClient()

    private fun createGraphqlClient() {
        graphqlClient = getGraphqlClient()
    }

    private fun getGraphqlClient(): DashXGraphQLKtorClient {
        val httpClient =
            HttpClient(engineFactory = io.ktor.client.engine.okhttp.OkHttp) {
                engine {
                    config {
                        connectTimeout(10, TimeUnit.SECONDS)
                        readTimeout(60, TimeUnit.SECONDS)
                        writeTimeout(60, TimeUnit.SECONDS)
                    }
                }
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.ALL
                }

                defaultRequest {
                    publicKey?.let { header("X-Public-Key", it) }

                    privateKey?.let { header("X-Private-Key", it) }

                    targetEnvironment?.let { header("X-Target-Environment", it) }
                }
            }

        return DashXGraphQLKtorClient(
            url = URI(baseUrl ?: "https://api.dashx.com/graphql").toURL(),
            httpClient = httpClient,
            serializer = GraphQLClientKotlinxSerializer(),
        )
    }

    private fun generateAccountAnonymousUid(): String = UUID.randomUUID().toString()

    /**
     * Identifies a user with the provided options.
     *
     * @param options User identification options
     * @return A CompletableFuture that will be completed with the identification result or
     *   completed exceptionally
     *
     * ```
     *         if there are GraphQL errors or execution errors
     * ```
     */
    fun identify(options: HashMap<String, String>? = null): CompletableFuture<Account?> {
        val future = CompletableFuture<Account?>()

        if (options == null) {
            logger.debug {
                "'identify' cannot be called with null, please pass options of type 'object'."
            }

            future.completeExceptionally(
                RuntimeException(
                    "'identify' cannot be called with null, please pass options of type 'object'.",
                ),
            )
            return future
        }

        val uid = options[UserAttributes.UID] ?: this.accountUid
        val anonymousUid =
            options[UserAttributes.ANONYMOUS_UID]
                ?: if (this.accountAnonymousUid != null) {
                    this.accountAnonymousUid
                } else if (uid == null) {
                    generateAccountAnonymousUid()
                } else {
                    null
                }

        val query =
            IdentifyAccount(
                variables =
                    IdentifyAccount.Variables(
                        IdentifyAccountInput(
                            uid = uid,
                            anonymousUid = anonymousUid,
                            email = options[UserAttributes.EMAIL],
                            phone = options[UserAttributes.PHONE],
                            name = options[UserAttributes.NAME],
                            firstName = options[UserAttributes.FIRST_NAME],
                            lastName = options[UserAttributes.LAST_NAME],
                        ),
                    ),
            )

        coroutineScope.launch {
            try {
                val result = graphqlClient.execute(query)
                if (!result.errors.isNullOrEmpty()) {
                    logger.debug { result.errors.toString() }

                    future.completeExceptionally(
                        RuntimeException("GraphQL errors: ${result.errors}"),
                    )
                } else {
                    future.complete(result.data?.identifyAccount)
                }
            } catch (e: Exception) {
                logger.error(e) { "Error executing identify query." }

                future.completeExceptionally(e)
            }
        }
        return future
    }

    /**
     * Tracks an event for a user.
     *
     * @param event The event name (event type)
     * @param uid Optional user ID
     * @param data Optional event data
     * @return A CompletableFuture that will be completed with the tracking result or completed
     *   exceptionally
     *
     * ```
     *         if there are GraphQL errors or execution errors
     * ```
     */
    fun track(
        event: String,
        uid: String? = null,
        data: HashMap<String, String>? = hashMapOf(),
    ): CompletableFuture<TrackEventResponse?> {
        val future = CompletableFuture<TrackEventResponse?>()
        val jsonData =
            data?.toMap()?.let { Json.parseToJsonElement(JSONObject(it).toString()).jsonObject }

        // Use the passed uid or else use the identified uid,
        // and if that's null too, use the anonymuos uid if present,
        // and if that's null too, generate a random uuid.
        // Also, make sure to pass annonymous uid as null if a uid is present.
        val accUid = uid ?: accountUid
        var accAnonUid = accountAnonymousUid

        if (accUid == null) {
            if (accAnonUid == null) {
                accAnonUid = generateAccountAnonymousUid()
            }
        } else {
            accAnonUid = null
        }

        val query =
            TrackEvent(
                variables =
                    TrackEvent.Variables(
                        TrackEventInput(
                            accountAnonymousUid = accAnonUid,
                            accountUid = accUid,
                            data = jsonData,
                            event = event,
                        ),
                    ),
            )

        coroutineScope.launch {
            try {
                val result = graphqlClient.execute(query)
                if (!result.errors.isNullOrEmpty()) {
                    logger.debug { result.errors.toString() }

                    future.completeExceptionally(
                        RuntimeException("GraphQL errors: ${result.errors}"),
                    )
                } else {
                    future.complete(result.data?.trackEvent)
                }
            } catch (e: Exception) {
                logger.error(e) { "Error executing track query." }

                future.completeExceptionally(e)
            }
        }
        return future
    }

    /**
     * Retrieves an asset by its ID.
     *
     * @param id The asset ID
     * @return A CompletableFuture that will be completed with the asset result or completed
     *   exceptionally
     *
     * ```
     *         if there are GraphQL errors or execution errors
     * ```
     */
    fun getAsset(id: String): CompletableFuture<Asset?> {
        val future = CompletableFuture<Asset?>()
        val query = GetAsset(variables = GetAsset.Variables(id = id))

        coroutineScope.launch {
            try {
                val result = graphqlClient.execute(query)
                if (!result.errors.isNullOrEmpty()) {
                    logger.debug { result.errors.toString() }

                    future.completeExceptionally(
                        RuntimeException("GraphQL errors: ${result.errors}"),
                    )
                } else {
                    future.complete(result.data?.asset?.toAsset())
                }
            } catch (e: Exception) {
                logger.error(e) { "Error executing getAsset query." }

                future.completeExceptionally(e)
            }
        }
        return future
    }

    /**
     * Lists assets with optional filtering and pagination.
     *
     * @param filter Optional filter criteria
     * @param order Optional ordering criteria
     * @param limit Optional maximum number of results
     * @param page Optional page number for pagination
     * @return A CompletableFuture that will be completed with the list of assets or completed
     *   exceptionally
     *
     * ```
     *         if there are GraphQL errors or execution errors
     * ```
     */
    fun listAssets(
        filter: JsonObject? = null,
        order: List<JsonObject>? = null,
        limit: Int? = null,
        page: Int? = null,
    ): CompletableFuture<List<Asset>?> {
        val future = CompletableFuture<List<Asset>?>()
        val query =
            ListAssets(
                variables =
                    ListAssets.Variables(
                        filter = filter,
                        order = order,
                        limit = limit,
                        page = page,
                    ),
            )

        coroutineScope.launch {
            try {
                val result = graphqlClient.execute(query)
                if (!result.errors.isNullOrEmpty()) {
                    logger.debug { result.errors.toString() }

                    future.completeExceptionally(
                        RuntimeException("GraphQL errors: ${result.errors}"),
                    )
                } else {
                    future.complete(result.data?.assetsList?.map { it.toAsset() })
                }
            } catch (e: Exception) {
                logger.error(e) { "Error executing listAssets query." }

                future.completeExceptionally(e)
            }
        }
        return future
    }

    /**
     * Searches records for a given resource with optional search parameters.
     *
     * @param resource The resource identifier to search (e.g., "users", "products")
     * @param options Optional search parameters like filters, sorting, pagination
     * @return A CompletableFuture that will be completed with the search results or completed
     *   exceptionally
     *
     * ```
     *         if there are GraphQL errors or execution errors
     *
     * @example
     * ```kotlin
     * ```
     * // Kotlin usage dashX.searchRecords("users", options)
     * ```
     *
     * .thenAccept { result -> // Handle successful result } .exceptionally { error -> // Handle
     * error null }
     *
     * ```
     * ```
     *
     * @example
     *
     * ```java
     * // Java usage
     * dashX.searchRecords("users", options)
     *     .thenAccept(result -> {
     *         // Handle successful result
     *     })
     *     .exceptionally(error -> {
     *         // Handle error
     *         return null;
     *     });
     * ```
     */
    fun searchRecords(
        resource: String,
        options: SearchRecordsOptions? = null,
    ): CompletableFuture<List<JsonObject>> {
        val future = CompletableFuture<List<JsonObject>>()
        val input = options?.toInput(resource) ?: SearchRecordsInput(resource = resource)
        val query = SearchRecords(variables = SearchRecords.Variables(input = input))

        coroutineScope.launch {
            try {
                val result = graphqlClient.execute(query)
                if (!result.errors.isNullOrEmpty()) {
                    logger.debug { result.errors.toString() }

                    future.completeExceptionally(
                        RuntimeException("GraphQL errors: ${result.errors}"),
                    )
                } else {
                    future.complete(result.data?.searchRecords ?: emptyList())
                }
            } catch (e: Exception) {
                logger.error(e) { "Error executing searchRecords query." }

                future.completeExceptionally(e)
            }
        }
        return future
    }
}
