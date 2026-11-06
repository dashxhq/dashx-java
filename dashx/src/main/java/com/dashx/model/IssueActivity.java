package com.dashx.model;

import com.dashx.graphql.generated.types.Account;
import com.dashx.graphql.generated.types.Activity;
import com.dashx.graphql.generated.types.Group;
import com.dashx.graphql.generated.types.IssueStatus;

/**
 * One entry in an issue's history, with the ids it references resolved to objects.
 *
 * <p>Which typed pair is filled depends on {@link #getField()}: {@code ASSIGNEE} fills
 * {@link #getFromAssignee()} / {@link #getToAssignee()}, {@code STATUS} the status pair,
 * {@code GROUP} the group pair and {@code REQUESTED_BY} the requester pair. For the issue's
 * {@code CREATE} entry, {@link #getToAssignee()} is the initial assignee. Other fields (title,
 * priority, due date...) carry their plain values in {@link #getOldValue()} /
 * {@link #getNewValue()}.
 *
 * <p>A {@code null} typed value means "none" (e.g. unassigned) when the matching raw value is
 * also {@code null}. If the raw value is an id but the typed value is {@code null}, the record
 * could not be resolved (deleted, or outside what the key can read).
 */
public final class IssueActivity {

    public static final String FIELD_ASSIGNEE = "ASSIGNEE";
    public static final String FIELD_STATUS = "STATUS";
    public static final String FIELD_GROUP = "GROUP";
    public static final String FIELD_REQUESTED_BY = "REQUESTED_BY";

    private final Activity raw;
    private final Account actor;
    private final Object oldValue;
    private final Object newValue;
    private final Account fromAssignee;
    private final Account toAssignee;
    private final IssueStatus fromStatus;
    private final IssueStatus toStatus;
    private final Group fromGroup;
    private final Group toGroup;
    private final Account fromRequestedBy;
    private final Account toRequestedBy;

    private IssueActivity(Builder builder) {
        this.raw = builder.raw;
        this.actor = builder.actor;
        this.oldValue = builder.oldValue;
        this.newValue = builder.newValue;
        this.fromAssignee = builder.fromAssignee;
        this.toAssignee = builder.toAssignee;
        this.fromStatus = builder.fromStatus;
        this.toStatus = builder.toStatus;
        this.fromGroup = builder.fromGroup;
        this.toGroup = builder.toGroup;
        this.fromRequestedBy = builder.fromRequestedBy;
        this.toRequestedBy = builder.toRequestedBy;
    }

    public static Builder newBuilder(Activity raw) {
        return new Builder(raw);
    }

    public String getId() {
        return raw.getId();
    }

    /** {@code CREATE}, {@code UPDATE} or {@code DESTROY}. */
    public String getVerb() {
        return raw.getVerb();
    }

    /** The changed field, e.g. {@code ASSIGNEE} or {@code STATUS}; {@code null} on create. */
    public String getField() {
        return raw.getField();
    }

    public String getCreatedAt() {
        return raw.getCreatedAt();
    }

    /** {@code ACCOUNT}, {@code SYSTEM} or {@code INTEGRATION}. */
    public String getActorType() {
        return raw.getActorType();
    }

    public String getActorId() {
        return raw.getActorId();
    }

    /**
     * Who made the change; {@code null} for system changes. For a private-key write this is the
     * account that owns the key pair, not the person your backend acted for.
     */
    public Account getActor() {
        return actor;
    }

    /** The raw previous value (an id for assignee, status, group and requester changes). */
    public Object getOldValue() {
        return oldValue;
    }

    /** The raw new value (an id for assignee, status, group and requester changes). */
    public Object getNewValue() {
        return newValue;
    }

    public Account getFromAssignee() {
        return fromAssignee;
    }

    public Account getToAssignee() {
        return toAssignee;
    }

    public IssueStatus getFromStatus() {
        return fromStatus;
    }

    public IssueStatus getToStatus() {
        return toStatus;
    }

    public Group getFromGroup() {
        return fromGroup;
    }

    public Group getToGroup() {
        return toGroup;
    }

    public Account getFromRequestedBy() {
        return fromRequestedBy;
    }

    public Account getToRequestedBy() {
        return toRequestedBy;
    }

    public boolean isAssigneeChange() {
        return FIELD_ASSIGNEE.equals(getField());
    }

    public boolean isStatusChange() {
        return FIELD_STATUS.equals(getField());
    }

    /** The entry exactly as the API returned it. */
    public Activity getRaw() {
        return raw;
    }

    @Override
    public String toString() {
        return "IssueActivity{verb=" + getVerb() + ", field=" + getField() +
            ", old=" + oldValue + ", new=" + newValue + ", at=" + getCreatedAt() + "}";
    }

    public static final class Builder {

        private final Activity raw;
        private Account actor;
        private Object oldValue;
        private Object newValue;
        private Account fromAssignee;
        private Account toAssignee;
        private IssueStatus fromStatus;
        private IssueStatus toStatus;
        private Group fromGroup;
        private Group toGroup;
        private Account fromRequestedBy;
        private Account toRequestedBy;

        private Builder(Activity raw) {
            this.raw = raw;
        }

        public Builder actor(Account actor) {
            this.actor = actor;
            return this;
        }

        public Builder values(Object oldValue, Object newValue) {
            this.oldValue = oldValue;
            this.newValue = newValue;
            return this;
        }

        public Builder assignees(Account from, Account to) {
            this.fromAssignee = from;
            this.toAssignee = to;
            return this;
        }

        public Builder statuses(IssueStatus from, IssueStatus to) {
            this.fromStatus = from;
            this.toStatus = to;
            return this;
        }

        public Builder groups(Group from, Group to) {
            this.fromGroup = from;
            this.toGroup = to;
            return this;
        }

        public Builder requesters(Account from, Account to) {
            this.fromRequestedBy = from;
            this.toRequestedBy = to;
            return this;
        }

        public IssueActivity build() {
            return new IssueActivity(this);
        }
    }
}
