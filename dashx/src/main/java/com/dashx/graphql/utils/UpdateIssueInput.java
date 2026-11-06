package com.dashx.graphql.utils;

import com.dashx.exception.DashXValidationException;
import com.dashx.graphql.generated.types.IssuePriority;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Input for {@code DashX.updateIssue}: a partial update of one issue, by id.
 *
 * <p>Only the fields you set are sent. On {@code updateIssue} an explicit {@code null} CLEARS a
 * field, so this class never turns an unset field into {@code null} (unlike the generated input
 * POJOs, which serialize every unset field as {@code null}). Clearing is always a deliberate call:
 * {@link Builder#clearAssignee()}, {@link Builder#clearDescription()}, {@link Builder#clearDueAt()},
 * {@link Builder#clearGroup()} and {@link Builder#clearParent()}. Passing {@code null} to a setter
 * is rejected rather than read as "clear".
 *
 * <pre>{@code
 * // Unassign and move to a closing status in ONE call, so auto-assignment can't
 * // pick the ticket up between the two changes.
 * UpdateIssueInput input = UpdateIssueInput.newBuilder(issueId)
 *     .clearAssignee()
 *     .issueStatus("Approved")
 *     .build();
 * }</pre>
 */
public final class UpdateIssueInput {

    private final String id;
    private final Map<String, Object> changes;

    private UpdateIssueInput(String id, Map<String, Object> changes) {
        this.id = id;
        this.changes = Collections.unmodifiableMap(new LinkedHashMap<>(changes));
    }

    /**
     * @param id the DashX issue id to update
     */
    public static Builder newBuilder(String id) {
        return new Builder(id);
    }

    public String getId() {
        return id;
    }

    /** The fields this update sends, in the order they were set; a {@code null} value clears. */
    public Map<String, Object> getChanges() {
        return changes;
    }

    /** Whether this update sends {@code field} (set or cleared). */
    public boolean has(String field) {
        return changes.containsKey(field);
    }

    /**
     * The GraphQL {@code input} variable: {@code id} plus exactly the fields that were set.
     * Keys absent here are left unchanged by the server.
     */
    public Map<String, Object> toVariables() {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("id", id);
        input.putAll(changes);
        return input;
    }

    @Override
    public String toString() {
        return "UpdateIssueInput" + toVariables();
    }

    public static final class Builder {

        private final String id;
        private final Map<String, Object> changes = new LinkedHashMap<>();

        private Builder(String id) {
            if (id == null || id.trim().isEmpty()) {
                throw new DashXValidationException("Issue ID cannot be null or empty");
            }
            this.id = id;
        }

        private Builder set(String field, Object value) {
            if (value == null) {
                throw new DashXValidationException(
                    field + " cannot be null; use the matching clear method to clear a field"
                );
            }
            changes.put(field, value);
            return this;
        }

        private Builder clear(String field) {
            changes.put(field, null);
            return this;
        }

        /** Status by id, identifier or exact name; must belong to the issue's type. */
        public Builder issueStatus(String issueStatus) {
            return set("issueStatus", issueStatus);
        }

        /** Assigns the issue to this DashX account id. */
        public Builder assigneeId(String assigneeId) {
            return set("assigneeId", assigneeId);
        }

        /**
         * Removes the assignee. If the issue's group has an auto-assignment rule covering its
         * resulting status, auto-assignment runs right away and may assign it again.
         */
        public Builder clearAssignee() {
            return clear("assigneeId");
        }

        public Builder title(String title) {
            return set("title", title);
        }

        public Builder description(String description) {
            return set("description", description);
        }

        public Builder clearDescription() {
            return clear("description");
        }

        public Builder priority(IssuePriority priority) {
            return set("priority", priority);
        }

        public Builder position(Integer position) {
            return set("position", position);
        }

        /** Merged into the issue's existing properties, not a replacement. */
        public Builder properties(Map<String, Object> properties) {
            return set("properties", properties);
        }

        /** Replaces the issue's labels. */
        public Builder labels(List<String> labels) {
            return set("labels", labels);
        }

        /** ISO-8601 timestamp. */
        public Builder dueAt(String dueAt) {
            return set("dueAt", dueAt);
        }

        public Builder clearDueAt() {
            return clear("dueAt");
        }

        /** Group by name or id. */
        public Builder group(String group) {
            return set("group", group);
        }

        public Builder clearGroup() {
            return clear("group");
        }

        public Builder parent(String parent) {
            return set("parent", parent);
        }

        public Builder clearParent() {
            return clear("parent");
        }

        /** Identify-style requester, e.g. {@code {"email": "..."}} or {@code {"uid": "..."}}. */
        public Builder requestedBy(Map<String, Object> requestedBy) {
            return set("requestedBy", requestedBy);
        }

        public UpdateIssueInput build() {
            if (changes.isEmpty()) {
                throw new DashXValidationException(
                    "UpdateIssueInput must set or clear at least one field"
                );
            }
            return new UpdateIssueInput(id, changes);
        }
    }
}
