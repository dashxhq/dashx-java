package com.dashx.graphql.models

import com.dashx.graphql.generated.Timestamp
import com.dashx.graphql.generated.UUID
import com.dashx.graphql.generated.enums.AssetProcessingStatus
import com.dashx.graphql.generated.enums.AssetUploadStatus
import com.dashx.graphql.generated.scalars.JsonObjectSerializer
import kotlin.Int
import kotlin.String
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
public data class Asset(
        public val id: UUID,
        public val resourceId: UUID? = null,
        public val attributeId: UUID? = null,
        public val uploaderId: UUID? = null,
        public val name: String? = null,
        public val mimeType: String? = null,
        @Serializable(with = JsonObjectSerializer::class) public val data: JsonObject,
        public val size: Int? = null,
        public val uploadStatus: AssetUploadStatus = AssetUploadStatus.__UNKNOWN_VALUE,
        public val uploadStatusReason: String? = null,
        public val processingStatus: AssetProcessingStatus = AssetProcessingStatus.__UNKNOWN_VALUE,
        public val processingStatusReason: String? = null,
        public val createdAt: Timestamp,
        public val updatedAt: Timestamp,
)
