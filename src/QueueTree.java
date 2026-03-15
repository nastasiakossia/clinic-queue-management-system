/**
 * 2-3 tree indexed by doctor load (queue size).
 * Each leaf represents a load bucket and stores:
 * - the number of doctors with this load,
 * - the total number of patients contributed by this bucket,
 * - a linked list of doctors sharing the same load.
 */
public class QueueTree {
    public QNode root;

    public QNode search(int key) {
        return searchNode(root, key);
    }

    public void addDoctorToBucket(QNode q, Doctor d) {
        d.loadPrev = q.tailDoctor;
        d.loadNext = null;

        if (q.tailDoctor != null) {
            q.tailDoctor.loadNext = d;
        } else {
            q.headDoctor = d;
        }

        q.tailDoctor = d;
    }

    public void removeDoctorFromBucket(QNode bucket, Doctor doctor) {
        if (doctor.loadPrev != null) {
            doctor.loadPrev.loadNext = doctor.loadNext;
        } else {
            bucket.headDoctor = doctor.loadNext;
        }

        if (doctor.loadNext != null) {
            doctor.loadNext.loadPrev = doctor.loadPrev;
        } else {
            bucket.tailDoctor = doctor.loadPrev;
        }

        doctor.loadPrev = null;
        doctor.loadNext = null;
    }

    /**
     * Inserts a new load bucket for the given doctor.
     */
    public void insert(int value, Doctor d) {
        QNode z = new QNode();
        z.key = value;
        z.docCount = 1;
        z.patientSum = value;
        z.headDoctor = null;
        z.tailDoctor = null;
        addDoctorToBucket(z, d);
        insertLeaf(z);
    }

    /**
     * Inserts a new load bucket for the given doctor.
     */
    public int upperBound(int k) {
        QNode x = root;
        int ans = -1;

        while (x != null) {
            if (x.isLeaf()) {
                if (x.key <= k) ans = x.key;
                break;
            }

            if (k <= x.left.key) {
                x = x.left;
            } else if (x.right == null || k <= x.middle.key) {
                ans = x.left.key;
                x = x.middle;
            } else {
                ans = x.middle.key;
                x = x.right;
            }
        }
        return ans;
    }


    public QNode searchNode(QNode x, int k) {
        if (x == null) return null;

        if (x.isLeaf()) return (k == x.key) ? x : null;

        if (k <= x.left.key) {
            return searchNode(x.left, k);
        } else if (x.right == null || k <= x.middle.key) {
            return searchNode(x.middle, k);
        } else {
            return searchNode(x.right, k);
        }
    }


    private void insertLeaf(QNode z) {
        if (root == null) {
            root = z;
            return;
        }

        QNode y = root;
        while (!y.isLeaf()) {
            if (z.key <= y.left.key) {
                y = y.left;
            } else if (y.right == null || z.key <= y.middle.key) {
                y = y.middle;
            } else {
                y = y.right;
            }
        }


        if (y == null) {
            throw new IllegalStateException("QueueTree invariant violated: reached null while descending");
        }


        if (y.parent == null) {
            QNode w = new QNode();
            if (z.key < y.key) setChildren(w, z, y, null);
            else setChildren(w, y, z, null);
            root = w;
            return;
        }

        QNode x = y.parent;
        QNode zPrime = insertAndSplit(x, z);

        while (x != root) {
            x = x.parent;
            if (zPrime != null) zPrime = insertAndSplit(x, zPrime);
            else {
                updateKey(x);
                updateAggregates(x);
            }
        }

        if (zPrime != null) {
            QNode w = new QNode();
            setChildren(w, root, zPrime, null);
            root = w;
        }

    }


    private void updateKey(QNode x) {
        if (x == null || x.isLeaf()) return;


        if (x.middle == null) {
            x.key = x.left.key;
            return;
        }


        x.key = (x.right != null) ? x.right.key : x.middle.key;
    }

