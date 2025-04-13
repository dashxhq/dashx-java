package com.dashx.graphql

import com.dashx.graphql.generated.inputs.SearchRecordsInput

fun SearchRecordsOptions.toInput(resource: String): SearchRecordsInput {
    return SearchRecordsInput(
            exclude = this.exclude,
            fields = this.fields,
            filter = this.filter,
            include = this.include,
            language = this.language,
            limit = this.limit,
            order = this.order,
            page = this.page,
            preview = this.preview,
            resource = resource
    )
}
