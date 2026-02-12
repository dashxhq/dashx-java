package com.dashx.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dashx.exception.DashXValidationException;
import com.dashx.graphql.generated.types.TemplateSubkind;

/**
 * Builder for email messages.
 *
 * <p>Produces content JSON matching the backend {@code EmailContent} struct (camelCase):</p>
 * <pre>{@code
 * {
 *   "from": "sender@example.com",
 *   "to": [{"email": "user@example.com"}],
 *   "cc": [{"email": "cc@example.com"}],
 *   "bcc": [{"email": "bcc@example.com"}],
 *   "replyTo": "reply@example.com",
 *   "subject": "Welcome!",
 *   "htmlBody": "<h1>Hello</h1>",
 *   "plainBody": "Hello",
 *   "inReplyTo": "<message-id>",
 *   "references": ["<ref1>", "<ref2>"]
 * }
 * }</pre>
 *
 * <p>Usage:</p>
 * <pre>{@code
 * DashXMessage.email()
 *     .subject("Welcome!")
 *     .htmlBody("<h1>Hello</h1>")
 *     .plainBody("Hello")
 *     .conversationId("conv-123")
 *     .send(dashX);
 * }</pre>
 */
public class EmailMessageBuilder extends BaseMessageBuilder<EmailMessageBuilder> {

    private String from;
    private List<Object> to;
    private List<Object> cc;
    private List<Object> bcc;
    private String replyTo;
    private String subject;
    private String htmlBody;
    private String plainBody;
    private String inReplyTo;
    private List<String> references;

    EmailMessageBuilder() {
    }

    /**
     * Sets the sender email address.
     *
     * @param from the sender address
     * @return this builder for fluent chaining
     */
    public EmailMessageBuilder from(String from) {
        this.from = from;
        return this;
    }

    /**
     * Sets the list of "To" recipients. Each element can be a string email address
     * or a map with structured recipient data (e.g. {@code Map.of("email", "user@example.com")}).
     *
     * @param to the list of recipients
     * @return this builder for fluent chaining
     */
    public EmailMessageBuilder to(List<Object> to) {
        this.to = to;
        return this;
    }

    /**
     * Sets the list of "CC" recipients.
     *
     * @param cc the list of CC recipients
     * @return this builder for fluent chaining
     */
    public EmailMessageBuilder cc(List<Object> cc) {
        this.cc = cc;
        return this;
    }

    /**
     * Sets the list of "BCC" recipients.
     *
     * @param bcc the list of BCC recipients
     * @return this builder for fluent chaining
     */
    public EmailMessageBuilder bcc(List<Object> bcc) {
        this.bcc = bcc;
        return this;
    }

    /**
     * Sets the Reply-To email address.
     *
     * @param replyTo the reply-to address
     * @return this builder for fluent chaining
     */
    public EmailMessageBuilder replyTo(String replyTo) {
        this.replyTo = replyTo;
        return this;
    }

    /**
     * Sets the email subject line.
     *
     * @param subject the subject
     * @return this builder for fluent chaining
     */
    public EmailMessageBuilder subject(String subject) {
        this.subject = subject;
        return this;
    }

    /**
     * Sets the HTML body of the email.
     *
     * @param htmlBody the HTML content
     * @return this builder for fluent chaining
     */
    public EmailMessageBuilder htmlBody(String htmlBody) {
        this.htmlBody = htmlBody;
        return this;
    }

    /**
     * Sets the plain-text body of the email. This is used as a fallback
     * when the recipient's email client does not support HTML rendering.
     *
     * @param plainBody the plain-text content
     * @return this builder for fluent chaining
     */
    public EmailMessageBuilder plainBody(String plainBody) {
        this.plainBody = plainBody;
        return this;
    }

    /**
     * Sets the In-Reply-To header value, used for email threading.
     *
     * @param inReplyTo the Message-ID of the email being replied to
     * @return this builder for fluent chaining
     */
    public EmailMessageBuilder inReplyTo(String inReplyTo) {
        this.inReplyTo = inReplyTo;
        return this;
    }

    /**
     * Sets the References header values, used for email threading.
     *
     * @param references list of Message-IDs in the thread
     * @return this builder for fluent chaining
     */
    public EmailMessageBuilder references(List<String> references) {
        this.references = references;
        return this;
    }

    @Override
    protected void validate() {
        if ((htmlBody == null || htmlBody.trim().isEmpty())
                && (plainBody == null || plainBody.trim().isEmpty())) {
            throw new DashXValidationException(
                    "Email must have at least one of htmlBody or plainBody");
        }
    }

    @Override
    protected Map<String, Object> buildContent() {
        Map<String, Object> content = new HashMap<>();
        if (from != null) {
            content.put("from", from);
        }
        if (to != null) {
            content.put("to", to);
        } else {
            content.put("to", new ArrayList<>());
        }
        if (cc != null) {
            content.put("cc", cc);
        }
        if (bcc != null) {
            content.put("bcc", bcc);
        }
        if (replyTo != null) {
            content.put("replyTo", replyTo);
        }
        if (subject != null) {
            content.put("subject", subject);
        }
        if (htmlBody != null) {
            content.put("htmlBody", htmlBody);
        }
        if (plainBody != null) {
            content.put("plainBody", plainBody);
        }
        if (inReplyTo != null) {
            content.put("inReplyTo", inReplyTo);
        }
        if (references != null) {
            content.put("references", references);
        }
        return content;
    }

    @Override
    protected TemplateSubkind getTemplateSubkind() {
        return TemplateSubkind.EMAIL;
    }
}
