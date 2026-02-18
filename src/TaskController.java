import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.time.LocalDate;

public class TaskController {
    @FXML
    private TextArea taskDescription;
    @FXML
    private DatePicker date;

    private Task task;
    private Stage stage;

    private ObservableList<Task> tasks;
    private BackupStack backups;
    private BackupStack redo;

    @FXML
    private void delete() {
        backups.push(new Action(
                () -> tasks.add(task),
                () -> tasks.remove(task)
        ));
        redo.clear();
        tasks.remove(task);
        stage.close();
    }

    @FXML
    private void save() {
        String oldText = task.getTask();
        String newText = taskDescription.getText();
        LocalDate oldDate = task.getDate();
        LocalDate newDate = date.getValue();
        backups.push(new Action(
                () -> {
                    task.setTask(oldText);
                    task.setDate(oldDate);
                },
                () -> {
                    task.setTask(newText);
                    task.setDate(newDate);
                }
        ));
        redo.clear();
        task.setTask(newText);
        task.setDate(newDate);
        stage.close();
    }

    @FXML
    private void dateToday() {
        date.setValue(LocalDate.now());
    }

    /**
     * Set the task to edit
     * @param task to edit
     */
    public void setTask(Task task) {
        this.task = task;
        taskDescription.setText(task.getTask());
        date.setValue(task.getDate());
    }

    /**
     * Set the stage to open up the task information
     * @param stage to set
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Set controller task list to edit
     * @param tasks to edit
     */
    public void setTasks(ObservableList<Task> tasks) {
        this.tasks = tasks;
    }

    /**
     * Set backup stack to push
     * @param backups to push
     */
    public void setBackups(BackupStack backups) {
        this.backups = backups;
    }

    /**
     * Set redo stack to push
     * @param redo to push
     */
    public void setRedo(BackupStack redo) {
        this.redo = redo;
    }
}
