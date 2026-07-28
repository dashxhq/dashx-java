package com.dashx.graphql;

import com.dashx.DashXGraphQLClient;
import com.dashx.graphql.generated.types.Conversation;
import com.dashx.graphql.generated.types.StartInAppChatConversationInput;
import java.util.Map;
import reactor.core.publisher.Mono;

/**
 * Service class for conversation operations.
 *
 * <p>Currently covers In-App Chat conversation creation. Creating a chat conversation is a
 * <strong>server-only</strong> operation: DashX rejects identity-token callers, so it is
 * authenticated with the workspace public/private key pair this SDK already sends. Visitors
 * (browsers, apps) participate in a conversation your backend created for them — they never
 * create one themselves, which is what keeps server-derived metadata out of client hands.
 */
public class ConversationService {

    private final DashXGraphQLClient client;
    private final String fullConversationProjection;

    /**
     * Constructs a new ConversationService with the specified GraphQL client.
     *
     * @param client the GraphQL client to use for executing queries and mutations
     */
    public ConversationService(DashXGraphQLClient client) {
        this.client = client;
        this.fullConversationProjection =
            """
            {
                id
                workspaceId
                environmentId
                kind
                channel
                subChannel
                name
                idempotencyKey
                lastMessageAt
                isTest
                createdAt
                updatedAt
            }
            """;
    }

    /**
     * Starts (or idempotently re-finds) an In-App Chat conversation for a visitor.
     *
     * <p>Retrying the same logical start with the same {@code clientIdempotencyKey} returns the
     * same conversation instead of creating a second one, so a network failure can be retried
     * safely.
     *
     * @param input the start input; {@code identityId} (the workspace Chat Identity) and
     *              {@code accountUid} (the visitor's account uid in the target environment) are
     *              both required and are different values
     * @return a Mono that emits the created or re-found Conversation, whose {@code id} is the
     *         conversation id to hand to the visitor's client
     */
    public Mono<Conversation> startInAppChatConversation(
        StartInAppChatConversationInput input
    ) {
        String query =
            "mutation StartInAppChatConversation($input: StartInAppChatConversationInput!) { startInAppChatConversation(input: $input) " +
            this.fullConversationProjection +
            " }";

        Map<String, Object> variables = Map.of("input", input);

        return client
            .execute(query, variables)
            .map(response ->
                response.extractValueAsObject(
                    "startInAppChatConversation",
                    Conversation.class
                )
            );
    }
}
