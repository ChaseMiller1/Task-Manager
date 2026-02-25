import java.util.EmptyStackException;

/**
 * Stores old actions for undoing / redoing actions
 * Stack using a linked-list, allowing for O(1) popping and pushing
 * No size tracked, because we only care if the stack is empty or not
 */
public class BackupStack {
    private static class Node {
        UndoAction action;
        Node next;

        /**
         * 1-param constructor
         * @param action to store on node
         */
        Node(UndoAction action) {
            this.action = action;
        }
    }

    private Node top;

    /**
     * non-param constructor
     */
    public BackupStack() {
        this.top = null;
    }

    /**
     * Pushes a snapshot of the current task list onto the stack
     * @param action to push
     */
    public void push(UndoAction action) {
        Node node = new Node(action);
        node.next = top;
        top = node;
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
}
