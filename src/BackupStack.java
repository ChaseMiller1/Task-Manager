import java.util.EmptyStackException;

/**
 * Stores old lists
 * Stack using a linked-list, allowing for O(1) popping and pushing
 */
public class BackupStack {
    private static class Node {
        UndoAction action;
        Node next;

        Node(UndoAction action) {
            this.action = action;
        }
    }

    private Node top;
    private int size;

    /**
     * non-param constructor
     */
    public BackupStack() {
        this.top = null;
        this.size = 0;
    }

    /**
     * Pushes a snapshot of the current task list onto the stack
     * @param action to push
     */
    public void push(UndoAction action) {
        Node node = new Node(action);
        node.next = top;
        top = node;
        size++;
    }

    /**
     * Pops the most recent tasks off the stack
     * @throws EmptyStackException when stack is empty
     */
    public UndoAction pop() throws EmptyStackException {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        UndoAction action = top.action;
        top = top.next;
        size--;
        return action;
    }

    /**
     * Clears list
     */
    public void clear() {
        top = null;
    }

    /**
     * Checks if the stack is empty.
     */
    public boolean isEmpty() {
        return top == null;
    }

    /**
     * Returns the number of snapshots stored.
     */
    public int size() {
        return size;
    }
}
