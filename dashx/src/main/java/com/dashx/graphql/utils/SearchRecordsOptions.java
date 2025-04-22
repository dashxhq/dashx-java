package com.dashx.graphql.utils;

import java.util.Map;
import java.util.List;

public class SearchRecordsOptions {
    private List<Map<String, Object>> exclude;
    private List<Map<String, Object>> fields;
    private Map<String, Object> filter;
    private List<Map<String, Object>> include;
    private String language;
    private Integer limit;
    private List<Map<String, Object>> order;
    private Integer page;
    private Boolean preview;

    private SearchRecordsOptions() {}

    public static Builder newBuilder() {
        return new Builder();
    }

    public List<Map<String, Object>> getExclude() {
        return exclude;
    }

    public List<Map<String, Object>> getFields() {
        return fields;
    }

    public Map<String, Object> getFilter() {
        return filter;
    }

    public List<Map<String, Object>> getInclude() {
        return include;
    }

    public String getLanguage() {
        return language;
    }

    public Integer getLimit() {
        return limit;
    }

    public List<Map<String, Object>> getOrder() {
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

        public Builder() {
            options = new SearchRecordsOptions();
        }

        public Builder exclude(List<Map<String, Object>> exclude) {
            options.exclude = exclude;
            return this;
        }

        public Builder fields(List<Map<String, Object>> fields) {
            options.fields = fields;
            return this;
        }

        public Builder filter(Map<String, Object> filter) {
            options.filter = filter;
            return this;
        }

        public Builder include(List<Map<String, Object>> include) {
            options.include = include;
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
            options.order = order;
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
