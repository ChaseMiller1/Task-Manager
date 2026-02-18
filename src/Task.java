import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.time.LocalDate;

/**
 * Object representing a task that is stored in the list
 */
public class Task {
    private String task;
    private LocalDate date;
    private final BooleanProperty completed;

    /**
     * Task constructor
     * @param task description
     * @param date task is due
     */
    public Task(String task, LocalDate date) {
        this.task = task;
        this.date = date;
        completed = new SimpleBooleanProperty(false);
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getTask() { return task; }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

    public BooleanProperty completedProperty() {
        return completed;
    }

    /*
    public boolean isCompleted() {
        return completed.get();
    }

    public void setCompleted(boolean value) {
        completed.set(value);
    }
    */

    public String toString() {
        return task + (date == null? "" : " - " + date);
    }
}