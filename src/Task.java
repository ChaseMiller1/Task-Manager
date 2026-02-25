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

    /**
     * Get task description
     * @return task description
     */
    public String getTask() {
        return task;
    }

    /**
     * Set task description
     * @param task new description
     */
    public void setTask(String task) {
        this.task = task;
    }

    /**
     * Get task due date
     * @return date due
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Set task due date
     * @param date new date
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /**
     * Get task completion value
     * Controls checkboxes when switching boolean values
     * @return completed value
     */
    public BooleanProperty completedProperty() {
        return completed;
    }

    /**
     * Serialize task for saving to a text file
     * @return serialized task
     */
    public String serialize() {
        return task + " - " + date + " - " + completed.get();
    }

    @Override
    public String toString() {
        return task + " - " +  date;
    }
}