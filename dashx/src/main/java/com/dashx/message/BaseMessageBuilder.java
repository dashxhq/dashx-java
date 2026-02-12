package com.dashx.message;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.dashx.DashX;
import com.dashx.graphql.generated.types.Message;
import com.dashx.graphql.generated.types.SendMessageInput;
import com.dashx.graphql.generated.types.TemplateSubkind;

/**
 * Abstract base class for all message builders.
 * Provides common fields (conversationId, integrationId, templateId, data) and the
 * terminal {@link #send(DashX)} method that validates, builds the {@link SendMessageInput},
 * and delegates to {@link DashX#sendMessage(SendMessageInput)}.
 *
 * <p>Uses the self-type generic pattern so that fluent setter methods return the
 * concrete subclass type.</p>
 *
 * @param <T> the concrete builder type (self-type)
 */
public abstract class BaseMessageBuilder<T extends BaseMessageBuilder<T>> {

    protected String conversationId;
    protected String integrationId;
    protected String templateId;
    protected Map<String, Object> data;

    /**
     * Sets the conversation ID for the message.
     *
     * @param conversationId the conversation ID
     * @return this builder for fluent chaining
     */
    public T conversationId(String conversationId) {
        this.conversationId = conversationId;
        return self();
    }

    /**
     * Sets the integration ID for the message.
     *
     * @param integrationId the integration ID
     * @return this builder for fluent chaining
     */
    public T integrationId(String integrationId) {
        this.integrationId = integrationId;
        return self();
    }

    /**
     * Sets the template ID for the message.
     *
     * @param templateId the template ID
     * @return this builder for fluent chaining
     */
    public T templateId(String templateId) {
        this.templateId = templateId;
        return self();
    }

    /**
     * Sets additional data to include with the message.
     *
     * @param data a map of additional data
     * @return this builder for fluent chaining
     */
    public T data(Map<String, Object> data) {
        this.data = data;
        return self();
    }

    /**
     * Validates the builder state, builds a {@link SendMessageInput}, and sends the
     * message via the provided {@link DashX} client.
     *
     * @param dashX the configured DashX client instance
     * @return a CompletableFuture that will be completed with the sent Message
     */
    public CompletableFuture<Message> send(DashX dashX) {
        validate();

        SendMessageInput input = SendMessageInput.newBuilder()
                .conversationId(conversationId)
                .integrationId(integrationId)
                .templateSubkind(getTemplateSubkind())
                .content(buildContent())
                .templateId(templateId)
                .data(data)
                .build();

        return dashX.sendMessage(input);
    }

    /**
     * Validates the builder state. Implementations should throw
     * {@link com.dashx.exception.DashXValidationException} if required fields are missing
     * or invalid.
     */
    protected abstract void validate();

    /**
     * Builds the channel-specific content map that will be set as the {@code content}
     * field of {@link SendMessageInput}.
     *
     * @return the content map
     */
    protected abstract Map<String, Object> buildContent();

    /**
     * Returns the {@link TemplateSubkind} that corresponds to this message's channel.
     *
     * @return the template subkind
     */
    protected abstract TemplateSubkind getTemplateSubkind();

    /**
     * Returns {@code this} cast to the concrete type. Used by fluent setter methods.
     */
    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }
}
