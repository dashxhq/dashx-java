package com.dashx.message;

import java.util.HashMap;
import java.util.Map;

import com.dashx.exception.DashXValidationException;
import com.dashx.graphql.generated.types.TemplateSubkind;

/**
 * Builder for WhatsApp text messages.
 *
 * <p>Produces content JSON matching the backend {@code WhatsAppContent} struct:</p>
 * <pre>{@code
 * {
 *   "type": "text",
 *   "from": "15551234567",          // optional
 *   "text": { "body": "Hello World" }
 * }
 * }</pre>
 *
 * <p>Usage:</p>
 * <pre>{@code
 * DashXMessage.whatsapp()
 *     .text("Hello World!")
 *     .conversationId("conv-123")
 *     .send(dashX);
 * }</pre>
 */
public class WhatsAppTextBuilder extends BaseMessageBuilder<WhatsAppTextBuilder> {

    private final String body;
    private String from;

    WhatsAppTextBuilder(String body) {
        this.body = body;
    }

    /**
     * Sets the sender phone number for the WhatsApp message.
     *
     * @param from the sender phone number
     * @return this builder for fluent chaining
     */
    public WhatsAppTextBuilder from(String from) {
        this.from = from;
        return this;
    }

    @Override
    protected void validate() {
        if (body == null || body.trim().isEmpty()) {
            throw new DashXValidationException(
                    "WhatsApp text message body cannot be null or empty");
        }
    }

    @Override
    protected Map<String, Object> buildContent() {
        Map<String, Object> content = new HashMap<>();
        content.put("type", "text");
        content.put("text", Map.of("body", body));
        if (from != null) {
            content.put("from", from);
        }
        return content;
    }

    @Override
    protected TemplateSubkind getTemplateSubkind() {
        return TemplateSubkind.WHATSAPP;
    }
}
