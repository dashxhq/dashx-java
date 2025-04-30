package com.dashx.demo.springboot;

import com.dashx.DashX;
import com.dashx.graphql.generated.types.Asset;
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
        });
    }

    @GetMapping("/get-asset")
    public CompletableFuture<Map<String, Object>> getAsset(@RequestParam String id) {
        return dashX.getAsset(id).thenApply(response -> {
            Map<String, Object> result = new HashMap<>();
            result.put("id", response.getId());
            result.put("url", response.getUrl());
            return result;
        });
    }

    @GetMapping("/list-assets")
    public CompletableFuture<List<Asset>> listAssets(@RequestParam String resourceId) {
        return dashX.listAssets(
                Map.of("resourceId", Map.of("eq", resourceId), "id", Map.of("in", List.of(1234))));
    }

    @GetMapping("/search-records")
    public CompletableFuture<List<Map<String, Object>>> searchRecords(@RequestParam String resource,
            @RequestParam(required = false) SearchRecordsOptions options) {

        if (options == null) {
            options = new SearchRecordsOptions.Builder().order(List.of(Map.of("createdAt", "desc")))
                    .build();
        }

        return dashX.searchRecords(resource, options);
    }
}
