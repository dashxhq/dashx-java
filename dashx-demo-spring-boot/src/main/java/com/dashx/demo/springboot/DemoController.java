package com.dashx.demo.springboot;

import com.dashx.DashX;
import com.dashx.graphql.generated.types.Asset;
import com.dashx.graphql.generated.types.CreateIssueInput;
import com.dashx.graphql.generated.types.Issue;
import com.dashx.graphql.generated.types.UpsertIssueInput;
import com.dashx.graphql.utils.SearchRecordsOptions;
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
public class DemoController {
    private final DashX dashX;

    public DemoController(DashX dashX) {
        this.dashX = dashX;
    }

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome to the DashX Demo!";
    }

    @GetMapping("/identify")
    public CompletableFuture<Map<String, Object>> identify(
            @RequestParam Map<String, Object> options) {

        return dashX.identify(options).thenApply(response -> {
            Map<String, Object> result = new HashMap<>();
            result.put("id", response.getId());
            result.put("firstName", response.getFirstName());
            result.put("lastName", response.getLastName());
            result.put("email", response.getEmail());
            result.put("phone", response.getPhone());
            result.put("name", response.getName());
            result.put("anonymousUid", response.getAnonymousUid());
            result.put("uid", response.getUid());
            return result;
        }).exceptionally(ex -> {
            throw new RuntimeException("Failed to identify user: " + ex.getMessage(), ex);
        });
    }

    @GetMapping("/track")
    public CompletableFuture<Map<String, Object>> trackEvent(@RequestParam String event,
            @RequestParam(required = false) String uid) {

        return dashX.track(event, uid).thenApply(response -> {
            Map<String, Object> result = new HashMap<>();
            result.put("status", response.getSuccess() ? "success" : "error");
            result.put("event", event);
            return result;
        }).exceptionally(ex -> {
            throw new RuntimeException("Failed to track event '" + event + "': " + ex.getMessage(), ex);
        });
    }

    @GetMapping("/track-with-data")
    public CompletableFuture<Map<String, Object>> trackEventWithData(
            @RequestParam String event,
            @RequestParam(required = false) String uid,
            @RequestParam(required = false) String dataKey,
            @RequestParam(required = false) String dataValue) {

        Map<String, Object> data = new HashMap<>();
        if (dataKey != null && dataValue != null) {
            data.put(dataKey, dataValue);
        }

        return dashX.track(event, uid, data).thenApply(response -> {
            Map<String, Object> result = new HashMap<>();
            result.put("status", response.getSuccess() ? "success" : "error");
            result.put("event", event);
            result.put("data", data);
            return result;
        }).exceptionally(ex -> {
            throw new RuntimeException("Failed to track event '" + event + "' with data: " + ex.getMessage(), ex);
        });
    }

    @GetMapping("/get-asset")
    public CompletableFuture<Map<String, Object>> getAsset(@RequestParam String id) {
        return dashX.getAsset(id).thenApply(response -> {
            Map<String, Object> result = new HashMap<>();
            result.put("id", response.getId());
            result.put("url", response.getUrl());
            return result;
        }).exceptionally(ex -> {
            throw new RuntimeException("Failed to get asset with id '" + id + "': " + ex.getMessage(), ex);
        });
    }

    @GetMapping("/list-assets")
    public CompletableFuture<List<Asset>> listAssets(@RequestParam(required = false) String resourceId) {
        CompletableFuture<List<Asset>> future;
        if (resourceId != null && !resourceId.isEmpty()) {
            future = dashX.listAssets(Map.of("resourceId", Map.of("eq", resourceId)));
        } else {
            future = dashX.listAssets();
        }
        return future.exceptionally(ex -> {
            throw new RuntimeException("Failed to list assets: " + ex.getMessage(), ex);
        });
    }

    @GetMapping("/list-assets-filtered")
    public CompletableFuture<List<Asset>> listAssetsFiltered(
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) String orderField,
            @RequestParam(required = false) String orderDirection) {

        Map<String, Object> filter = null;
        if (resourceId != null && !resourceId.isEmpty()) {
            filter = Map.of("resourceId", Map.of("eq", resourceId));
        }

        List<Map<String, Object>> order = null;
        if (orderField != null && !orderField.isEmpty()) {
            String direction = orderDirection != null ? orderDirection : "desc";
            order = List.of(Map.of(orderField, direction));
        }

        return dashX.listAssets(filter, order).exceptionally(ex -> {
            throw new RuntimeException("Failed to list filtered assets: " + ex.getMessage(), ex);
        });
    }

    @GetMapping("/search-records")
    public CompletableFuture<List<Map<String, Object>>> searchRecords(
            @RequestParam String resource,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer page) {

        SearchRecordsOptions options = SearchRecordsOptions.newBuilder()
                .order(List.of(Map.of("createdAt", "desc")))
                .limit(limit)
                .page(page)
                .build();

        return dashX.searchRecords(resource, options).exceptionally(ex -> {
            throw new RuntimeException("Failed to search records for resource '" + resource + "': " + ex.getMessage(), ex);
        });
    }

    @GetMapping("/create-issue")
    public CompletableFuture<Issue> createIssue(@RequestParam String title,
            @RequestParam(required = false) String issueType,
            @RequestParam(required = false) String issueStatus) {
        CreateIssueInput.Builder builder = CreateIssueInput.newBuilder();

        builder.title(title);
        builder.issueType(issueType);
        builder.group("Test Group"); // Must match a group name or id in your Workspace
        builder.issueStatus(issueStatus);
        builder.labels(List.of("Test1", "Test2"));
        builder.properties(Map.of("some-property", "value"));

        builder.requestedBy(Map.of("uid", "123", "name", "John Doe"));
        // builder.requestedBy(Map.of("email", "test@test.com", "name", "John Doe"));
        // builder.requestedBy(Map.of("phone", "0987654321", "name", "John Doe"));

        CreateIssueInput input = builder.build();

        return dashX.createIssue(input).exceptionally(ex -> {
            throw new RuntimeException("Failed to create issue with title '" + title + "': " + ex.getMessage(), ex);
        });
    }

    @GetMapping("/upsert-issue")
    public CompletableFuture<Issue> upsertIssue(
            @RequestParam String title,
            @RequestParam(required = false) String issueType,
            @RequestParam(required = false) String issueStatus,
            @RequestParam(required = false) String idempotencyKey) {

        UpsertIssueInput.Builder builder = UpsertIssueInput.newBuilder();

        builder.title(title);
        builder.issueType(issueType);
        builder.group("Test Group"); // Must match a group name or id in your Workspace
        builder.issueStatus(issueStatus);
        builder.labels(List.of("Test1", "Test2"));
        builder.properties(Map.of("some-property", "value"));

        if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
            builder.idempotencyKey(idempotencyKey);
        }

        UpsertIssueInput input = builder.build();

        return dashX.upsertIssue(input).exceptionally(ex -> {
            throw new RuntimeException("Failed to upsert issue with title '" + title + "': " + ex.getMessage(), ex);
        });
    }
}
