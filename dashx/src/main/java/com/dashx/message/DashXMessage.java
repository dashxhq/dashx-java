package com.dashx.message;

/**
 * Entry point for building channel-specific messages.
 *
 * <p>Usage examples:</p>
 * <pre>{@code
 * // WhatsApp text
 * DashXMessage.whatsapp()
 *     .text("Hello!")
 *     .conversationId("conv-123")
 *     .send(dashX);
 *
 * // Email
 * DashXMessage.email()
 *     .subject("Welcome")
 *     .htmlBody("<h1>Hello</h1>")
 *     .conversationId("conv-123")
 *     .send(dashX);
 * }</pre>
 */
public final class DashXMessage {

    private DashXMessage() {
        // static-only entry point
    }

    /**
     * Starts building a WhatsApp message. Call one of the returned builder's
     * sub-type methods ({@code .text()}, {@code .template()}, {@code .interactive()})
     * to select the message type.
     *
     * @return a WhatsApp channel selector
     */
    public static WhatsAppMessageBuilder whatsapp() {
        return new WhatsAppMessageBuilder();
    }

    /**
     * Starts building an email message.
     *
     * @return an email message builder
     */
    public static EmailMessageBuilder email() {
        return new EmailMessageBuilder();
    }
}
