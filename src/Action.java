/**
 * Controls runnable's for undo / redo
 */
public class Action implements UndoAction {
    private final Runnable undo;
    private final Runnable redo;

    /**
     * 2-param constructor
     * @param undo runnable
     * @param redo runnable
     */
    public Action(Runnable undo, Runnable redo) {
        this.undo = undo;
        this.redo = redo;
    }

    @Override
    public void undo() {
        undo.run();
    }

    @Override
    public void redo() {
        redo.run();
    }
}