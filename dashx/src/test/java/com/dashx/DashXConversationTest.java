package com.dashx;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.dashx.exception.DashXValidationException;
import com.dashx.graphql.ConversationService;
import com.dashx.graphql.generated.types.Conversation;
import com.dashx.graphql.generated.types.StartInAppChatConversationInput;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests for In-App Chat conversation creation.
 *
 * <p>Two concerns, deliberately separated: the {@link ConversationService} tests assert what
 * actually goes over the wire (both identity fields forwarded, correct mutation name), and the
 * {@link DashX} tests assert the edge validation that stops a malformed start before a round
 * trip.
 */
@ExtendWith(MockitoExtension.class)
class DashXConversationTest {

    @Mock
    private DashXGraphQLClient mockClient;

    @Mock
    private GraphQLResponse mockResponse;

    private DashX dashx;

    @BeforeEach
    void setUp() {
        DashX.resetInstances();
        dashx = DashX.getInstance("test-conversation");
        dashx.configure(
            new DashXConfig.Builder()
                // A deliberately unroutable base URL. `DashXConfig` defaults to
                // https://api.dashx.com/graphql, so a regression that let a request past
                // validation would fire a unit test at PRODUCTION with fake credentials. Pointing
                // at a closed local port makes that impossible rather than merely unlikely.
                .baseUrl("http://127.0.0.1:1/graphql")
                .publicKey("test-public-key")
                .privateKey("test-private-key")
                .targetEnvironment("test")
                .build()
        );
    }

    @AfterEach
    void tearDown() {
        DashX.resetInstances();
    }

    private static StartInAppChatConversationInput validInput() {
        StartInAppChatConversationInput input =
            new StartInAppChatConversationInput();
        input.setIdentityId("11111111-1111-4111-8111-111111111111");
        input.setAccountUid("visitor-uid-1");
        input.setClientIdempotencyKey("order-OMS_ORDER-12345678901-abc");
        return input;
    }

    // ---- wire contract ----

    @Test
    void startForwardsBothIdentityFieldsAndReturnsTheConversation() {
        Conversation conversation = new Conversation();
        conversation.setId("22222222-2222-4222-8222-222222222222");

        when(
            mockResponse.extractValueAsObject(
                "startInAppChatConversation",
                Conversation.class
            )
        ).thenReturn(conversation);
        when(mockClient.execute(anyString(), any())).thenReturn(
            Mono.just(mockResponse)
        );

        ConversationService service = new ConversationService(mockClient);
        StartInAppChatConversationInput input = validInput();

        StepVerifier.create(service.startInAppChatConversation(input))
            .expectNextMatches(result ->
                "22222222-2222-4222-8222-222222222222".equals(result.getId())
            )
            .verifyComplete();

        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(
            String.class
        );
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> variablesCaptor =
            ArgumentCaptor.forClass(Map.class);
        verify(mockClient).execute(
            queryCaptor.capture(),
            variablesCaptor.capture()
        );

        String query = queryCaptor.getValue();
        assertTrue(
            query.contains("mutation StartInAppChatConversation"),
            "must call the server-only start mutation"
        );
        assertTrue(
            query.contains("$input: StartInAppChatConversationInput!"),
            "input must be declared non-null"
        );

        // The whole input is forwarded as one variable — both identity fields must survive,
        // because they are different things and sending one for the other resolves the wrong
        // owner (identityId = Chat Identity, accountUid = the visitor).
        StartInAppChatConversationInput forwarded =
            (StartInAppChatConversationInput) variablesCaptor
                .getValue()
                .get("input");
        assertSame(input, forwarded);
        assertEquals(
            "11111111-1111-4111-8111-111111111111",
            forwarded.getIdentityId()
        );
        assertEquals("visitor-uid-1", forwarded.getAccountUid());
        assertEquals(
            "order-OMS_ORDER-12345678901-abc",
            forwarded.getClientIdempotencyKey()
        );
    }

    @Test
    void startForwardsTheOptionalMessagePayloadUnchanged() {
        Conversation conversation = new Conversation();
        conversation.setId("33333333-3333-4333-8333-333333333333");

        when(
            mockResponse.extractValueAsObject(
                "startInAppChatConversation",
                Conversation.class
            )
        ).thenReturn(conversation);
        when(mockClient.execute(anyString(), any())).thenReturn(
            Mono.just(mockResponse)
        );

        StartInAppChatConversationInput input = validInput();
        input.setContent(Map.of("text", "I need help with this order"));
        input.setClientMessageId("client-message-1");
        input.setData(Map.of("category", "contextual"));
        input.setIssueProperties(Map.of("order-id", "12345678901"));

        ConversationService service = new ConversationService(mockClient);

        StepVerifier.create(service.startInAppChatConversation(input))
            .expectNextCount(1)
            .verifyComplete();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> variablesCaptor =
            ArgumentCaptor.forClass(Map.class);
        verify(mockClient).execute(anyString(), variablesCaptor.capture());

        StartInAppChatConversationInput forwarded =
            (StartInAppChatConversationInput) variablesCaptor
                .getValue()
                .get("input");
        assertEquals(
            Map.of("text", "I need help with this order"),
            forwarded.getContent()
        );
        assertEquals("client-message-1", forwarded.getClientMessageId());
        assertEquals(Map.of("category", "contextual"), forwarded.getData());
        assertEquals(
            Map.of("order-id", "12345678901"),
            forwarded.getIssueProperties()
        );
    }

