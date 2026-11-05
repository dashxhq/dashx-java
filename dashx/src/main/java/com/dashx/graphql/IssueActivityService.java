package com.dashx.graphql;

import com.dashx.DashXGraphQLClient;
import com.dashx.graphql.generated.types.Account;
import com.dashx.graphql.generated.types.Activity;
import com.dashx.graphql.generated.types.Group;
import com.dashx.graphql.generated.types.IssueStatus;
import com.dashx.model.IssueActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import reactor.core.publisher.Mono;

/**
 * Reads an issue's history and resolves the ids it references (actor, assignees, statuses,
 * groups, requesters) into objects, in at most one bulk lookup per kind.
 */
public class IssueActivityService {

    static final String NIL_UUID = "00000000-0000-0000-0000-000000000000";
    static final int PAGE_SIZE = 100;
    static final int MAX_PAGES = 50;

    private static final String ACTIVITY_PROJECTION = """
        {
            id
            actorType
            actorId
            verb
            objectType
            objectId
            field
            metadata
            createdAt
            updatedAt
            workspaceId
        }
        """;

    private static final String STATUS_PROJECTION = """
        {
            id
            workspaceId
            issueTypeId
            kind
            name
            identifier
            color
            position
            isDefault
            createdAt
            updatedAt
        }
        """;

    private final DashXGraphQLClient client;
    private final AccountService accountService;
    private final GroupService groupService;

    public IssueActivityService(
        DashXGraphQLClient client,
        AccountService accountService,
        GroupService groupService
    ) {
        this.client = client;
        this.accountService = accountService;
        this.groupService = groupService;
    }

    /**
     * The issue's full history, oldest first, with referenced records resolved.
     *
     * @param issueId the issue id
     * @return a Mono that emits the resolved entries
     */
    public Mono<List<IssueActivity>> getIssueActivity(String issueId) {
        return listAllActivities(issueId, 1, new ArrayList<>()).flatMap(this::resolve);
    }

    private Mono<List<Activity>> listAllActivities(
        String issueId,
        int page,
        List<Activity> collected
    ) {
        String query =
            "query ListIssueActivities($filter: JSON, $order: [JSON!], $limit: Int, $page: Int) " +
            "{ activitiesList(filter: $filter, order: $order, limit: $limit, page: $page) " +
            ACTIVITY_PROJECTION +
            " }";

        Map<String, Object> variables = new HashMap<>();
        variables.put("filter", Map.of("objectId", Map.of("eq", issueId)));
        // A fixed order so pages neither skip nor repeat entries. `id` is not orderable on the
        // server, so `createdAt` alone is the sort key.
        variables.put("order", List.of(Map.of("createdAt", "asc")));
        variables.put("limit", PAGE_SIZE);
        variables.put("page", page);

        return client.execute(query, variables).flatMap(response -> {
            Activity[] batch = response.extractValueAsObject("activitiesList", Activity[].class);
            if (batch != null) collected.addAll(List.of(batch));
            int size = batch == null ? 0 : batch.length;
            if (size < PAGE_SIZE || page >= MAX_PAGES) {
                return Mono.just(collected);
            }
            return listAllActivities(issueId, page + 1, collected);
        });
    }

