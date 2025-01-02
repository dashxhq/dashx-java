package com.dashx

import com.dashx.graphql.generated.*
import com.dashx.graphql.generated.inputs.*
import com.dashx.util.Config
import com.expediagroup.graphql.client.serialization.GraphQLClientKotlinxSerializer
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.json.JSONObject

private val logger = KotlinLogging.logger {}

class DashX {
    companion object {
        // Setup variables
        private var baseURI: String? = null
        private var publicKey: String? = null
        private var privateKey: String? = null
        private var targetEnvironment: String? = null

        // Account variables
        private var accountAnonymousUid: String? = null
        private var accountUid: String? = null

        private val coroutineScope = CoroutineScope(Dispatchers.IO)

        fun configure(config: Config) {
            init(config)
        }

        private fun init(
            config: Config
        ) {
            this.baseURI = config.baseUri
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
            val httpClient = HttpClient(engineFactory = io.ktor.client.engine.okhttp.OkHttp) {
                engine {
                    config {
                        connectTimeout(10, TimeUnit.SECONDS)
                        readTimeout(60, TimeUnit.SECONDS)
                        writeTimeout(60, TimeUnit.SECONDS)
                    }
                }
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.NONE
                }

                defaultRequest {
                    publicKey?.let {
                        header("X-Public-Key", it)
                    }

                    privateKey?.let {
                        header("X-Private-Key", it)
                    }

                    targetEnvironment?.let {
                        header("X-Target-Environment", it)
                    }
                }
            }

            return DashXGraphQLKtorClient(
                url = URL(baseURI ?: "https://api.dashx.com/graphql"),
                httpClient = httpClient,
                serializer = GraphQLClientKotlinxSerializer()
            )
        }

        private fun generateAccountAnonymousUid(): String {
            return UUID.randomUUID().toString()
        }

        fun identify(options: HashMap<String, String>? = null) {
            if (options == null) {
                logger.debug { "'identify' cannot be called with null, please pass options of type 'object'." }
                return
            }

            val uid = if (options.containsKey(UserAttributes.UID)) {
                options[UserAttributes.UID]
            } else {
                this.accountUid
            }

            val anonymousUid = if (options.containsKey(UserAttributes.ANONYMOUS_UID)) {
                options[UserAttributes.ANONYMOUS_UID]
            } else {
                this.accountAnonymousUid
            }

            val query = IdentifyAccount(
                variables = IdentifyAccount.Variables(
                    IdentifyAccountInput(
                        uid = uid,
                        anonymousUid = anonymousUid,
                        email = options[UserAttributes.EMAIL],
                        phone = options[UserAttributes.PHONE],
                        name = options[UserAttributes.NAME],
                        firstName = options[UserAttributes.FIRST_NAME],
                        lastName = options[UserAttributes.LAST_NAME]
                    )
                )
            )

            coroutineScope.launch {
                val result = graphqlClient.execute(query)

                if (!result.errors.isNullOrEmpty()) {
                    val errors = result.errors?.toString() ?: ""
                    logger.debug { errors }
                    return@launch
                }

                logger.debug { result.data?.identifyAccount?.toString() }
            }

        }

        fun track(event: String, data: HashMap<String, String>? = hashMapOf()) {
            val jsonData =
                data?.toMap()?.let { Json.parseToJsonElement(JSONObject(it).toString()).jsonObject }

            val query = TrackEvent(
                variables = TrackEvent.Variables(
                    TrackEventInput(
                        accountAnonymousUid = accountAnonymousUid,
                        accountUid = accountUid,
                        data = jsonData,
                        event = event
                    )
                )
            )

            coroutineScope.launch {
                val result = graphqlClient.execute(query)

                if (!result.errors.isNullOrEmpty()) {
                    val errors = result.errors?.toString()
                    logger.debug { errors }
                    return@launch
                }

                logger.debug { result.data?.trackEvent?.toString() }
            }
        }
    }
}
