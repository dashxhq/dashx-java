package com.dashx.message;

/**
 * Channel selector for WhatsApp messages.
 *
 * <p>This is <strong>not</strong> a message builder itself — it is a lightweight factory
 * that returns the appropriate sub-type builder based on the desired message type.</p>
 *
 * <p>Usage:</p>
 * <pre>{@code
 * DashXMessage.whatsapp()
 *     .text("Hello!")          // returns WhatsAppTextBuilder
 *     .conversationId("...")
 *     .send(dashX);
 *
 * DashXMessage.whatsapp()
 *     .template("order_update") // returns WhatsAppTemplateBuilder
 *     .language("en")
 *     .send(dashX);
 *
 * DashXMessage.whatsapp()
 *     .interactive()            // returns WhatsAppInteractiveBuilder
 *     .body("Pick one")
 *     .button("a", "Option A")
 *     .send(dashX);
 * }</pre>
 */
public class WhatsAppMessageBuilder {

    WhatsAppMessageBuilder() {
        // package-private — created by DashXMessage.whatsapp()
    }

    /**
     * Creates a WhatsApp text message builder with the given body.
     *
     * @param body the text message body
     * @return a WhatsApp text message builder
     */
    public WhatsAppTextBuilder text(String body) {
        return new WhatsAppTextBuilder(body);
    }

    /**
     * Creates a WhatsApp template message builder with the given template name.
     *
     * @param templateName the name of the WhatsApp message template
     * @return a WhatsApp template message builder
     */
    public WhatsAppTemplateBuilder template(String templateName) {
        return new WhatsAppTemplateBuilder(templateName);
    }

    /**
     * Creates a WhatsApp interactive message builder.
     *
     * @return a WhatsApp interactive message builder
     */
    public WhatsAppInteractiveBuilder interactive() {
        return new WhatsAppInteractiveBuilder();
    }
}
