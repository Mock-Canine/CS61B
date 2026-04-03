package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {
    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof MyHashMap<?, ?>.Node other) {
                return other.key.equals(key);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }
    }

    private class hashMapIterator implements Iterator<K> {
        private int iterSize;
        private int iterIdx;
        private Iterator<Node> bucketIterator;

        public hashMapIterator() {
           iterSize = 0;
           iterIdx = 0;
           bucketIterator = buckets[iterIdx].iterator();
        }

        @Override
        public boolean hasNext() {
            return iterSize != size;
        }

        @Override
        public K next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more elements");
            }
            iterSize++;
            while (!bucketIterator.hasNext()) {
                bucketIterator = buckets[++iterIdx].iterator();
            }
            return bucketIterator.next().key;
        }
    }

    /* Instance Variables */
    private int capacity;
    private final double loadFactor;
    private Collection<Node>[] buckets;

    // # total elements
    private int size = 0;
    private int initialCapacity = 16;
    private final int resizeFactor = 2;
    private final double defaultloadFacotr = 0.75;

    /** Constructors */
    public MyHashMap() {
        capacity = initialCapacity;
        loadFactor = defaultloadFacotr;
        buckets = initTable(capacity);
    }

    public MyHashMap(int initialCapacity) {
        capacity = initialCapacity;
        loadFactor = defaultloadFacotr;
        buckets = initTable(capacity);
        this.initialCapacity = initialCapacity;
    }

    /**
     * MyHashMap constructor that creates a backing array of initialCapacity.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialCapacity initial size of backing array
     * @param loadFactor maximum load factor
     */
    public MyHashMap(int initialCapacity, double loadFactor) {
        capacity = initialCapacity;
        this.loadFactor = loadFactor;
        buckets = initTable(capacity);
        this.initialCapacity = initialCapacity;
    }

    /**
     * Init an empty hash table with empty buckets
     */
    private Collection<Node>[] initTable(int capacity) {
        Collection<Node>[] table = new Collection[capacity];
        for (int i = 0; i < capacity; i++) {
            table[i] = createBucket();
        }
        return table;
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *  Note that this is referring to the hash table bucket itself,
     *  not the hash map itself.
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map already contains the specified key, replaces the key's mapping
     * with the value specified.
     */
    @Override
    public void put(K key, V value) {
        double factor = (double) size / capacity;
        if (factor >= loadFactor) {
            resize();
        }
        Node node = new Node(key, value);
        int idx = hashIdx(node);
        if (!buckets[idx].remove(node)) {
            size++;
        }
        buckets[idx].add(node);
    }

    private void resize() {
        capacity *= resizeFactor;
        Collection<Node>[] newBuckets = initTable(capacity);
        for (Collection<Node> bucket : buckets) {
            for (Node n : bucket) {
                newBuckets[hashIdx(n)].add(n);
            }
        }
        buckets = newBuckets;
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     */
    @Override
    public V get(K key) {
        Node tmp = new Node(key, null);
        for (Node n : buckets[hashIdx(tmp)]) {
            if (n.equals(tmp)) {
                return n.value;
            }
        }
        return null;
    }

    /**
     * Returns whether this map contains a mapping for the specified key.
     */
    @Override
    public boolean containsKey(K key) {
        Node tmp = new Node(key, null);
        return buckets[hashIdx(tmp)].contains(tmp);
    }

    /**
     * Returns the number of key-value mappings in this map.
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Removes every mapping from this map.
     */
    @Override
    public void clear() {
        capacity = initialCapacity;
        buckets = initTable(initialCapacity);
        size = 0;
    }

    /**
     * Returns a Set view of the keys contained in this map. Not required for this lab.
     * If you don't implement this, throw an UnsupportedOperationException.
     */
    @Override
    public Set<K> keySet() {
        Set<K> set = new HashSet<>();
        for (K key : this) {
            set.add(key);
        }
        return set;
    }

    /**
     * Removes the mapping for the specified key from this map if present,
     * or null if there is no such mapping.
     * Not required for this lab. If you don't implement this, throw an
     * UnsupportedOperationException.
     *
     * @param key
     */
    @Override
    public V remove(K key) {
        V value = null;
        Node tmp = new Node(key, null);
        int idx = hashIdx(tmp);
        for (Node n : buckets[idx]) {
            if (n.equals(tmp)) {
                value = n.value;
                buckets[idx].remove(n);
                size--;
            }
        }
        return value;
    }

    @Override
    public Iterator<K> iterator() {
        return new hashMapIterator();
    }

    /**
     * Calculate the entry for a node
     */
    private int hashIdx(Node node) {
        return Math.floorMod(node.hashCode(), capacity);
    }
}
