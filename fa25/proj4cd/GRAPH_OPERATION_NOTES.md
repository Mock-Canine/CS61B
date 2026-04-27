# Algorithm Design Notes From Project 4C/4D

Saved on 2026-04-25.

```java
/**
 * Universal ideas in this note:
 * 1. Multi-source BFS: one traversal from several start nodes.
 * 2. visited / marked: avoid duplicate processing in graph traversal.
 * 3. Smallest-first intersection: shrink the candidate set early.
 * 4. PriorityQueue: keep only top k items, avoid sorting the whole universe.
 * 5. Helper record / class: make comparator and tie-breaking explicit.
 * 6. Cache repeated aggregations when the source data is static.
 */
```

## 1. `WorldNet.getHyponyms`: Multi-Source BFS

```java
/**
 * Universal idea:
 * - If one query corresponds to several start vertices, enqueue all of them first
 *   and run one BFS.
 * - This is the standard pattern for "reachable from any source in this set".
 *
 * Why:
 * - one queue, one visited set
 * - no repeated BFS setup
 * - shared descendants are processed once
 */
public Set<String> getHyponyms(String word) {
    Set<String> hyponyms = new HashSet<>();
    Set<Integer> marked = new HashSet<>();
    Queue<Integer> queue = new ArrayDeque<>(
            wordSynsets.getOrDefault(word, Collections.emptyList()));

    while (!queue.isEmpty()) {
        Integer synset = queue.poll();
        if (!marked.add(synset)) {
            continue;
        }
        hyponyms.addAll(List.of(synsets.get(synset)));
        queue.addAll(graph.adj(synset));
    }
    return hyponyms;
}
```

## 2. `commonWords`: Fail Fast + Smallest-First Intersection

```java
/**
 * Universal idea:
 * - For "common reachable nodes / common neighbors / common candidates",
 *   compute each candidate set first.
 * - If any set is empty, stop immediately.
 * - Intersect the smallest set first so the running answer shrinks early.
 *
 * Why:
 * - fail-fast avoids useless later work
 * - smallest-first usually reduces total retainAll cost
 */
private Set<String> commonWords(List<String> words) {
    List<Set<String>> allSets = new ArrayList<>();

    for (String word : new HashSet<>(words)) {
        Set<String> hyponyms = wn.getHyponyms(word);
        if (hyponyms.isEmpty()) {
            return new HashSet<>();
        }
        allSets.add(hyponyms);
    }

    if (allSets.isEmpty()) {
        return new HashSet<>();
    }

    allSets.sort(Comparator.comparingInt(Set::size));

    Set<String> common = new HashSet<>(allSets.getFirst());
    for (int i = 1; i < allSets.size() && !common.isEmpty(); i++) {
        common.retainAll(allSets.get(i));
    }
    return common;
}
```

## 3. `selectK`: Helper Record + Comparator + Priority Queue

```java
/**
 * Universal idea:
 * - When ranking by several fields, define a small helper type first.
 * - Then write one comparator that captures the exact ranking rule.
 *
 * Why:
 * - cleaner than mixing parallel maps and ad hoc comparator logic
 * - tie-breaking becomes explicit and reusable
 */
private record WordCount(String word, double count) {}

/**
 * Universal idea:
 * - If only the best k items are needed, keep a min-heap of size k.
 * - Do not sort the whole universe unless k is close to n.
 *
 * Why:
 * - full sort: O(n log n)
 * - heap of size k: O(n log k)
 *
 * Note:
 * - ranking order and output order can be different problems
 * - here we rank by count, then alphabetize final output later
 */
private List<String> selectK(Set<String> common, int startYear, int endYear, int k) {
    Comparator<WordCount> worstFirst = (a, b) -> {
        int count = Double.compare(a.count(), b.count());
        if (count != 0) {
            return count;
        }
        return b.word().compareTo(a.word());
    };

    PriorityQueue<WordCount> pq = new PriorityQueue<>(worstFirst);

    for (String word : common) {
        double count = countSum(word, startYear, endYear);
        if (count <= 0) {
            continue;
        }

        WordCount wc = new WordCount(word, count);
        if (pq.size() < k) {
            pq.add(wc);
        } else if (worstFirst.compare(wc, pq.peek()) > 0) {
            pq.poll();
            pq.add(wc);
        }
    }

    List<String> result = new ArrayList<>();
    while (!pq.isEmpty()) {
        result.add(pq.poll().word());
    }
    return result;
}
```

## 4. `countSum`: Aggregation and Optional Cache

```java
/**
 * Universal idea:
 * - This function is an aggregation over a query result.
 * - The current version is simple and correct.
 * - If the same (word, startYear, endYear) query repeats often, cache it.
 *
 * General analogy:
 * - This is the same idea as caching repeated database aggregations.
 *
 * Trade-off:
 * - no cache: simpler, but repeats work
 * - with cache: faster repeated queries, but uses more memory
 */
private double countSum(String word, int startYear, int endYear) {
    TreeMap<Integer, Double> countHistory = ngm.countHistory(word, startYear, endYear);
    double result = 0.0;
    for (double v : countHistory.values()) {
        result += v;
    }
    return result;
}
```

```java
/**
 * Optional cached version:
 * - Memoize the final aggregation result.
 * - Good when the underlying data is static and the same queries repeat.
 */
private record CountKey(String word, int startYear, int endYear) {}

private final Map<CountKey, Double> countCache = new HashMap<>();

private double countSum(String word, int startYear, int endYear) {
    CountKey key = new CountKey(word, startYear, endYear);
    Double cached = countCache.get(key);
    if (cached != null) {
        return cached;
    }

    TreeMap<Integer, Double> countHistory = ngm.countHistory(word, startYear, endYear);
    double result = 0.0;
    for (double v : countHistory.values()) {
        result += v;
    }

    countCache.put(key, result);
    return result;
}
```

## 5. Separate Note

```java
/**
 * This note focuses on algorithm design only.
 * Graph representation trade-offs such as Map<Integer, List<Integer>>
 * vs int[][] should live in a separate note.
 */
```
