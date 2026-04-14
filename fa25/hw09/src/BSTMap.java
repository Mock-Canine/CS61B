import java.util.*;

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

    private class BSTIteratorPre implements Iterator<K> {

        private Stack<BSTNode> keyStack;

        BSTIteratorPre() {
            keyStack = new Stack<>();
            pushNoNull(root);
        }

        private void pushNoNull(BSTNode node) {
            if (node != null) {
                keyStack.push(node);
            }
        }

        @Override
        public boolean hasNext() {
            return !keyStack.isEmpty();
        }

        @Override
        public K next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No element any more");
            }
            BSTNode node = keyStack.pop();
            pushNoNull(node.right);
            pushNoNull(node.left);
            return node.key;
        }
    }

    private class BSTIterator implements Iterator<K> {
        // Store the whole node
        private Stack<BSTNode> keyStack;

        BSTIterator() {
            this.keyStack = new Stack<>();
            pushLeft(root);
        }

        /**
         * Push the left-most children of a node into stack,
         * simulate the in-order tree traversal
         */
        private void pushLeft(BSTNode node) {
            while(node != null) {
                keyStack.push(node);
                node = node.left;
            }
        }

        @Override
        public boolean hasNext() {
            return !keyStack.isEmpty();
        }

        @Override
        public K next() {
            if (!hasNext()) {
                // Convention
                throw new NoSuchElementException("No element any more");
            }
            BSTNode node = keyStack.pop();
            pushLeft(node.right);
            return node.key;
        }
    }

    private class BSTIteratorPos implements Iterator<K> {

        private Stack<BSTNode> keyStack;

        BSTIteratorPos() {
            keyStack = new Stack<>();
            pushToLeaf(root);
        }

        private void pushToLeaf(BSTNode node) {
            /* Iteration is simple here, so do not use recursion.
             * By the way, the stack of postorder can be generated immediately in constructor
             * using push(node) -> push(node.right) -> push(node.left)
             * but it will store all the nodes in the stack. while this method will only store
             * O(H) nodes, H is the height of the tree, and push, pop the stack when next()
             * get called, which is the purpose of an iterator.(lazy loading, rather than output
             * all the possible values)
             */
            while(node != null) {
                keyStack.push(node);
                if (node.left != null) {
                    node = node.left;
                } else {
                    node = node.right;
                }
            }
        }

        @Override
        public boolean hasNext() {
            return !keyStack.isEmpty();
        }

        @Override
        public K next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No element any more");
            }
            BSTNode node = keyStack.pop();
            if (hasNext()) {
                BSTNode parent = keyStack.peek();
                if (node == parent.left) {
                    pushToLeaf(parent.right);
                }
            }
            return node.key;
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
        // Do not mix .equal()
        int cmp = node.key.compareTo(key);
        if (cmp == 0) {
            node.value = value;
        } else if (cmp > 0){
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
        int cmp = node.key.compareTo(key);
        if (cmp == 0) {
            return node.value;
        } else if (cmp > 0){
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
        int cmp = node.key.compareTo(key);
        if (cmp == 0) {
            return true;
        } else if (cmp > 0){
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
        return removed.value;
    }

    /**
     * Remove the (key, value) pair of node, constructively, return itself
     * Mutate the value in the input removed node if found
     */
    /* Note that different from put() which only need to modify the tree structure, this
     * method needs to return the value of the deleted node.
     * Two methods to modify tree structure:
     *      pass parent as argument and return void(see past git log for my implementation, very verbose)
     *      return the node itself(change the reference of the node to newly refactored one)
     * How to output extra information(removed):
     *      get the information first then remove(use get(key) then remove)
     *      use dummy node as argument
     */
    private BSTNode removeHelper(K key, BSTNode node, BSTNode removed) {
        if (node == null) {
            return null;
        }
        int cmp = node.key.compareTo(key);
        if (cmp == 0) {
            removed.value = node.value;
            this.size--;
            // Less curly braces, more readable
            if (node.left == null) return node.right;
            if (node.right == null) return node.left;
            BSTNode subMax = new BSTNode(null, null);
            node.left = removeMax(node.left, subMax);
            node.key = subMax.key;
            node.value = subMax.value;
        } else if (cmp > 0){
            node.left = removeHelper(key, node.left, removed);
        } else {
            node.right = removeHelper(key, node.right, removed);
        }
        return node;
    }

    /**
     * Remove the largest item in the node tree, and put its (key, value) in removed
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
        return new BSTIteratorPos();
    }

    void main() {
        Map61B<Integer, Integer> map = new BSTMap<>();
        map.put(4, 4);
        map.put(1, 1);
        map.put(3, 3);
        map.put(6, 6);
        map.put(2, 2);
        map.put(5, 5);
        for (Integer k : map) {
            IO.println(k);
        }
    }
}
