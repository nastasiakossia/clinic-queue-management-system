/**
 * Generic 2-3 tree storing values indexed by string keys.
 * Used as the base index structure for doctors and patients.
 */
public class Tree<T extends HasKey> {
    public Node<T> root;

    /**
     * Returns the value associated with the given key, or null if the key is absent.
     */
    public T search(String id) {
        Node<T> n = searchNode(root, id);
        return (n == null) ? null : n.value;
    }

    /**
     * Returns the value associated with the given key, or null if the key is absent.
     */
    public void insert(T value) {
        Node<T> z = new Node<T>();
        z.value = value;
        z.key = value.id();
        insertLeaf(z);
    }

    private Node<T> searchNode(Node<T> x, String k) {
        if (x == null) return null;

        if (x.isLeaf()) {
            return k.equals(x.key) ? x : null;
        }
        if (k.compareTo(x.left.key) <= 0) {
            return searchNode(x.left, k);
        } else if (x.right == null || k.compareTo(x.middle.key) <= 0) {
            return searchNode(x.middle, k);
        } else {
            return searchNode(x.right, k);
        }
    }

    private void insertLeaf(Node<T> z) {
        if (root == null) {
            root = z;
            return;
        }

        Node<T> y = root;
        while (!y.isLeaf()) {
            if (z.key.compareTo(y.left.key) <= 0) {
                y = y.left;
            } else if (y.right == null || z.key.compareTo(y.middle.key) <= 0) {
                y = y.middle;
            } else {
                y = y.right;
            }
        }


        if (y == null) {
            throw new IllegalStateException("Tree invariant violated: reached null while descending");
        }


        if (y.parent == null) {
            Node<T> w = new Node<T>();
            if (z.key.compareTo(y.key) < 0) setChildren(w, z, y, null);
            else setChildren(w, y, z, null);
            root = w;
            return;
        }

        Node<T> x = y.parent;
        Node<T> zPrime = insertAndSplit(x, z);

        while (x != root) {
            x = x.parent;
            if (zPrime != null) zPrime = insertAndSplit(x, zPrime);
            else {
                updateKey(x);
            }
        }

        if (zPrime != null) {
            Node<T> w = new Node<T>();
            setChildren(w, root, zPrime, null);
            root = w;
        }
    }


    private void updateKey(Node<T> x) {
        if (x == null || x.isLeaf()) return;

        if (x.middle == null) {
            x.key = x.left.key;
            return;
        }

        x.key = (x.right != null) ? x.right.key : x.middle.key;

    }

    private void setChildren(Node<T> x, Node<T> l, Node<T> m, Node<T> r) {
        x.left = l;
        x.middle = m;
        x.right = r;
        l.parent = x;
        if (m != null) m.parent = x;
        if (r != null) r.parent = x;

        updateKey(x);
    }

    /**
     * Splits a full internal node after insertion.
     * Returns the newly created sibling if a split occurs, otherwise null.
     */
    private Node<T> insertAndSplit(Node<T> x, Node<T> z) {
        Node<T> l = x.left, m = x.middle, r = x.right;

        if (r == null) {
            if (z.key.compareTo(l.key) < 0) setChildren(x, z, l, m);
            else if (z.key.compareTo(m.key) < 0) setChildren(x, l, z, m);
            else setChildren(x, l, m, z);
            return null;
        }

        Node<T> y = new Node<T>();

        if (z.key.compareTo(l.key) < 0) {
            setChildren(x, z, l, null);
            setChildren(y, m, r, null);
        } else if (z.key.compareTo(m.key) < 0) {
            setChildren(x, l, z, null);
            setChildren(y, m, r, null);
        } else if (z.key.compareTo(r.key) < 0) {
            setChildren(x, l, m, null);
            setChildren(y, z, r, null);
        } else {
            setChildren(x, l, m, null);
            setChildren(y, r, z, null);
        }

        return y;
    }

    /**
     * Restores 2-3 tree invariants after deletion by borrowing from or merging with a sibling.
     */
    private Node<T> borrowOrMerge(Node<T> y) {
        Node<T> z = y.parent;
        if (y == z.left) {
            Node<T> x = z.middle;
            if (x.right != null) {
                setChildren(y, y.left, x.left, null);
                setChildren(x, x.middle, x.right, null);
            } else {
                setChildren(x, y.left, x.left, x.middle);
                setChildren(z, x, z.right, null);
            }
            return z;
        }
        if (y == z.middle) {
            Node<T> x = z.left;
            if (x.right != null) {
                setChildren(y, x.right, y.left, null);
                setChildren(x, x.left, x.middle, null);
            } else {
                setChildren(x, x.left, x.middle, y.left);
                setChildren(z, x, z.right, null);
            }
            return z;
        }
        Node<T> x = z.middle;
        if (x.right != null) {
            setChildren(y, x.right, y.left, null);
            setChildren(x, x.left, x.middle, null);
        } else {
            setChildren(x, x.left, x.middle, y.left);
            setChildren(z, z.left, x, null);
        }
        return z;
    }

    public void delete(String key) {
        Node<T> node = searchNode(root, key);
        if (node != null) {
            deleteNode(node);
        }
    }

    /**
     * Restores 2-3 tree invariants after deletion by borrowing from or merging with a sibling.
     */
    private void deleteNode(Node<T> x) {
        Node<T> y = x.parent;
        if (y == null) {
            root = null;
            return;
        }
        if (x == y.left) setChildren(y, y.middle, y.right, null);
        else if (x == y.middle) setChildren(y, y.left, y.right, null);
        else setChildren(y, y.left, y.middle, null);
        while (y != null) {
            if (y.middle != null) {
                updateKey(y);
                y = y.parent;
            } else {
                if (y != root) y = borrowOrMerge (y);
                else {
                    root = y.left;
                    y.left.parent = null;
                    return;
                }
            }
        }
    }
}
