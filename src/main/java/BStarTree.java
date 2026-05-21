import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BStarTree {
    private static final int MAX_KEYS = 5;

    private Node root;
    private long operations;
    private Set<Integer> deletedKeys;

    public BStarTree() {
        root = new Node(true);
        deletedKeys = new HashSet<>();
    }

    public OperationResult add(int value) {
        long start = System.nanoTime();
        operations = 0;

        SearchResult existing = searchPhysical(root, value);

        boolean success;

        if (existing != null) {
            operations++;
            if (deletedKeys.contains(value)) {
                deletedKeys.remove(value);
                success = true;
            } else {
                success = false;
            }
        } else {
            if (root.keys.size() == MAX_KEYS) {
                splitRoot();
            }

            insertNonFull(root, value);
            success = true;
        }

        long end = System.nanoTime();
        return new OperationResult(success, end - start, operations);
    }

    public OperationResult contains(int value) {
        long start = System.nanoTime();
        operations = 0;

        SearchResult result = searchPhysical(root, value);

        boolean success = result != null;

        operations++;
        if (success && deletedKeys.contains(value)) {
            success = false;
        }

        long end = System.nanoTime();
        return new OperationResult(success, end - start, operations);
    }

    public OperationResult remove(int value) {
        long start = System.nanoTime();
        operations = 0;

        SearchResult result = searchPhysical(root, value);

        boolean success = false;

        if (result != null) {
            operations++;
            if (!deletedKeys.contains(value)) {
                deletedKeys.add(value);
                success = true;
            }
        }

        long end = System.nanoTime();
        return new OperationResult(success, end - start, operations);
    }

    private void insertNonFull(Node node, int value) {
        if (node.leaf) {
            int position = findPosition(node, value);
            operations += node.keys.size() - position + 1;
            node.keys.add(position, value);
        } else {
            int childIndex = findChildIndex(node, value);
            Node child = node.children.get(childIndex);
            operations++;

            if (child.keys.size() == MAX_KEYS) {
                fixFullChild(node, childIndex);
                childIndex = findChildIndex(node, value);
            }

            insertNonFull(node.children.get(childIndex), value);
        }
    }

    private void fixFullChild(Node parent, int childIndex) {
        Node child = parent.children.get(childIndex);

        if (childIndex > 0) {
            Node left = parent.children.get(childIndex - 1);
            operations++;

            if (left.keys.size() < MAX_KEYS) {
                balanceTwoChildren(parent, childIndex - 1);
                return;
            }
        }

        if (childIndex + 1 < parent.children.size()) {
            Node right = parent.children.get(childIndex + 1);
            operations++;

            if (right.keys.size() < MAX_KEYS) {
                balanceTwoChildren(parent, childIndex);
                return;
            }
        }

        if (childIndex + 1 < parent.children.size()) {
            splitTwoChildrenIntoThree(parent, childIndex);
        } else {
            splitTwoChildrenIntoThree(parent, childIndex - 1);
        }
    }

    private void balanceTwoChildren(Node parent, int leftIndex) {
        Node left = parent.children.get(leftIndex);
        Node right = parent.children.get(leftIndex + 1);

        List<Integer> keys = new ArrayList<>();
        keys.addAll(left.keys);
        keys.add(parent.keys.get(leftIndex));
        keys.addAll(right.keys);

        operations += keys.size();

        int middle = keys.size() / 2;

        left.keys = new ArrayList<>(keys.subList(0, middle));
        parent.keys.set(leftIndex, keys.get(middle));
        right.keys = new ArrayList<>(keys.subList(middle + 1, keys.size()));

        if (!left.leaf) {
            List<Node> children = new ArrayList<>();
            children.addAll(left.children);
            children.addAll(right.children);

            int leftChildrenCount = left.keys.size() + 1;

            left.children = new ArrayList<>(children.subList(0, leftChildrenCount));
            right.children = new ArrayList<>(children.subList(leftChildrenCount, children.size()));
        }
    }

    private void splitTwoChildrenIntoThree(Node parent, int leftIndex) {
        Node left = parent.children.get(leftIndex);
        Node right = parent.children.get(leftIndex + 1);

        List<Integer> keys = new ArrayList<>();
        keys.addAll(left.keys);
        keys.add(parent.keys.get(leftIndex));
        keys.addAll(right.keys);

        operations += keys.size();

        int firstMiddle = keys.size() / 3;
        int secondMiddle = (keys.size() * 2) / 3;

        Node newLeft = new Node(left.leaf);
        Node newMiddle = new Node(left.leaf);
        Node newRight = new Node(left.leaf);

        newLeft.keys = new ArrayList<>(keys.subList(0, firstMiddle));
        newMiddle.keys = new ArrayList<>(keys.subList(firstMiddle + 1, secondMiddle));
        newRight.keys = new ArrayList<>(keys.subList(secondMiddle + 1, keys.size()));

        int firstParentKey = keys.get(firstMiddle);
        int secondParentKey = keys.get(secondMiddle);

        if (!left.leaf) {
            List<Node> children = new ArrayList<>();
            children.addAll(left.children);
            children.addAll(right.children);

            int leftChildrenCount = newLeft.keys.size() + 1;
            int middleChildrenCount = newMiddle.keys.size() + 1;

            newLeft.children = new ArrayList<>(children.subList(0, leftChildrenCount));
            newMiddle.children = new ArrayList<>(children.subList(leftChildrenCount, leftChildrenCount + middleChildrenCount));
            newRight.children = new ArrayList<>(children.subList(leftChildrenCount + middleChildrenCount, children.size()));
        }

        parent.keys.set(leftIndex, firstParentKey);
        parent.keys.add(leftIndex + 1, secondParentKey);

        parent.children.set(leftIndex, newLeft);
        parent.children.set(leftIndex + 1, newMiddle);
        parent.children.add(leftIndex + 2, newRight);
    }

    private void splitRoot() {
        Node oldRoot = root;

        Node left = new Node(oldRoot.leaf);
        Node right = new Node(oldRoot.leaf);

        int middle = oldRoot.keys.size() / 2;

        left.keys = new ArrayList<>(oldRoot.keys.subList(0, middle));
        right.keys = new ArrayList<>(oldRoot.keys.subList(middle + 1, oldRoot.keys.size()));

        if (!oldRoot.leaf) {
            left.children = new ArrayList<>(oldRoot.children.subList(0, middle + 1));
            right.children = new ArrayList<>(oldRoot.children.subList(middle + 1, oldRoot.children.size()));
        }

        Node newRoot = new Node(false);
        newRoot.keys.add(oldRoot.keys.get(middle));
        newRoot.children.add(left);
        newRoot.children.add(right);

        root = newRoot;
        operations += oldRoot.keys.size();
    }

    private int findPosition(Node node, int value) {
        int index = 0;

        while (index < node.keys.size()) {
            operations++;
            if (value < node.keys.get(index)) {
                break;
            }

            index++;
        }

        return index;
    }

    private int findChildIndex(Node node, int value) {
        int index = 0;

        while (index < node.keys.size()) {
            operations++;
            if (value < node.keys.get(index)) {
                break;
            }

            index++;
        }

        return index;
    }

    private SearchResult searchPhysical(Node node, int value) {
        int index = 0;

        while (index < node.keys.size()) {
            operations++;
            int current = node.keys.get(index);

            if (value == current) {
                return new SearchResult(node, index);
            }

            operations++;
            if (value < current) {
                break;
            }

            index++;
        }


        if (node.leaf) {
            return null;
        }

        operations++;
        return searchPhysical(node.children.get(index), value);
    }

    private static class Node {
        private List<Integer> keys;
        private List<Node> children;
        private boolean leaf;

        private Node(boolean leaf) {
            this.leaf = leaf;
            keys = new ArrayList<>();
            children = new ArrayList<>();
        }
    }

    private static class SearchResult {
        private Node node;
        private int index;

        private SearchResult(Node node, int index) {
            this.node = node;
            this.index = index;
        }
    }
}