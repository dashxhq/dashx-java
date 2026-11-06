package com.dashx.graphql;

import com.dashx.exception.DashXException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import reactor.core.publisher.Mono;

/**
 * Reads every record matching a filter, oldest first, without skipping or repeating any.
 *
 * <p>Offset paging ordered by {@code createdAt} alone is unstable: records created in the same
 * transaction share a timestamp, the server cannot order by {@code id} as a tiebreaker, and tied
 * rows may come back in a different order on each request. So instead of page numbers, each
 * request asks for {@code createdAt >= <last timestamp seen>} and excludes the ids already read at
 * that timestamp. Every row it returns is new, and none is passed over.
 *
 * <p>Reading stops with an error, never a partial list, if the server returns a record twice or
 * it reads {@code pageSize * maxRequests} records and the last page is still full.
 */
public final class KeysetPager<T> {

    /** Runs one list query. */
    @FunctionalInterface
    public interface Fetch<T> {
        Mono<List<T>> fetch(
            Map<String, Object> filter,
            List<Map<String, Object>> order,
            int limit
        );
    }

    static final List<Map<String, Object>> ORDER = List.of(Map.of("createdAt", "asc"));

    private final String description;
    private final int pageSize;
    private final int maxRequests;
    private final Function<T, String> idOf;
    private final Function<T, String> createdAtOf;
    private final Fetch<T> fetch;

    /**
     * @param description what is being read, for error messages (e.g. "Issue history")
     */
    public KeysetPager(
        String description,
        int pageSize,
        int maxRequests,
        Function<T, String> idOf,
        Function<T, String> createdAtOf,
        Fetch<T> fetch
    ) {
        this.description = description;
        this.pageSize = pageSize;
        this.maxRequests = maxRequests;
        this.idOf = idOf;
        this.createdAtOf = createdAtOf;
        this.fetch = fetch;
    }

    /**
     * @param filter the caller's filter; must not use {@code createdAt} or {@code and}, which the
     *     pager adds itself
     */
    public Mono<List<T>> fetchAll(Map<String, Object> filter) {
        if (filter.containsKey("createdAt") || filter.containsKey("and")) {
            throw new IllegalArgumentException("filter must not set createdAt or and");
        }
        return next(filter, null, new LinkedHashSet<>(), new ArrayList<>(), new HashSet<>(), 1);
    }

    private Mono<List<T>> next(
        Map<String, Object> base,
        String cursor,
        Set<String> readAtCursor,
        List<T> collected,
        Set<String> seen,
        int request
    ) {
        if (request > maxRequests) {
            return Mono.error(
                new DashXException(
                    description + " has " + (pageSize * maxRequests) +
                    " or more entries; stopped instead of returning a possibly partial list"
                )
            );
        }

        Map<String, Object> filter = new LinkedHashMap<>(base);
        if (cursor != null) {
            filter.put("createdAt", Map.of("gte", cursor));
            if (!readAtCursor.isEmpty()) {
                filter.put(
                    "and",
                    readAtCursor.stream().map(id -> Map.of("id", Map.of("neq", id))).toList()
                );
            }
        }

        return fetch.fetch(filter, ORDER, pageSize).flatMap(batch -> {
            for (T record : batch) {
                if (!seen.add(idOf.apply(record))) {
                    return Mono.error(
                        new DashXException(
                            description + ": the server returned " + idOf.apply(record) +
                            " twice; stopped instead of returning a list with duplicates"
                        )
                    );
                }
                if (createdAtOf.apply(record) == null) {
                    return Mono.error(
                        new DashXException(
                            description + ": " + idOf.apply(record) + " has no createdAt"
                        )
                    );
                }
            }
            collected.addAll(batch);
            if (batch.size() < pageSize) {
                return Mono.just(List.copyOf(collected));
            }

            // Results are ordered by createdAt, so the last record's timestamp is the newest
            // read. Ids read at that timestamp carry over while it stays the cursor.
            String last = createdAtOf.apply(batch.get(batch.size() - 1));
            Set<String> nextReadAtCursor = last.equals(cursor)
                ? readAtCursor
                : new LinkedHashSet<>();
            for (T record : batch) {
                if (last.equals(createdAtOf.apply(record))) {
                    nextReadAtCursor.add(idOf.apply(record));
                }
            }
            return next(base, last, nextReadAtCursor, collected, seen, request + 1);
        });
    }
}
