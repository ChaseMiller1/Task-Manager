public class Action implements UndoAction {
    private final Runnable undo;
    private final Runnable redo;

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