    /**
     * Recomputes aggregate values for a node:
     * - docCount: number of doctors in the subtree
     * - patientSum: total number of patients in the subtree
     */
    private void updateAggregates(QNode x) {

        if (x == null) return;

        if (x.isLeaf()) {
            x.patientSum = x.docCount * x.key;
            return;
        }

        if (x.middle == null) {
            x.docCount = x.left.docCount;
            x.patientSum = x.left.patientSum;
            return;
        }

        x.docCount = x.left.docCount + x.middle.docCount + (x.right != null ? x.right.docCount : 0);
        x.patientSum = x.left.patientSum + x.middle.patientSum + (x.right != null ? x.right.patientSum : 0);
    }

    private void setChildren(QNode x, QNode l, QNode m, QNode r) {
        x.left = l;
        x.middle = m;
        x.right = r;
        l.parent = x;
        if (m != null) m.parent = x;
        if (r != null) r.parent = x;

        updateKey(x);
        updateAggregates(x);
    }

    private QNode insertAndSplit(QNode x, QNode z) {
        QNode l = x.left, m = x.middle, r = x.right;

        if (r == null) {
            if (z.key < l.key) setChildren(x, z, l, m);
            else if (z.key < m.key) setChildren(x, l, z, m);
            else setChildren(x, l, m, z);
            return null;
        }

        QNode y = new QNode();

        if (z.key < l.key) {
            setChildren(x, z, l, null);
            setChildren(y, m, r, null);
        } else if (z.key < m.key) {
            setChildren(x, l, z, null);
            setChildren(y, m, r, null);
        } else if (z.key < r.key) {
            setChildren(x, l, m, null);
            setChildren(y, z, r, null);
        } else {
            setChildren(x, l, m, null);
            setChildren(y, r, z, null);
        }

        return y;
    }

    private QNode borrowOrMerge(QNode y) {
        QNode z = y.parent;
        if (y == z.left) {
            QNode x = z.middle;
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
            QNode x = z.left;
            if (x.right != null) {
                setChildren(y, x.right, y.left, null);
                setChildren(x, x.left, x.middle, null);
            } else {
                setChildren(x, x.left, x.middle, y.left);
                setChildren(z, x, z.right, null);
            }
            return z;
        }
        QNode x = z.middle;
        if (x.right != null) {
            setChildren(y, x.right, y.left, null);
            setChildren(x, x.left, x.middle, null);
        } else {
            setChildren(x, x.left, x.middle, y.left);
            setChildren(z, z.left, x, null);
        }
        return z;
    }

    public void delete(QNode x) {
        QNode y = x.parent;
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
                updateAggregates(y);
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

    public int sumOfDoctors(int key) {
        if (root == null) return 0;
        return sumOfDoctorsRec(root, key);
    }

    private int sumOfDoctorsRec(QNode x, int key) {
        if (x == null) return 0;
        if (x.isLeaf()) return x.key <= key ? x.docCount : 0;

        if (x.middle == null) return sumOfDoctorsRec(x.left, key);

        if (key <= x.left.key) return sumOfDoctorsRec(x.left, key);

        if (x.right == null || key <= x.middle.key)
            return x.left.docCount + sumOfDoctorsRec(x.middle, key);

        return x.left.docCount + x.middle.docCount + sumOfDoctorsRec(x.right, key);
    }

    public int sumOfPatients(int key) {
        if (root == null) return 0;
        return sumOfPatientsRec(root, key);
    }

    private int sumOfPatientsRec(QNode x, int key) {
        if (x == null) return 0;
        if (x.isLeaf()) return x.key <= key ? x.docCount * x.key : 0;

        if (x.middle == null) return sumOfPatientsRec(x.left, key);

        if (key <= x.left.key) return sumOfPatientsRec(x.left, key);

        if (x.right == null || key <= x.middle.key)
            return x.left.patientSum + sumOfPatientsRec(x.middle, key);

        return x.left.patientSum + x.middle.patientSum + sumOfPatientsRec(x.right, key);

    }

    /**
     * Restores keys and aggregate values on the path from a modified node to the root.
     */
     public void fixUp(QNode x) {
        while (x != null) {
            updateKey(x);
            updateAggregates(x);
            x = x.parent;
        }
    }

    /**
     * Restores keys and aggregate values on the path from a modified node to the root.
     */
    public QNode maxLeaf() {
        if (root == null) return null;
        QNode x = this.root;
        while (!x.isLeaf()) {
            x = (x.right != null) ? x.right : x.middle;
        }
        return x;
    }
}
