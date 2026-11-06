package com.dashx.graphql;

import static org.junit.jupiter.api.Assertions.*;

import com.dashx.exception.DashXException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

class KeysetPagerTest {

    record Row(String id, String createdAt) {}

    /**
     * Rows with runs of identical timestamps, including a run longer than a page, so ties
     * straddle page boundaries.
     */
    private static List<Row> rowsWithTies() {
        int[] runLengths = { 1, 3, 1, 12, 2, 1, 4, 7, 1, 1, 5, 2 };
        List<Row> rows = new ArrayList<>();
        int n = 0;
        for (int run = 0; run < runLengths.length; run++) {
            String createdAt = String.format("2026-11-05T10:00:%02d.000000Z", run);
            for (int i = 0; i < runLengths[run]; i++) {
                rows.add(new Row(String.format("id-%03d", n++), createdAt));
            }
        }
        return rows;
    }

    /**
     * Applies the filter the way the server does: createdAt >= cursor, every `and` id excluded,
     * ordered by createdAt only, with ties in a different random order on every request.
     */
    @SuppressWarnings("unchecked")
    private static KeysetPager.Fetch<Row> server(List<Row> rows, Random random, List<Map<String, Object>> seenFilters) {
        return (filter, order, limit) -> {
            seenFilters.add(filter);
            assertEquals(KeysetPager.ORDER, order);
            String gte = filter.containsKey("createdAt")
                ? (String) ((Map<String, Object>) filter.get("createdAt")).get("gte")
                : null;
            Set<String> excluded = new HashSet<>();
            for (Map<String, Object> clause : (List<Map<String, Object>>) filter.getOrDefault("and", List.of())) {
                excluded.add((String) ((Map<String, Object>) clause.get("id")).get("neq"));
            }
            List<Row> matches = rows.stream()
                .filter(r -> gte == null || r.createdAt().compareTo(gte) >= 0)
                .filter(r -> !excluded.contains(r.id()))
                .collect(Collectors.toCollection(ArrayList::new));
            Collections.shuffle(matches, random);
            matches.sort(Comparator.comparing(Row::createdAt));
            return Mono.just(List.copyOf(matches.subList(0, Math.min(limit, matches.size()))));
        };
    }

    private static KeysetPager<Row> pager(int pageSize, int maxRequests, KeysetPager.Fetch<Row> fetch) {
        return new KeysetPager<>("Rows", pageSize, maxRequests, Row::id, Row::createdAt, fetch);
    }

    @Test
    void readsEveryRowExactlyOnceWhenTiesStraddlePages() {
        List<Row> rows = rowsWithTies();
        for (int seed = 0; seed < 200; seed++) {
            List<Map<String, Object>> filters = new ArrayList<>();
            List<Row> read = pager(5, 100, server(rows, new Random(seed), filters))
                .fetchAll(Map.of("objectId", Map.of("eq", "x")))
                .block();

            assertEquals(rows.size(), read.size(), "seed " + seed);
            assertEquals(
                rows.stream().map(Row::id).collect(Collectors.toSet()),
                read.stream().map(Row::id).collect(Collectors.toSet()),
                "seed " + seed
            );
            for (int i = 1; i < read.size(); i++) {
                assertTrue(read.get(i - 1).createdAt().compareTo(read.get(i).createdAt()) <= 0);
            }
            // The caller's filter is kept on every request.
            filters.forEach(f -> assertEquals(Map.of("eq", "x"), f.get("objectId")));
        }
    }

    @Test
    void firstRequestHasNoCursor() {
        List<Map<String, Object>> filters = new ArrayList<>();
        pager(5, 10, server(List.of(new Row("a", "t1")), new Random(0), filters))
            .fetchAll(Map.of("objectId", Map.of("eq", "x")))
            .block();

        assertEquals(1, filters.size());
        assertEquals(Map.of("objectId", Map.of("eq", "x")), filters.get(0));
    }

    @Test
    void failsInsteadOfTruncatingAtTheRequestCap() {
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < 11; i++) rows.add(new Row("id-" + i, String.format("t%02d", i)));

        DashXException e = assertThrows(
            DashXException.class,
            () -> pager(5, 2, server(rows, new Random(0), new ArrayList<>())).fetchAll(Map.of()).block()
        );
        assertTrue(e.getMessage().contains("10 or more"), e.getMessage());
    }

    @Test
    void failsIfTheServerIgnoresTheCursor() {
        List<Row> page = List.of(new Row("a", "t1"), new Row("b", "t2"));

        DashXException e = assertThrows(
            DashXException.class,
            () -> pager(2, 10, (filter, order, limit) -> Mono.just(page)).fetchAll(Map.of()).block()
        );
        assertTrue(e.getMessage().contains("twice"), e.getMessage());
    }

    @Test
    void rejectsAFilterThatUsesThePagersOwnKeys() {
        KeysetPager<Row> pager = pager(5, 10, (filter, order, limit) -> Mono.just(List.of()));
        assertThrows(IllegalArgumentException.class, () -> pager.fetchAll(Map.of("createdAt", Map.of())));
        assertThrows(IllegalArgumentException.class, () -> pager.fetchAll(Map.of("and", List.of())));
    }
}
