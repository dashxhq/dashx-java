package com.dashx.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dashx.exception.DashXValidationException;
import com.dashx.graphql.generated.types.TemplateSubkind;

/**
 * Builder for WhatsApp template messages.
 *
 * <p>Produces content JSON matching the backend {@code WhatsAppContent} struct:</p>
 * <pre>{@code
 * {
 *   "type": "template",
 *   "from": "15551234567",
 *   "template_message": {
 *     "name": "order_update",
 *     "language": { "code": "en" },
 *     "components": [
 *       {
 *         "type": "body",
 *         "parameters": [
 *           { "type": "text", "text": "Order #123" },
 *           { "type": "text", "text": "$50.00" }
 *         ]
 *       }
 *     ]
 *   }
 * }
 * }</pre>
 *
 * <p>Usage:</p>
 * <pre>{@code
 * DashXMessage.whatsapp()
 *     .template("order_update")
 *     .language("en")
 *     .bodyParameters(List.of("Order #123", "$50.00"))
 *     .conversationId("conv-123")
 *     .send(dashX);
 * }</pre>
 */
public class WhatsAppTemplateBuilder extends BaseMessageBuilder<WhatsAppTemplateBuilder> {

    private final String templateName;
    private String from;
    private String language;
    private List<String> bodyParameters;
    private List<String> headerParameters;

    WhatsAppTemplateBuilder(String templateName) {
        this.templateName = templateName;
    }

    /**
     * Sets the sender phone number for the WhatsApp message.
     *
     * @param from the sender phone number
     * @return this builder for fluent chaining
     */
    public WhatsAppTemplateBuilder from(String from) {
        this.from = from;
        return this;
    }

    /**
     * Sets the language code for the template (e.g. "en", "es").
     *
     * @param language the BCP-47 language code
     * @return this builder for fluent chaining
     */
    public WhatsAppTemplateBuilder language(String language) {
        this.language = language;
        return this;
    }

    /**
     * Sets the body component parameters for the template.
     *
     * @param bodyParameters list of parameter values to substitute into the template body
     * @return this builder for fluent chaining
     */
    public WhatsAppTemplateBuilder bodyParameters(List<String> bodyParameters) {
        this.bodyParameters = bodyParameters;
        return this;
    }

    /**
     * Sets the header component parameters for the template.
     *
     * @param headerParameters list of parameter values to substitute into the template header
     * @return this builder for fluent chaining
     */
    public WhatsAppTemplateBuilder headerParameters(List<String> headerParameters) {
        this.headerParameters = headerParameters;
        return this;
    }

    @Override
    protected void validate() {
        if (templateName == null || templateName.trim().isEmpty()) {
            throw new DashXValidationException(
                    "WhatsApp template name cannot be null or empty");
        }
        if (language == null || language.trim().isEmpty()) {
            throw new DashXValidationException(
                    "WhatsApp template language cannot be null or empty");
        }
    }

    @Override
    protected Map<String, Object> buildContent() {
        List<Map<String, Object>> components = new ArrayList<>();

        if (headerParameters != null && !headerParameters.isEmpty()) {
            components.add(Map.of(
                    "type", "header",
                    "parameters", toTextParameters(headerParameters)));
        }

        if (bodyParameters != null && !bodyParameters.isEmpty()) {
            components.add(Map.of(
                    "type", "body",
                    "parameters", toTextParameters(bodyParameters)));
        }

        Map<String, Object> templateMessage = new HashMap<>();
        templateMessage.put("name", templateName);
        templateMessage.put("language", Map.of("code", language));
        if (!components.isEmpty()) {
            templateMessage.put("components", components);
        }

        Map<String, Object> content = new HashMap<>();
        content.put("type", "template");
        content.put("template_message", templateMessage);
        if (from != null) {
            content.put("from", from);
        }
        return content;
    }

    @Override
    protected TemplateSubkind getTemplateSubkind() {
        return TemplateSubkind.WHATSAPP;
    }

    private List<Map<String, Object>> toTextParameters(List<String> values) {
        return values.stream()
                .map(v -> Map.<String, Object>of("type", "text", "text", v))
                .toList();
    }
}
