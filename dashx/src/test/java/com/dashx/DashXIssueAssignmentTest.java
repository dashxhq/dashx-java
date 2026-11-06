package com.dashx;

import static org.junit.jupiter.api.Assertions.*;

import com.dashx.exception.DashXException;
import com.dashx.exception.DashXValidationException;
import com.dashx.graphql.generated.types.Account;
import com.dashx.graphql.generated.types.AccountKind;
import com.dashx.graphql.generated.types.Group;
import com.dashx.graphql.generated.types.Issue;
import com.dashx.graphql.generated.types.IssuePriority;
import com.dashx.graphql.utils.UpdateIssueInput;
import com.dashx.model.IssueActivity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Issue assignment support: sparse {@code updateIssue}, the assignee projection, member and
 * group-member lookups, and issue activity.
 *
 * <p>Wire-level tests run the real {@link DashXGraphQLClient} against a local stub server, so they
 * assert the JSON that actually leaves the SDK. That matters most for {@code updateIssue}, where an
 * explicit {@code null} clears a field on the server.
 */
class DashXIssueAssignmentTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ISSUE_ID = "11111111-1111-4111-8111-111111111111";
    private static final String GROUP_ID = "22222222-2222-4222-8222-222222222222";

    private HttpServer server;
    private final List<JsonNode> requests = Collections.synchronizedList(new ArrayList<>());
    private volatile Function<JsonNode, String> responder = request -> "{\"data\":{}}";
    private DashX dashx;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            JsonNode body = MAPPER.readTree(exchange.getRequestBody().readAllBytes());
            requests.add(body);
            byte[] response = responder.apply(body).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();

        DashX.resetInstances();
        dashx = DashX.getInstance("test-issue-assignment");
        dashx.configure(
            new DashXConfig.Builder()
                .baseUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/graphql")
                .publicKey("test-public-key")
                .privateKey("test-private-key")
                .targetEnvironment("test")
                .build()
        );
    }

    @AfterEach
    void tearDown() {
        DashX.resetInstances();
        server.stop(0);
    }

    private static <T> T await(java.util.concurrent.CompletableFuture<T> future) throws Exception {
        return future.get(10, TimeUnit.SECONDS);
    }

    private static String issueResponse(String field) {
        return "{\"data\":{\"" + field + "\":{\"id\":\"" + ISSUE_ID + "\",\"title\":\"t\"," +
            "\"assigneeId\":null,\"assignee\":null}}}";
    }

    // ---- UpdateIssueInput ----

    @Test
    void updateInputSendsOnlyTheFieldsThatWereSet() {
        UpdateIssueInput input = UpdateIssueInput.newBuilder(ISSUE_ID)
            .issueStatus("Approved")
            .build();

        assertEquals(Map.of("id", ISSUE_ID, "issueStatus", "Approved"), input.toVariables());
        assertFalse(input.has("assigneeId"));
    }

    @Test
    void clearAssigneeSendsAnExplicitNull() {
        UpdateIssueInput input = UpdateIssueInput.newBuilder(ISSUE_ID)
            .clearAssignee()
            .issueStatus("Approved")
            .build();

        Map<String, Object> variables = input.toVariables();
        assertTrue(variables.containsKey("assigneeId"));
        assertNull(variables.get("assigneeId"));
        assertEquals(3, variables.size());
    }

    @Test
    void passingNullToASetterIsRejectedRatherThanReadAsClear() {
        UpdateIssueInput.Builder builder = UpdateIssueInput.newBuilder(ISSUE_ID);

        DashXValidationException e = assertThrows(
            DashXValidationException.class,
            () -> builder.assigneeId(null)
        );
        assertTrue(e.getMessage().contains("clear"));
    }

    @Test
    void anUpdateWithNoChangesIsRejected() {
        assertThrows(
            DashXValidationException.class,
            () -> UpdateIssueInput.newBuilder(ISSUE_ID).build()
        );
    }

    @Test
    void aBlankIssueIdIsRejected() {
        assertThrows(DashXValidationException.class, () -> UpdateIssueInput.newBuilder(" "));
        assertThrows(DashXValidationException.class, () -> UpdateIssueInput.newBuilder(null));
    }

    // ---- updateIssue on the wire ----

    @Test
    void updateIssueSendsExactlyTheSetFieldsOverTheWire() throws Exception {
        responder = request -> issueResponse("updateIssue");

        Issue issue = await(
            dashx.updateIssue(
                UpdateIssueInput.newBuilder(ISSUE_ID)
                    .clearAssignee()
                    .issueStatus("Approved")
                    .build()
            )
        );

        assertEquals(ISSUE_ID, issue.getId());
        JsonNode request = requests.get(0);
        assertTrue(request.get("query").asText().contains("updateIssue(input: $input)"));

        JsonNode input = request.get("variables").get("input");
        List<String> keys = new ArrayList<>();
        input.fieldNames().forEachRemaining(keys::add);
        assertEquals(List.of("id", "assigneeId", "issueStatus"), keys);
        assertTrue(input.get("assigneeId").isNull(), "clearAssignee must send JSON null");
        assertEquals("Approved", input.get("issueStatus").asText());
    }

    @Test
    void anUnsetAssigneeIsNeverSentEvenAsNull() throws Exception {
        responder = request -> issueResponse("updateIssue");

        await(
            dashx.updateIssue(
                UpdateIssueInput.newBuilder(ISSUE_ID)
                    .priority(IssuePriority.HIGH)
                    .build()
            )
        );

        JsonNode input = requests.get(0).get("variables").get("input");
        assertFalse(input.has("assigneeId"));
        assertFalse(input.has("description"));
        assertFalse(input.has("dueAt"));
        assertEquals("HIGH", input.get("priority").asText());
    }

    @Test
    void updateIssueRejectsNullInput() {
        ExecutionException e = assertThrows(
            ExecutionException.class,
            () -> await(dashx.updateIssue(null))
        );
        assertInstanceOf(DashXValidationException.class, e.getCause());
        assertTrue(requests.isEmpty());
    }

    // ---- assignee projection ----

    @Test
    void issueQueriesRequestTheNestedAssignee() throws Exception {
        responder = request ->
            "{\"data\":{\"issue\":{\"id\":\"" + ISSUE_ID + "\",\"title\":\"t\"," +
            "\"assigneeId\":\"33333333-3333-4333-8333-333333333333\"," +
            "\"assignee\":{\"id\":\"33333333-3333-4333-8333-333333333333\"," +
            "\"kind\":\"MEMBER\",\"email\":\"jane@example.com\",\"name\":\"Jane Doe\"}}}}";

        Issue issue = await(dashx.getIssue(ISSUE_ID));

        String query = requests.get(0).get("query").asText();
        assertTrue(query.matches("(?s).*assignee \\{.*email.*name.*\\}.*"), query);
        assertEquals("Jane Doe", issue.getAssignee().getName());
        assertEquals("jane@example.com", issue.getAssignee().getEmail());
        assertEquals(AccountKind.MEMBER, issue.getAssignee().getKind());
    }

    // ---- findMemberByEmail ----

    @Test
    void findMemberByEmailFiltersOnMemberKindInTheQuery() throws Exception {
        responder = request ->
            "{\"data\":{\"accountsList\":[" +
            "{\"id\":\"55555555-5555-4555-8555-555555555555\",\"kind\":\"MEMBER\",\"email\":\"jane@example.com\"}" +
            "]}}";

        Optional<Account> member = await(dashx.findMemberByEmail(" jane@example.com "));

        assertTrue(member.isPresent());
        assertEquals("55555555-5555-4555-8555-555555555555", member.get().getId());
        // Users and visitors can share the email; filtering on kind server-side means any number
        // of them can't crowd the member out of the result.
        JsonNode variables = requests.get(0).get("variables");
        assertEquals("jane@example.com", variables.get("filter").get("email").get("eq").asText());
        assertEquals("MEMBER", variables.get("filter").get("kind").get("eq").asText());
        assertEquals(1, variables.get("limit").asInt());
    }

    @Test
    void findMemberByEmailIsEmptyWhenNoMemberMatches() throws Exception {
        responder = request -> "{\"data\":{\"accountsList\":[]}}";

        assertTrue(await(dashx.findMemberByEmail("jane@example.com")).isEmpty());
    }

    @Test
    void findMemberByEmailRejectsBlankEmail() {
        ExecutionException e = assertThrows(
            ExecutionException.class,
            () -> await(dashx.findMemberByEmail("  "))
        );
        assertInstanceOf(DashXValidationException.class, e.getCause());
    }

    // ---- listGroupMembers ----

    private static String accountsPage(int start, int count) {
        StringBuilder accounts = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) accounts.append(',');
            accounts.append(
                String.format(
                    "{\"id\":\"00000000-0000-4000-8000-%012d\",\"kind\":\"MEMBER\"," +
                    "\"createdAt\":\"2026-11-05T10:00:00.000000Z\"}",
                    start + i
                )
            );
        }
        return "{\"data\":{\"accountsList\":[" + accounts + "]}}";
    }

    @Test
    void listGroupMembersByIdFiltersOnGroupIdAndPagesUntilAShortPage() throws Exception {
        responder = request -> {
            if (request.get("query").asText().contains("groupsList")) {
                return memberGroupResponse();
            }
            return request.get("variables").get("filter").has("createdAt")
                ? accountsPage(100, 7)
                : accountsPage(0, 100);
        };

        List<Account> members = await(dashx.listGroupMembers(GROUP_ID));

        assertEquals(107, members.size());
        assertEquals(3, requests.size());
        // The id is looked up first so its kind can be checked.
        assertEquals(GROUP_ID, requests.get(0).get("variables").get("filter").get("id").get("eq").asText());
        requests.remove(0);
        JsonNode first = requests.get(0).get("variables");
        assertEquals(GROUP_ID, first.get("filter").get("groupId").get("eq").asText());
        assertEquals(100, first.get("limit").asInt());
        assertFalse(first.has("page"), "pages by cursor, not offset");
        // Staging rejects `{"id": "asc"}` ("`id` is not orderable"); order by createdAt only.
        assertEquals(1, first.get("order").size());
        assertEquals("asc", first.get("order").get(0).get("createdAt").asText());

        // The next request resumes at the last timestamp and excludes the 100 ids read there.
        JsonNode second = requests.get(1).get("variables").get("filter");
        assertEquals(GROUP_ID, second.get("groupId").get("eq").asText());
        assertEquals("2026-11-05T10:00:00.000000Z", second.get("createdAt").get("gte").asText());
        assertEquals(100, second.get("and").size());
        assertEquals(
            "00000000-0000-4000-8000-000000000000",
            second.get("and").get(0).get("id").get("neq").asText()
        );
    }

    private static String memberGroupResponse() {
        return "{\"data\":{\"groupsList\":[" +
            "{\"id\":\"" + GROUP_ID + "\",\"name\":\"Adjudicators\",\"accountKind\":\"MEMBER\"}" +
            "]}}";
    }

    @Test
    void listGroupMembersRejectsTheIdOfAUserGroup() {
        responder = request ->
            "{\"data\":{\"groupsList\":[" +
            "{\"id\":\"" + GROUP_ID + "\",\"name\":\"Customers\",\"accountKind\":\"USER\"}]}}";

        ExecutionException e = assertThrows(
            ExecutionException.class,
            () -> await(dashx.listGroupMembers(GROUP_ID))
        );
        assertInstanceOf(DashXException.class, e.getCause());
        assertTrue(e.getCause().getMessage().contains("USER accounts, not members"));
        assertEquals(1, requests.size(), "no accounts are listed for a non-member group");
    }

    @Test
    void listGroupMembersRejectsAnUnknownId() {
        responder = request -> "{\"data\":{\"groupsList\":[]}}";

        ExecutionException e = assertThrows(
            ExecutionException.class,
            () -> await(dashx.listGroupMembers(GROUP_ID))
        );
        assertTrue(e.getCause().getMessage().contains("No member group with id"));
    }

    @Test
    void listGroupMembersByNameResolvesTheMemberGroupFirst() throws Exception {
        responder = request -> {
            String query = request.get("query").asText();
            if (query.contains("groupsList")) {
                return memberGroupResponse();
            }
            return accountsPage(0, 3);
        };

        List<Account> members = await(dashx.listGroupMembers("Adjudicators"));

        assertEquals(3, members.size());
        assertEquals(
            "Adjudicators",
            requests.get(0).get("variables").get("filter").get("name").get("eq").asText()
        );
        assertEquals(
            GROUP_ID,
            requests.get(1).get("variables").get("filter").get("groupId").get("eq").asText()
        );
    }

    private static Group group(String id, AccountKind kind) {
        Group group = new Group();
        group.setId(id);
        group.setAccountKind(kind);
        return group;
    }

    @Test
    void resolvingAGroupNameIgnoresNonMemberGroups() {
        assertEquals(
            GROUP_ID,
            DashX.resolveMemberGroupId(
                "Adjudicators",
                false,
                List.of(
                    group("66666666-6666-4666-8666-666666666666", AccountKind.USER),
                    group(GROUP_ID, AccountKind.MEMBER)
                )
            )
        );
    }

    @Test
    void resolvingAnUnknownOrAmbiguousGroupNameFails() {
        DashXException missing = assertThrows(
            DashXException.class,
            () -> DashX.resolveMemberGroupId("Nope", false, List.of())
        );
        assertTrue(missing.getMessage().contains("No member group"));

        DashXException ambiguous = assertThrows(
            DashXException.class,
            () ->
                DashX.resolveMemberGroupId(
                    "Dup",
                    false,
                    List.of(
                        group(GROUP_ID, AccountKind.MEMBER),
                        group("77777777-7777-4777-8777-777777777777", AccountKind.MEMBER)
                    )
                )
        );
        assertTrue(ambiguous.getMessage().contains("pass the group id"));
    }

    // ---- getIssueActivity ----

    private static final String ACTOR = "a0000000-0000-4000-8000-000000000001";
    private static final String JANE = "a0000000-0000-4000-8000-000000000002";
    private static final String RAJ = "a0000000-0000-4000-8000-000000000003";
    private static final String OPEN = "b0000000-0000-4000-8000-000000000001";
    private static final String APPROVED = "b0000000-0000-4000-8000-000000000002";
    private static final String CARE = "c0000000-0000-4000-8000-000000000001";

    private static String activity(String id, String verb, String field, String metadata) {
        return "{\"id\":\"" + id + "\",\"actorType\":\"ACCOUNT\",\"actorId\":\"" + ACTOR + "\"," +
            "\"verb\":\"" + verb + "\",\"field\":" + (field == null ? "null" : "\"" + field + "\"") +
            ",\"metadata\":" + metadata + ",\"createdAt\":\"2026-11-05T10:00:00Z\"}";
    }

    private static String historyResponder(JsonNode request, String activities) {
        String query = request.get("query").asText();
        if (query.contains("activitiesList")) {
            return "{\"data\":{\"activitiesList\":[" + activities + "]}}";
        }
        if (query.contains("accountsList")) {
            return "{\"data\":{\"accountsList\":[" +
                "{\"id\":\"" + ACTOR + "\",\"kind\":\"SERVICE\",\"name\":\"Integration backend\"}," +
                "{\"id\":\"" + JANE + "\",\"kind\":\"MEMBER\",\"name\":\"Jane\"}," +
                "{\"id\":\"" + RAJ + "\",\"kind\":\"MEMBER\",\"name\":\"Raj\"}]}}";
        }
        if (query.contains("issueStatusesList")) {
            return "{\"data\":{\"issueStatusesList\":[" +
                "{\"id\":\"" + OPEN + "\",\"name\":\"Open\",\"kind\":\"NOT_STARTED\"}," +
                "{\"id\":\"" + APPROVED + "\",\"name\":\"Approved\",\"kind\":\"COMPLETED\"}]}}";
        }
        if (query.contains("groupsList")) {
            return "{\"data\":{\"groupsList\":[" +
                "{\"id\":\"" + CARE + "\",\"name\":\"Support Team\",\"accountKind\":\"MEMBER\"}]}}";
        }
        return "{\"data\":{}}";
    }

    @Test
    void getIssueActivityResolvesActorsAssigneesStatusesAndGroups() throws Exception {
        String activities = String.join(",",
            activity("d0000000-0000-4000-8000-000000000001", "CREATE", null,
                "{\"old_value\":null,\"new_value\":null,\"assignee_id\":\"" + JANE + "\"}"),
            activity("d0000000-0000-4000-8000-000000000002", "UPDATE", "ASSIGNEE",
                "{\"old_value\":\"" + JANE + "\",\"new_value\":\"" + RAJ + "\"}"),
            activity("d0000000-0000-4000-8000-000000000003", "UPDATE", "STATUS",
                "{\"old_value\":\"" + OPEN + "\",\"new_value\":\"" + APPROVED + "\"}"),
            activity("d0000000-0000-4000-8000-000000000004", "UPDATE", "GROUP",
                "{\"old_value\":null,\"new_value\":\"" + CARE + "\"}"),
            activity("d0000000-0000-4000-8000-000000000005", "UPDATE", "PRIORITY",
                "{\"old_value\":\"LOW\",\"new_value\":\"HIGH\"}")
        );
        responder = request -> historyResponder(request, activities);

        List<IssueActivity> history = await(dashx.getIssueActivity(ISSUE_ID));

        assertEquals(5, history.size());
        assertEquals("Integration backend", history.get(0).getActor().getName());
        assertEquals("Jane", history.get(0).getToAssignee().getName());

        IssueActivity reassigned = history.get(1);
        assertTrue(reassigned.isAssigneeChange());
        assertEquals("Jane", reassigned.getFromAssignee().getName());
        assertEquals("Raj", reassigned.getToAssignee().getName());

        IssueActivity approved = history.get(2);
        assertTrue(approved.isStatusChange());
        assertEquals("Open", approved.getFromStatus().getName());
        assertEquals("Approved", approved.getToStatus().getName());

        assertNull(history.get(3).getFromGroup());
        assertEquals("Support Team", history.get(3).getToGroup().getName());

        assertEquals("LOW", history.get(4).getOldValue());
        assertEquals("HIGH", history.get(4).getNewValue());
        assertNull(history.get(4).getFromAssignee());

        // One page of history plus exactly one bulk lookup per kind of record.
        assertEquals(4, requests.size());
        JsonNode accountLookup = requests.stream()
            .filter(r -> r.get("query").asText().contains("accountsList"))
            .findFirst().orElseThrow();
        List<String> accountIds = new ArrayList<>();
        accountLookup.get("variables").get("filter").get("id").get("in")
            .forEach(id -> accountIds.add(id.asText()));
        assertEquals(List.of(ACTOR, JANE, RAJ), accountIds);
        JsonNode historyQuery = requests.get(0).get("variables");
        assertEquals(ISSUE_ID, historyQuery.get("filter").get("objectId").get("eq").asText());
        assertEquals("asc", historyQuery.get("order").get(0).get("createdAt").asText());
        // Staging rejects `{"id": "asc"}` ("`id` is not orderable"); never send it.
        assertEquals(1, historyQuery.get("order").size());
    }

    @Test
    void unassigningResolvesToANullToAssigneeWithANullRawValue() throws Exception {
        String activities = activity("d0000000-0000-4000-8000-000000000001", "UPDATE", "ASSIGNEE",
            "{\"old_value\":\"" + RAJ + "\",\"new_value\":null}");
        responder = request -> historyResponder(request, activities);

        IssueActivity unassigned = await(dashx.getIssueActivity(ISSUE_ID)).get(0);

        assertEquals("Raj", unassigned.getFromAssignee().getName());
        assertNull(unassigned.getToAssignee());
        assertNull(unassigned.getNewValue(), "null raw value means unassigned, not unresolved");
    }

    @Test
    void anEmptyHistoryMakesNoLookups() throws Exception {
        responder = request -> historyResponder(request, "");

        assertTrue(await(dashx.getIssueActivity(ISSUE_ID)).isEmpty());
        assertEquals(1, requests.size());
    }

    @Test
    void getIssueActivityPagesThroughLongHistories() throws Exception {
        responder = request -> {
            String query = request.get("query").asText();
            if (!query.contains("activitiesList")) return historyResponder(request, "");
            int page = request.get("variables").get("filter").has("createdAt") ? 2 : 1;
            int count = page == 1 ? 100 : 3;
            List<String> entries = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                entries.add(activity(
                    String.format("e0000000-0000-4000-8000-%012d", page * 1000 + i),
                    "UPDATE", "PRIORITY", "{\"old_value\":\"LOW\",\"new_value\":\"HIGH\"}"));
            }
            return "{\"data\":{\"activitiesList\":[" + String.join(",", entries) + "]}}";
        };

        assertEquals(103, await(dashx.getIssueActivity(ISSUE_ID)).size());
    }

    @Test
    void getIssueActivityRejectsBlankIssueId() {
        ExecutionException e = assertThrows(
            ExecutionException.class,
            () -> await(dashx.getIssueActivity(" "))
        );
        assertInstanceOf(DashXValidationException.class, e.getCause());
        assertTrue(requests.isEmpty());
    }
}
