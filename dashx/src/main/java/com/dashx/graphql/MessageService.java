package com.dashx.graphql;

import com.dashx.DashXGraphQLClient;
import com.dashx.graphql.generated.types.Message;
import com.dashx.graphql.generated.types.SendMessageInput;
import java.util.Map;
import reactor.core.publisher.Mono;

/**
 * Service class for message operations.
 * Handles sending messages through the DashX GraphQL API.
 */
public class MessageService {
    private final DashXGraphQLClient client;
    private final String fullMessageProjection;

    /**
     * Constructs a new MessageService with the specified GraphQL client.
     *
     * @param client the GraphQL client to use for executing queries and mutations
     */
    public MessageService(DashXGraphQLClient client) {
        this.client = client;
        this.fullMessageProjection = """
                {
                    id
                    integrationId
                    broadcastId
                    conversationId
                    senderId
                    channel
                    subChannel
                    renderedContent
                    data
                    attachments
                    createdAt
                    updatedAt
                }
                """;
    }

    /**
     * Sends a message with the specified details.
     *
     * @param input the message input containing conversation, integration, template, content, and data details
     * @return a Mono that emits the sent Message object with all its fields populated
     */
    public Mono<Message> sendMessage(SendMessageInput input) {
        String query =
                "mutation SendMessage($input: SendMessageInput!) { sendMessage(input: $input) "
                        + this.fullMessageProjection + " }";

        Map<String, Object> variables = Map.of("input", input);

        return client.execute(query, variables)
                .map(response -> response.extractValueAsObject("sendMessage", Message.class));
    }
}
