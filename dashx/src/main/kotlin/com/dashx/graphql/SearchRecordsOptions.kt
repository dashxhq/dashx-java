package com.dashx.graphql

import com.dashx.graphql.generated.scalars.JsonObjectSerializer
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
public data class SearchRecordsOptions(
        public val exclude: List<@Serializable(with = JsonObjectSerializer::class) JsonObject>? =
                null,
        public val fields: List<@Serializable(with = JsonObjectSerializer::class) JsonObject>? =
                null,
        @Serializable(with = JsonObjectSerializer::class) public val filter: JsonObject? = null,
        public val include: List<@Serializable(with = JsonObjectSerializer::class) JsonObject>? =
                null,
        public val language: String? = null,
        public val limit: Int? = null,
        @Serializable(with = JsonObjectSerializer::class) public val order: JsonObject? = null,
        public val page: Int? = null,
        public val preview: Boolean? = null,
)
