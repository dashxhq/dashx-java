package com.dashx.graphql.utils;

import com.dashx.graphql.generated.client.AssetProjectionRoot;
import com.dashx.graphql.generated.client.IdentifyAccountProjectionRoot;
import com.dashx.graphql.generated.client.TrackEventProjectionRoot;

public class Projections {
    public static IdentifyAccountProjectionRoot<?, ?> fullAccountProjection() {
        return new IdentifyAccountProjectionRoot<>().id().environmentId().email().phone().fullName()
                .name().firstName().lastName().avatar().timeZone().uid().anonymousUid().createdAt()
                .updatedAt();
    }

    public static AssetProjectionRoot<?, ?> fullAssetProjection() {
        return new AssetProjectionRoot<>().workspaceId().resourceId().attributeId()
                .storageProviderId().uploaderId().data().uploadStatus().processingStatus()
                .createdAt().updatedAt().name().size().mimeType().uploadStatusReason()
                .processingStatusReason().url().staticVideoUrls().staticAudioUrl();
    }

    public static TrackEventProjectionRoot<?, ?> fullTrackEventProjection() {
        return new TrackEventProjectionRoot<>().success();
    }
}
