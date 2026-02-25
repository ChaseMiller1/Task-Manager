/**
 * Manages undo/redo of tasks
 */
public class UndoManager {

    private final BackupStack undoStack = new BackupStack();
    private final BackupStack redoStack = new BackupStack();
    private boolean performing = false;

    public void push(UndoAction action) {
        undoStack.push(action);
        redoStack.clear();
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            performing = true;

            UndoAction action = undoStack.pop();
            action.undo();

            redoStack.push(action);
            performing = false;
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            performing = true;

            UndoAction action = redoStack.pop();
            action.redo();

            undoStack.push(action);
            performing = false;
        }
    }

    public boolean isPerforming() {
        return performing;
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}