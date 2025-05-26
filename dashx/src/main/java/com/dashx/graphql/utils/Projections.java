package com.dashx.graphql.utils;

import com.dashx.graphql.generated.client.AssetProjectionRoot;
import com.dashx.graphql.generated.client.IdentifyAccountProjectionRoot;
import com.dashx.graphql.generated.client.TrackEventProjectionRoot;
import com.dashx.graphql.generated.client.IssueProjectionRoot;

public class Projections {
    public static IdentifyAccountProjectionRoot<?, ?> fullAccountProjection() {
        return new IdentifyAccountProjectionRoot<>().__typename().id().environmentId().email()
                .phone().fullName().name().firstName().lastName().avatar().timeZone().uid()
                .anonymousUid().createdAt().updatedAt();
    }

    public static AssetProjectionRoot<?, ?> fullAssetProjection() {
        return new AssetProjectionRoot<>().__typename().id().workspaceId().resourceId()
                .attributeId().storageProviderId().uploaderId().data().uploadStatus()
                .processingStatus().createdAt().updatedAt().name().size().mimeType()
                .uploadStatusReason().processingStatusReason().url().staticVideoUrls()
                .staticAudioUrl();
    }

    public static TrackEventProjectionRoot<?, ?> fullTrackEventProjection() {
        return new TrackEventProjectionRoot<>().__typename().success();
    }

    public static IssueProjectionRoot<?, ?> fullIssueProjection() {
        return new IssueProjectionRoot<>().id().workspaceId().issueStatusId().createdById()
                .environmentId().spaceId().parentId().assigneeId().title().description().position()
                .properties().createdAt().updatedAt().issueTypeId().dueAt().number()
                .idempotencyKey().priority().parent();
    }
}