    @Test
    void startRequestsTheConversationIdInTheProjection() {
        Conversation conversation = new Conversation();
        conversation.setId("44444444-4444-4444-8444-444444444444");

        when(
            mockResponse.extractValueAsObject(
                "startInAppChatConversation",
                Conversation.class
            )
        ).thenReturn(conversation);
        when(mockClient.execute(anyString(), any())).thenReturn(
            Mono.just(mockResponse)
        );

        ConversationService service = new ConversationService(mockClient);
        service.startInAppChatConversation(validInput()).block();

        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(
            String.class
        );
        verify(mockClient).execute(queryCaptor.capture(), any());

        // The id is the one field callers cannot do without — it is what gets handed to the
        // visitor's client. `idempotencyKey` is asserted too: it carries the ownership prefix
        // and is the handle for diagnosing a mis-owned conversation.
        //
        // Both are matched as WHOLE selections on their own line. A `contains("id")` would pass
        // vacuously — "idempotencyKey" starts with those two characters — so it would still be
        // green with no id in the projection at all.
        String query = queryCaptor.getValue();
        assertTrue(
            Pattern.compile("^\\s*id\\s*$", Pattern.MULTILINE)
                .matcher(query)
                .find(),
            "projection must select id as its own field: " + query
        );
        assertTrue(
            Pattern.compile("^\\s*idempotencyKey\\s*$", Pattern.MULTILINE)
                .matcher(query)
                .find(),
            "projection must select idempotencyKey as its own field: " + query
        );
    }

    // ---- edge validation (no round trip) ----

    private void assertRejected(
        StartInAppChatConversationInput input,
        String expectedMessageFragment
    ) {
        CompletableFuture<Conversation> future =
            dashx.startInAppChatConversation(input);

        // Bounded wait: a rejected input completes the future immediately, so needing the
        // timeout at all means validation was skipped.
        ExecutionException exception = assertThrows(
            ExecutionException.class,
            () -> future.get(5, TimeUnit.SECONDS)
        );
        assertInstanceOf(
            DashXValidationException.class,
            exception.getCause(),
            "must fail validation, not reach the server"
        );
        assertTrue(
            exception.getCause().getMessage().contains(
                expectedMessageFragment
            ),
            "expected message to contain '" +
            expectedMessageFragment +
            "' but was: " +
            exception.getCause().getMessage()
        );
    }

    @Test
    void nullInputIsRejected() {
        assertRejected(null, "cannot be null");
    }

    @Test
    void missingIdentityIdIsRejected() {
        StartInAppChatConversationInput input = validInput();
        input.setIdentityId(null);
        assertRejected(input, "identityId");

        input.setIdentityId("   ");
        assertRejected(input, "identityId");
    }

    @Test
    void missingAccountUidIsRejected() {
        StartInAppChatConversationInput input = validInput();
        input.setAccountUid(null);
        assertRejected(input, "accountUid");

        input.setAccountUid("  ");
        assertRejected(input, "accountUid");
    }

    @Test
    void missingClientIdempotencyKeyIsRejected() {
        StartInAppChatConversationInput input = validInput();
        input.setClientIdempotencyKey(null);
        assertRejected(input, "clientIdempotencyKey");
    }

    @Test
    void contentWithoutAClientMessageIdIsRejected() {
        // Both ride the first message, so a retry needs a stable message id to collapse on.
        StartInAppChatConversationInput input = validInput();
        input.setContent(Map.of("text", "hello"));
        assertRejected(input, "clientMessageId is required when content");
    }

    @Test
    void metadataWithoutContentIsRejected() {
        // `data` and `issueProperties` are carried by the first message, so a message-less start
        // would silently drop them — the server rejects this and so do we, earlier.
        StartInAppChatConversationInput withData = validInput();
        withData.setData(Map.of("category", "general"));
        assertRejected(withData, "content (and clientMessageId) is required");

        StartInAppChatConversationInput withProperties = validInput();
        withProperties.setIssueProperties(Map.of("order-id", "1"));
        assertRejected(
            withProperties,
            "content (and clientMessageId) is required"
        );
    }

    @Test
    void aFullyPopulatedInputPassesValidation() {
        // Guards against an over-eager guard: the valid shape must NOT be rejected.
        //
        // Asserted against the validator DIRECTLY, never by calling the facade: `configure()`
        // with no baseUrl points at https://api.dashx.com/graphql, so letting a valid input
        // through to the client would make this unit test fire a real request at PRODUCTION
        // with fake credentials and then block on `get()` with no timeout.
        StartInAppChatConversationInput input = validInput();
        input.setContent(Map.of("text", "hello"));
        input.setClientMessageId("client-message-1");
        input.setData(Map.of("category", "contextual"));
        input.setIssueProperties(Map.of("order-id", "12345678901"));

        assertDoesNotThrow(() ->
            DashX.validateStartInAppChatConversationInput(input)
        );
    }

    @Test
    void aMinimalInputPassesValidation() {
        assertDoesNotThrow(() ->
            DashX.validateStartInAppChatConversationInput(validInput())
        );
    }

    @Test
    void emptyIssuePropertiesCountAsOmitted() {
        // The server normalizes an EMPTY properties object to "absent" before deciding whether
        // content is required, so `{}` on a content-less start is valid there. Rejecting it here
        // would make the SDK stricter than DashX.
        StartInAppChatConversationInput input = validInput();
        input.setIssueProperties(Map.of());

        assertDoesNotThrow(() ->
            DashX.validateStartInAppChatConversationInput(input)
        );
    }

    @Test
    void emptyDataStillRequiresContent() {
        // The asymmetric half: the server tests `data` on the raw option, so an empty `data` map
        // IS "supplied" and does require content. Mirror that rather than tidying it away.
        StartInAppChatConversationInput input = validInput();
        input.setData(Map.of());

        DashXValidationException e = assertThrows(
            DashXValidationException.class,
            () -> DashX.validateStartInAppChatConversationInput(input)
        );
        assertTrue(
            e.getMessage().contains("content (and clientMessageId) is required")
        );
    }
}
