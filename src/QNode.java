public class QNode {
    public int key;
    public int docCount;
    public int patientSum;
    Doctor headDoctor, tailDoctor;

    QNode left, middle, right, parent;

    boolean isLeaf() {
        return left == null && middle == null && right == null;
    }
}