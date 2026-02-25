/**
 * Manages undo/redo of tasks
 */
public class UndoManager {

    private final BackupStack undoStack = new BackupStack();
    private final BackupStack redoStack = new BackupStack();
    private boolean performing = false;

    /**
     * Push an action to the undo stack
     * @param action to push
     */
    public void push(UndoAction action) {
        undoStack.push(action);
        redoStack.clear();
    }

    /**
     * Undoes an action by popping the opposite action off the undo stack
     */
    public void undo() {
        if (!undoStack.isEmpty()) {
            performing = true;

            UndoAction action = undoStack.pop();
            action.undo();

            redoStack.push(action);
            performing = false;
        }
    }

    /**
     * redoes an action by popping the original action off the redo stack
     */
    public void redo() {
        if (!redoStack.isEmpty()) {
            performing = true;

            UndoAction action = redoStack.pop();
            action.redo();

            undoStack.push(action);
            performing = false;
        }
    }

    /**
     * Boolean to control if runnable from either stack is running
     * Turning this on and off while running prevents errors
     * @return performing
     */
    public boolean isPerforming() {
        return performing;
    }

    /**
     * Clear all past action data
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}