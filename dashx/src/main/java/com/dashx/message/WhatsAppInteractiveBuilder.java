package com.dashx.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dashx.exception.DashXValidationException;
import com.dashx.graphql.generated.types.TemplateSubkind;

/**
 * Builder for WhatsApp interactive (button) messages.
 *
 * <p>Produces content JSON matching the backend {@code WhatsAppContent} struct:</p>
 * <pre>{@code
 * {
 *   "type": "interactive",
 *   "from": "15551234567",
 *   "interactive": {
 *     "type": "button",
 *     "body": { "text": "Choose an option" },
 *     "action": {
 *       "buttons": [
 *         { "type": "reply", "reply": { "id": "opt1", "title": "Option 1" } },
 *         { "type": "reply", "reply": { "id": "opt2", "title": "Option 2" } }
 *       ]
 *     }
 *   }
 * }
 * }</pre>
 *
 * <p>Usage:</p>
 * <pre>{@code
 * DashXMessage.whatsapp()
 *     .interactive()
 *     .body("Choose an option")
 *     .button("opt1", "Option 1")
 *     .button("opt2", "Option 2")
 *     .conversationId("conv-123")
 *     .send(dashX);
 * }</pre>
 */
public class WhatsAppInteractiveBuilder extends BaseMessageBuilder<WhatsAppInteractiveBuilder> {

    private static final int MAX_BUTTONS = 3;

    private String from;
    private String body;
    private final List<Map<String, Object>> buttons = new ArrayList<>();

    WhatsAppInteractiveBuilder() {
    }

    /**
     * Sets the sender phone number for the WhatsApp message.
     *
     * @param from the sender phone number
     * @return this builder for fluent chaining
     */
    public WhatsAppInteractiveBuilder from(String from) {
        this.from = from;
        return this;
    }

    /**
     * Sets the body text displayed above the buttons.
     *
     * @param body the body text
     * @return this builder for fluent chaining
     */
    public WhatsAppInteractiveBuilder body(String body) {
        this.body = body;
        return this;
    }

    /**
     * Adds a reply button to the interactive message.
     * WhatsApp allows a maximum of 3 buttons per interactive message.
     *
     * @param id a unique identifier for the button (returned when the user taps it)
     * @param title the display title of the button
     * @return this builder for fluent chaining
     */
    public WhatsAppInteractiveBuilder button(String id, String title) {
        buttons.add(Map.of(
                "type", "reply",
                "reply", Map.of("id", id, "title", title)));
        return this;
    }

    @Override
    protected void validate() {
        if (body == null || body.trim().isEmpty()) {
            throw new DashXValidationException(
                    "WhatsApp interactive message body cannot be null or empty");
        }
        if (buttons.isEmpty()) {
            throw new DashXValidationException(
                    "WhatsApp interactive message must have at least one button");
        }
        if (buttons.size() > MAX_BUTTONS) {
            throw new DashXValidationException(
                    "WhatsApp interactive message cannot have more than " + MAX_BUTTONS
                            + " buttons, got " + buttons.size());
        }
    }

    @Override
    protected Map<String, Object> buildContent() {
        Map<String, Object> content = new HashMap<>();
        content.put("type", "interactive");
        content.put("interactive", Map.of(
                "type", "button",
                "body", Map.of("text", body),
                "action", Map.of("buttons", List.copyOf(buttons))));
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
