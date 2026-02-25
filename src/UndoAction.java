/**
 * Undo / redo interface
 */
public interface UndoAction {
    /**
     * Undoes an action
     */
    void undo();

    /**
     * Redoes an undone action
     */
    void redo();
}
