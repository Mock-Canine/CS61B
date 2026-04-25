# Hyponyms Handler Notes

Saved on 2026-04-24.

`Collection.emptylist()` is slightly off. The actual method is `Collections.emptyList()`.

It means: "give me one shared empty list object."
It is useful in `src/main/Graph.java` because your current code creates a brand new empty `ArrayList` every time a node has no outgoing edges:

```java
public Collection<String> adj(String from) {
    return graph.getOrDefault(from, new ArrayList<>());
}
```

A better version is:

```java
public Collection<String> adj(String from) {
    return graph.getOrDefault(from, Collections.emptyList());
}
```

Why this is a little better:
- `new ArrayList<>()` allocates a new object every miss
- `Collections.emptyList()` reuses one immutable empty list
- same behavior for your use here, less garbage, cleaner intent

`Collections.emptyList()` is not a huge optimization, just a good habit.

For the graph identifier question: using `String` synset IDs works, but `int` is usually better here because the synset IDs in the file are already numbers like `0`, `1`, `2`, ...
With `String` IDs:
- every key is a `String` object
- hash map lookups must hash and compare strings
- you store more memory per vertex/edge endpoint

With `int` IDs:
- comparison is cheaper
- storage is smaller
- you can use array-based structures

Your concern about sparse nodes is valid, but that actually argues for an adjacency list, not against it.

Two important distinctions:
- adjacency list: `O(V + E)` space
- adjacency matrix: `O(V^2)` space

WordNet is sparse. Most synsets do not connect to most other synsets. So adjacency list is the right choice.

Even if many nodes have no outgoing edges, adjacency list is still fine. You have two common ways to represent it:

1. `Map<Integer, List<Integer>>`
- only store entries for vertices that actually have outgoing edges
- best if you want to avoid empty slots entirely

2. `List<List<Integer>>` or `List<Integer>[]`
- store one slot per vertex
- empty slots are cheap
- still far better than strings in maps for this project

So your intuition was partly right:
- sparse graph -> adjacency list is good
- many empty nodes do not make adjacency list bad
- they only mean you should avoid adjacency matrix

For this project, the best practical data design is usually:
- `Map<String, Set<Integer>> wordToSynsetIds`
- `List<String[]> synsetIdToWords`
- `Map<Integer, List<Integer>> adj` or `List<List<Integer>> adj`

That keeps words as strings, but synset IDs as ints.

For `HyponymsHandler`, "optimal" has two parts: correctness and performance.

## 1. Correct `commonWords` first

Your current issue is that "not initialized yet" and "already empty" are mixed together.
A strong version is:

```java
private Set<String> commonWords(List<String> words) {
    List<Set<String>> allSets = new ArrayList<>();

    for (String word : new LinkedHashSet<>(words)) {
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

    Set<String> common = new HashSet<>(allSets.get(0));
    for (int i = 1; i < allSets.size() && !common.isEmpty(); i++) {
        common.retainAll(allSets.get(i));
    }
    return common;
}
```

Why this is better:
- deduplicates repeated query words
- returns early if one word has no hyponyms
- intersects the smallest sets first, which is faster

That last point is the main optimization. If one query word has only 20 hyponyms and another has 5000, start from the 20-set.

## 2. For `k == 0`

This is simple:
- compute common hyponyms
- sort alphabetically
- return them

That is already near-optimal for the task.

## 3. For `k > 0`

Your current code:
- computes counts for every candidate
- sorts the entire result list

That is correct, but not asymptotically optimal when `k` is small.

A better strategy:
- compute total count for each candidate
- ignore words with total count `0`
- keep only the best `k` words in a min-heap (`PriorityQueue`)

That changes the ranking step from:
- full sort: `O(m log m)`

to:
- heap of size `k`: `O(m log k)`

where `m` is the number of common hyponyms.

A clean structure is:

```java
private static class WordCount {
    String word;
    double count;

    WordCount(String word, double count) {
        this.word = word;
        this.count = count;
    }
}
```

```java
private List<String> topK(Set<String> words, int startYear, int endYear, int k) {
    Comparator<WordCount> worstFirst = (a, b) -> {
        int cmp = Double.compare(a.count, b.count);
        if (cmp != 0) {
            return cmp;
        }
        return b.word.compareTo(a.word);
    };

    PriorityQueue<WordCount> pq = new PriorityQueue<>(worstFirst);

    for (String word : words) {
        double total = countSum(word, startYear, endYear);
        if (total <= 0) {
            continue;
        }

        WordCount candidate = new WordCount(word, total);

        if (pq.size() < k) {
            pq.add(candidate);
        } else if (worstFirst.compare(candidate, pq.peek()) > 0) {
            pq.poll();
            pq.add(candidate);
        }
    }

    List<String> result = new ArrayList<>();
    while (!pq.isEmpty()) {
        result.add(pq.poll().word);
    }
    result.sort(null);
    return result;
}
```

Why the tie rule looks weird:
- heap top should be the "worst" among current top `k`
- smaller count is worse
- if counts tie, alphabetically larger word is worse
- that way alphabetically smaller words survive ties, which matches the project behavior

Then `handle` becomes conceptually:

```java
@Override
public String handle(NgordnetQuery q) {
    Set<String> common = commonWords(q.words());
    List<String> result;

    if (q.k() == 0) {
        result = new ArrayList<>(common);
        result.sort(null);
    } else {
        result = topK(common, q.startYear(), q.endYear(), q.k());
    }

    return result.toString();
}
```

A few extra cleanup suggestions for `HyponymsHandler`:
- avoid mutating `common` inside helper methods like `rmZeroCount`; returning a new result is easier to reason about
- avoid using `Double.NaN` as a sentinel value; it works, but it makes the logic less clear
- `countSum` can just return `0.0` if there is no history, and the caller can skip `<= 0`

So the short answer is:

- `Collections.emptyList()` avoids unnecessary empty-list allocations.
- Sparse graph does not make adjacency lists bad; it makes them the right choice.
- Using `int` synset IDs is more efficient than `String` IDs because lookup/storage is cheaper.
- The best `HyponymsHandler` strategy is:
  1. deduplicate query words
  2. build each hyponym set once
  3. intersect smallest sets first
  4. for `k > 0`, use a size-`k` min-heap instead of sorting everything
