package com.dashx.demo.springboot;

import com.dashx.DashX;
import com.dashx.graphql.generated.types.Asset;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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

    @GetMapping("/track")
    public CompletableFuture<Map<String, String>> trackEvent(@RequestParam String event,
            @RequestParam(required = false) String uid) {

        return dashX.track(event, uid).thenApply(response -> {
            Map<String, String> result = new HashMap<>();
            result.put("status", response.getSuccess() ? "success" : "error");
            result.put("event", event);
            return result;
        });
    }

    @GetMapping("/get-asset")
    public CompletableFuture<Map<String, String>> getAsset(@RequestParam String id) {
        return dashX.getAsset(id).thenApply(response -> {
            Map<String, String> result = new HashMap<>();
            result.put("id", response.getId());
            result.put("url", response.getUrl());
            return result;
        });
    }

    @GetMapping("/list-assets")
    public CompletableFuture<Map<String, String>> listAssets(@RequestParam String resourceId) {
        return dashX.listAssets(new HashMap<String, Object>() {
            {
                put("resourceId", new HashMap<String, Object>() {
                    {
                        put("eq", resourceId);
                    }
                });
            }
        }).thenApply(response -> {
            List<Asset> assets = response;
            Map<String, String> result = new HashMap<>();

            for (Asset asset : assets) {
                if (asset.getUrl() != null) {
                    result.put("id", asset.getId());
                    result.put("url", asset.getUrl());
                }
            }

            return result;
        });
    }
}
