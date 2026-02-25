import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import java.io.*;
import java.time.LocalDate;
import java.util.Scanner;

/**
 * Static methods for loading and saving files of tasks
 */
public class FileUtilities {

    /**
     * Loads a set of tasks by parsing its description, date, and completion
     * @param file to load
     * @return tasks loaded from the file
     * @throws IOException if issue with file to load
     */
    public static ObservableList<Task> load(File file) throws IOException {
        ObservableList<Task> tasks = FXCollections.observableArrayList();
        try (Scanner scanner = new Scanner(file)) {
            int errors = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!line.isBlank()) {
                    String[] parts = line.split(" - ", 3);
                    if (parts.length == 3) {
                        try {
                            String taskText = parts[0].trim();
                            LocalDate taskDate = LocalDate.parse(parts[1].trim());
                            boolean completed = Boolean.parseBoolean(parts[2].trim());
                            Task task = new Task(taskText, taskDate);
                            task.completedProperty().set(completed);
                            tasks.add(task);
                        } catch (Exception e) {
                            errors++;
                        }
                    } else {
                        errors++;
                    }
                }
            }
            if (errors == 1) {
                new Alert(Alert.AlertType.ERROR, "Parsing issue, " + errors + " task skipped").showAndWait();
            } else if (errors > 1) {
                new Alert(Alert.AlertType.ERROR, "Parsing issue, " + errors + " tasks skipped").showAndWait();
            }
        }
        return tasks;
    }

    /**
     * Saved a list of tasks to a file
     * @param file to save to
     * @param tasks to save to the file
     * @throws IOException if issue with file to save to
     */
    public static void save(File file, ObservableList<Task> tasks) throws IOException {
        if (file != null) {
            try (PrintWriter pw = new PrintWriter(file)) {
                for (Task task : tasks) {
                    pw.println(task.serialize());
                }
            }
        }
    }
}