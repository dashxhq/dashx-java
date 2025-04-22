package com.dashx.graphql.utils;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONObject;

public class SearchRecordsOptions {
    private List<JSONObject> exclude;
    private List<JSONObject> fields;
    private JSONObject filter;
    private List<JSONObject> include;
    private String language;
    private Integer limit;
    private List<JSONObject> order;
    private Integer page;
    private Boolean preview;

    private SearchRecordsOptions() {}

    public static Builder newBuilder() {
        return new Builder();
    }

    public List<Map<String, Object>> getExclude() {
        return exclude.stream().map(JSONObject::toMap).collect(Collectors.toList());
    }

    public List<JSONObject> getExcludeJSON() {
        return exclude;
    }

    public List<Map<String, Object>> getFields() {
        return fields.stream().map(JSONObject::toMap).collect(Collectors.toList());
    }

    public List<JSONObject> getFieldsJSON() {
        return fields;
    }

    public Map<String, Object> getFilter() {
        return filter.toMap();
    }

    public JSONObject getFilterJSON() {
        return filter;
    }

    public List<Map<String, Object>> getInclude() {
        return include.stream().map(JSONObject::toMap).collect(Collectors.toList());
    }

    public List<JSONObject> getIncludeJSON() {
        return include;
    }

    public String getLanguage() {
        return language;
    }

    public Integer getLimit() {
        return limit;
    }

    public List<Map<String, Object>> getOrder() {
        return order.stream().map(JSONObject::toMap).collect(Collectors.toList());
    }

    public List<JSONObject> getOrderJSON() {
        return order;
    }

    public Integer getPage() {
        return page;
    }

    public Boolean getPreview() {
        return preview;
    }

    public static class Builder {
        private final SearchRecordsOptions options;

        private Builder() {
            options = new SearchRecordsOptions();
        }

        public Builder exclude(List<Map<String, Object>> exclude) {
            options.exclude = exclude.stream().map(JSONObject::new).collect(Collectors.toList());
            return this;
        }

        public Builder fields(List<Map<String, Object>> fields) {
            options.fields = fields.stream().map(JSONObject::new).collect(Collectors.toList());
            return this;
        }

        public Builder filter(Map<String, Object> filter) {
            options.filter = new JSONObject(filter);
            return this;
        }

        public Builder include(List<Map<String, Object>> include) {
            options.include = include.stream().map(JSONObject::new).collect(Collectors.toList());
            return this;
        }

        public Builder language(String language) {
            options.language = language;
            return this;
        }

        public Builder limit(Integer limit) {
            options.limit = limit;
            return this;
        }

        public Builder order(List<Map<String, Object>> order) {
            options.order = order.stream().map(JSONObject::new).collect(Collectors.toList());
            return this;
        }

        public Builder page(Integer page) {
            options.page = page;
            return this;
        }

        public Builder preview(Boolean preview) {
            options.preview = preview;
            return this;
        }

        public SearchRecordsOptions build() {
            return options;
        }
    }
}
