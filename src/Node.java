public class Node<T extends HasKey> {
    public String key;

    public Node<T> parent;
    public Node<T> left, middle, right;

    public T value;

    public boolean isLeaf() {
        return left == null && middle == null && right == null;
    }
}