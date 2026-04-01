import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    private class BSTNode {
        private K key;
        private V value;
        private BSTNode left;
        private BSTNode right;

        BSTNode(K k, V v) {
            key = k;
            value = v;
        }
    }

    private class BSTIterator implements Iterator<K>{
        private final Iterator<K> keySetIterator;
        public BSTIterator() {
            this.keySetIterator = keySet().iterator();
        }

        @Override
        public boolean hasNext() {
            return keySetIterator.hasNext();
        }

        @Override
        public K next() {
            return keySetIterator.next();
        }
    }

    private BSTNode root;
    private int size;
    /**
     * Associates the specified value with the specified key in this map.
     * If the map already contains the specified key, replaces the key's mapping
     * with the value specified.
     *
     * @param key
     * @param value
     */
    @Override
    public void put(K key, V value) {
        root = putHelper(key, value, root);
    }

    private BSTNode putHelper(K key, V value, BSTNode node) {
        if (node == null) {
            this.size++;
            return new BSTNode(key, value);
        }
        if (node.key.equals(key)) {
            node.value = value;
        } else if (node.key.compareTo(key) > 0){
            node.left = putHelper(key, value, node.left);
        } else {
            node.right = putHelper(key, value, node.right);
        }
        return node;
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     *
     * @param key
     */
    @Override
    public V get(K key) {
        return getHelper(key, this.root);
    }

    private V getHelper(K key, BSTNode node) {
        if (node == null) {
            return null;
        }
        if (node.key.equals(key)) {
            return node.value;
        } else if (node.key.compareTo(key) > 0){
            return getHelper(key, node.left);
        } else {
            return getHelper(key, node.right);
        }
    }

    /**
     * Returns whether this map contains a mapping for the specified key.
     *
     * @param key
     */
    @Override
    public boolean containsKey(K key) {
        return containHelper(key, root);
    }

    private boolean containHelper(K key, BSTNode node) {
        if (node == null) {
            return false;
        }
        if (node.key.equals(key)) {
            return true;
        } else if (node.key.compareTo(key) > 0){
            return containHelper(key, node.left);
        } else {
            return containHelper(key, node.right);
        }
    }

    /**
     * Returns the number of key-value mappings in this map.
     */
    @Override
    public int size() {
        return this.size;
    }

    /**
     * Removes every mapping from this map.
     */
    @Override
    public void clear() {
        this.root = null;
        this.size = 0;
    }

    /**
     * Returns a Set view of the keys contained in this map. Not required for Lab 7.
     * If you don't implement this, throw an UnsupportedOperationException.
     */
    @Override
    public Set<K> keySet() {
        Set<K> set = new TreeSet<>();
        keySetHelper(set, root);
        return set;
    }

    // Mutator, input set will not be null, so it does not have to return
    private void keySetHelper(Set<K> set, BSTNode node) {
        if (node == null) {
            return;
        }
        set.add(node.key);
        keySetHelper(set, node.left);
        keySetHelper(set, node.right);
    }

    /**
     * Removes the mapping for the specified key from this map if present,
     * or null if there is no such mapping.
     * Not required for Lab 7. If you don't implement this, throw an
     * UnsupportedOperationException.
     *
     * @param key
     */
    @Override
    public V remove(K key) {
        BSTNode removed = new BSTNode(null, null);
        this.root = removeHelper(key, this.root, removed);
        if (removed.value != null) {
            this.size--;
        }
        return removed.value;
    }

    /**
     * Remove the (key, value) pair of node, constructively, return itself
     * Mutate the value in the input removed node if found
     */
    private BSTNode removeHelper(K key, BSTNode node, BSTNode removed) {
        if (node == null) {
            return null;
        }
        if (node.key.equals(key)) {
            removed.value = node.value;
            if (node.left == null || node.right == null) {
                 return node.left == null ? node.right : node.left;
            } else {
                BSTNode subMax = new BSTNode(null, null);
                node.left = removeMax(node.left, subMax);
                node.key = subMax.key;
                node.value = subMax.value;
            }
        } else if (node.key.compareTo(key) > 0){
            node.left = removeHelper(key, node.left, removed);
        } else {
            node.right = removeHelper(key, node.right, removed);
        }
        return node;
    }

    /**
     * Remove the largest item in the node, and put its (key, value) in removed
     */
    private BSTNode removeMax(BSTNode node, BSTNode removed) {
        if (node.right == null) {
            removed.key = node.key;
            removed.value = node.value;
            return node.left;
        }
        node.right = removeMax(node.right, removed);
        return node;
    }

    @Override
    public Iterator<K> iterator() {
        return new BSTIterator();
    }
}
