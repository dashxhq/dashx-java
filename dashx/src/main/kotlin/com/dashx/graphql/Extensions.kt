package com.dashx.graphql

import com.dashx.graphql.generated.getasset.Asset as GetAssetResult
import com.dashx.graphql.generated.inputs.SearchRecordsInput
import com.dashx.graphql.generated.listassets.Asset as ListAssetsResult
import com.dashx.graphql.models.Asset

fun SearchRecordsOptions.toInput(resource: String): SearchRecordsInput =
        SearchRecordsInput(
                exclude = this.exclude,
                fields = this.fields,
                filter = this.filter,
                include = this.include,
                language = this.language,
                limit = this.limit,
                order = this.order,
                page = this.page,
                preview = this.preview,
                resource = resource,
        )

fun GetAssetResult.toAsset(): Asset =
        Asset(
                id = this.id,
                resourceId = this.resourceId,
                attributeId = this.attributeId,
                uploaderId = this.uploaderId,
                name = this.name,
                mimeType = this.mimeType,
                url = this.url,
                size = this.size,
                uploadStatus = this.uploadStatus,
                uploadStatusReason = this.uploadStatusReason,
                processingStatus = this.processingStatus,
                processingStatusReason = this.processingStatusReason,
                createdAt = this.createdAt,
                updatedAt = this.updatedAt,
        )

fun ListAssetsResult.toAsset(): Asset =
        Asset(
                id = this.id,
                resourceId = this.resourceId,
                attributeId = this.attributeId,
                uploaderId = this.uploaderId,
                name = this.name,
                mimeType = this.mimeType,
                url = this.url,
                size = this.size,
                uploadStatus = this.uploadStatus,
                uploadStatusReason = this.uploadStatusReason,
                processingStatus = this.processingStatus,
                processingStatusReason = this.processingStatusReason,
                createdAt = this.createdAt,
                updatedAt = this.updatedAt,
        )
