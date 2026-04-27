# Graph Representation Notes

Saved on 2026-04-25.

```java
/**
 * Universal ideas in this note:
 * 1. Sparse graph usually means adjacency list, not adjacency matrix.
 * 2. Build-time convenience and query-time efficiency are different goals.
 * 3. Static graphs can justify compact primitive representations.
 * 4. Two-pass construction is a standard way to build int[][] safely.
 */
```

## 1. Sparse Graph: `Map<Integer, List<Integer>>`

```java
/**
 * Why this is a reasonable default:
 * - simple incremental construction
 * - easy getOrDefault query style
 * - only vertices with outgoing edges need map entries
 *
 * Trade-off:
 * - hash lookup overhead
 * - boxed Integer overhead
 */
Map<Integer, List<Integer>> graph = new HashMap<>();
```

## 2. Static Graph: `int[][]`

```java
/**
 * Why this can be better for a static graph:
 * - no boxing
 * - fast indexed access
 * - smaller constant factors than map-based adjacency
 *
 * Trade-off:
 * - harder to build directly
 * - usually needs one more pass over the input
 */
int[][] adj;
```

## 3. Two-Pass Build Pattern

```java
/**
 * Standard static-graph construction pattern:
 * 1. first pass: count out-degree
 * 2. allocate exact-size arrays
 * 3. second pass: fill adjacency arrays
 */
int[] outDegree = new int[numVertices];
int[][] adj = new int[numVertices][];
int[] nextIndex = new int[numVertices];
```