    private Mono<List<IssueActivity>> resolve(List<Activity> activities) {
        Set<String> accountIds = new LinkedHashSet<>();
        Set<String> statusIds = new LinkedHashSet<>();
        Set<String> groupIds = new LinkedHashSet<>();

        for (Activity activity : activities) {
            if ("ACCOUNT".equals(activity.getActorType())) addId(accountIds, activity.getActorId());
            String field = activity.getField();
            if (IssueActivity.FIELD_ASSIGNEE.equals(field) ||
                IssueActivity.FIELD_REQUESTED_BY.equals(field)) {
                addId(accountIds, oldValue(activity));
                addId(accountIds, newValue(activity));
            } else if (IssueActivity.FIELD_STATUS.equals(field)) {
                addId(statusIds, oldValue(activity));
                addId(statusIds, newValue(activity));
            } else if (IssueActivity.FIELD_GROUP.equals(field)) {
                addId(groupIds, oldValue(activity));
                addId(groupIds, newValue(activity));
            }
            if ("CREATE".equals(activity.getVerb())) {
                addId(accountIds, metadataValue(activity, "assignee_id"));
            }
        }

        Mono<Map<String, Account>> accounts = lookup(
            accountIds,
            filter -> accountService.listAccounts(filter, null, PAGE_SIZE, null),
            Account::getId
        );
        Mono<Map<String, IssueStatus>> statuses = lookup(statusIds, this::listIssueStatuses, IssueStatus::getId);
        Mono<Map<String, Group>> groups = lookup(
            groupIds,
            filter -> groupService.listGroups(filter, null, PAGE_SIZE, null),
            Group::getId
        );

        return Mono.zip(accounts, statuses, groups).map(found -> {
            Map<String, Account> accountsById = found.getT1();
            Map<String, IssueStatus> statusesById = found.getT2();
            Map<String, Group> groupsById = found.getT3();

            List<IssueActivity> resolved = new ArrayList<>(activities.size());
            for (Activity activity : activities) {
                Object oldValue = metadataValue(activity, "old_value");
                Object newValue = metadataValue(activity, "new_value");
                IssueActivity.Builder builder = IssueActivity.newBuilder(activity)
                    .values(oldValue, newValue)
                    .actor(accountsById.get(activity.getActorId()));

                String field = activity.getField();
                if (IssueActivity.FIELD_ASSIGNEE.equals(field)) {
                    builder.assignees(accountsById.get(asId(oldValue)), accountsById.get(asId(newValue)));
                } else if (IssueActivity.FIELD_REQUESTED_BY.equals(field)) {
                    builder.requesters(accountsById.get(asId(oldValue)), accountsById.get(asId(newValue)));
                } else if (IssueActivity.FIELD_STATUS.equals(field)) {
                    builder.statuses(statusesById.get(asId(oldValue)), statusesById.get(asId(newValue)));
                } else if (IssueActivity.FIELD_GROUP.equals(field)) {
                    builder.groups(groupsById.get(asId(oldValue)), groupsById.get(asId(newValue)));
                } else if ("CREATE".equals(activity.getVerb())) {
                    builder.assignees(null, accountsById.get(asId(metadataValue(activity, "assignee_id"))));
                }
                resolved.add(builder.build());
            }
            return resolved;
        });
    }

    private Mono<List<IssueStatus>> listIssueStatuses(Map<String, Object> filter) {
        String query =
            "query ListIssueStatuses($filter: JSON, $limit: Int) " +
            "{ issueStatusesList(filter: $filter, limit: $limit) " +
            STATUS_PROJECTION +
            " }";
        Map<String, Object> variables = Map.of("filter", filter, "limit", PAGE_SIZE);
        return client.execute(query, variables).map(response -> {
            IssueStatus[] statuses = response.extractValueAsObject("issueStatusesList", IssueStatus[].class);
            return statuses != null ? List.of(statuses) : List.of();
        });
    }

    /** Fetches records by id in chunks of {@link #PAGE_SIZE}; no ids means no request. */
    private static <T> Mono<Map<String, T>> lookup(
        Set<String> ids,
        Function<Map<String, Object>, Mono<List<T>>> list,
        Function<T, String> idOf
    ) {
        // A HashMap, never Map.of(): callers look up null ids (e.g. "unassigned"), and
        // Map.of().get(null) throws.
        if (ids.isEmpty()) return Mono.just(new HashMap<>());

        List<String> all = new ArrayList<>(ids);
        List<Mono<List<T>>> chunks = new ArrayList<>();
        for (int start = 0; start < all.size(); start += PAGE_SIZE) {
            List<String> chunk = all.subList(start, Math.min(start + PAGE_SIZE, all.size()));
            chunks.add(list.apply(Map.of("id", Map.of("in", List.copyOf(chunk)))));
        }
        return Mono.zip(chunks, results -> {
            Map<String, T> byId = new HashMap<>();
            for (Object result : results) {
                @SuppressWarnings("unchecked")
                List<T> records = (List<T>) result;
                for (T record : records) byId.put(idOf.apply(record), record);
            }
            return byId;
        });
    }

    private static Object oldValue(Activity activity) {
        return metadataValue(activity, "old_value");
    }

    private static Object newValue(Activity activity) {
        return metadataValue(activity, "new_value");
    }

    private static Object metadataValue(Activity activity, String key) {
        Map<String, Object> metadata = activity.getMetadata();
        return metadata == null ? null : metadata.get(key);
    }

    private static String asId(Object value) {
        return value instanceof String id && !id.isEmpty() && !NIL_UUID.equals(id) ? id : null;
    }

    private static void addId(Set<String> ids, Object value) {
        String id = asId(value);
        if (id != null) ids.add(id);
    }
}
