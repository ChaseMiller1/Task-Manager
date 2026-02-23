import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.Scanner;

/**
 * Controls the FXML of the task manager
 * Handles all button functions, sorting, and saving
 */
public class Controller implements Initializable {
    @FXML
    private TableView<Task> tableView;
    @FXML
    private TableColumn<Task, Boolean> doneCol;
    @FXML
    private TableColumn<Task, String> taskCol;
    @FXML
    private TextField currentFileDisplay;
    @FXML
    private TextArea taskDescription;
    @FXML
    private DatePicker date;

    private ObservableList<Task> tasks;
    private BackupStack backups;
    private BackupStack redo;
    private File currentFile;
    private final Comparator<Task> taskComparator =
            Comparator.comparing(Task::getDate)
                    .thenComparing(Task::getTask);
    private boolean isUndoRedo = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // instantiate tasks and backup stack
        tasks = FXCollections.observableArrayList();
        backups = new BackupStack();
        redo = new BackupStack();

        // Load saved backup list
        try (Scanner read = new Scanner(new File("savedList.txt"))) {
            if (read.hasNextLine()) {
                currentFile = new File(read.nextLine());
                openFile(currentFile);
            } else {
                currentFile = null;
            }
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "File not found, list couldn't be retrieved").showAndWait();
        }

        // Set list to tableview
        tableView.setItems(tasks);

        // Checkbox column
        doneCol.setCellValueFactory(cell -> cell.getValue().completedProperty());
        doneCol.setCellFactory(CheckBoxTableCell.forTableColumn(doneCol));

        // Task text column
        taskCol.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().toString())
        );

        // Gray out completed tasks
        taskCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String text, boolean empty) {
                super.updateItem(text, empty);
                if (empty || text == null) {
                    setText(null);
                } else {
                    Task task = getTableView().getItems().get(getIndex());
                    textFillProperty().bind(
                            Bindings.when(task.completedProperty())
                                    .then(Color.GRAY)
                                    .otherwise(Color.BLACK)
                    );
                    setText(text);
                }
            }
        });

        // Listen for a double click on a task to edit it
        tableView.setRowFactory(tv -> {
            TableRow<Task> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    openTask(row.getItem());
                }
            });
            return row;
        });

        // Attach checkbox to tasks
        for (Task task : tasks) {
            attachCompletionListener(task);
        }
        tasks.addListener((javafx.collections.ListChangeListener<Task>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Task task : change.getAddedSubList()) {
                        attachCompletionListener(task);
                    }

                }
            }
            if (currentFile != null) {
                save(currentFile);
            }
        });

        // Save on close
        Platform.runLater(() -> {
            Stage stage = (Stage) tableView.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                try (PrintWriter writer = new PrintWriter("savedList.txt")) {
                    if (currentFile != null) {
                        writer.print(currentFile.toPath());
                    } else {
                        writer.print("");
                    }
                } catch (FileNotFoundException e) {
                    new Alert(Alert.AlertType.ERROR,
                            "File not found, current list couldn't be saved").showAndWait();
                }
            });
        });
    }

    private void attachCompletionListener(Task task) {
        task.completedProperty().addListener(
                (obs, oldValue, newValue) -> {
            if (!isUndoRedo) {
                backups.push(new Action(
                        () -> task.completedProperty().set(oldValue),
                        () -> task.completedProperty().set(newValue)
                ));
                redo.clear();
            }
        });
    }

    @FXML
    private void undo() {
        if (!backups.isEmpty()) {
            UndoAction action = backups.pop();
            isUndoRedo = true;
            action.undo();
            isUndoRedo = false;
            redo.push(action);

            FXCollections.sort(tasks, taskComparator);
            tableView.refresh();
            save(currentFile);
        }
    }

    @FXML
    private void redo() {
         if (!redo.isEmpty()) {
             UndoAction action = redo.pop();
             isUndoRedo = true;
             action.redo();
             isUndoRedo = false;
             backups.push(action);

             FXCollections.sort(tasks, taskComparator);
             tableView.refresh();
             save(currentFile);
         }
    }

    @FXML
    private void clearChecked() {
        // Keep track of checked tasks that are removed
        ObservableList<Task> removedTasks = FXCollections.observableArrayList();
        tasks.removeIf(task -> {
            if (task.completedProperty().getValue()) {
                removedTasks.add(task);
                return true;
            }
            return false;
        });

        // Push undo action to restore removed tasks
        backups.push(new Action(
                () -> tasks.addAll(removedTasks),
                () -> tasks.clear()
        ));
        redo.clear();
        tableView.refresh();
        save(currentFile);
    }

    @FXML
    private void clearList() {
        if (!tasks.isEmpty()) {
            // Backup the entire list
            ObservableList<Task> removedTasks = FXCollections.observableArrayList(tasks);
            backups.push(new Action(
                    () -> tasks.addAll(removedTasks),
                    () -> tasks.removeAll(removedTasks)
            ));
            redo.clear();

            // Clear list
            tasks.clear();
            save(currentFile);
        }
    }

    @FXML
    private void add() {
        if (date.getValue() != null) {
            Task newTask = new Task(taskDescription.getText(), date.getValue());
            backups.push(new Action(
                    () -> tasks.remove(newTask),
                    () -> tasks.add(newTask)
            ));
            redo.clear();
            tasks.add(newTask);
            FXCollections.sort(tasks, taskComparator);
            save(currentFile);
        } else {
            new Alert(Alert.AlertType.ERROR, "Must have a date").showAndWait();
        }
    }

    @FXML
    private void clear() {
        date.setValue(null);
        taskDescription.clear();
    }

    @FXML
    private void dateToday() {
        date.setValue(LocalDate.now());
    }

    @FXML
    private void close() {
        currentFile = null;
        currentFileDisplay.clear();
        tasks.clear();
        backups.clear();
        redo.clear();
    }

    @FXML
    private void open() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Task File");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = chooser.showOpenDialog(tableView.getScene().getWindow());
        if (file != null) {
            openFile(file);
        }
    }

    @FXML
    private void saveAs() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Task File");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        File file = chooser.showSaveDialog(tableView.getScene().getWindow());
        currentFileDisplay.setText(file.getName());
    }

    @FXML
    private void commands() {

    }

    /**
     * Opens a txt file to table view
     * @param file to open
     */
    private void openFile(File file) {
        tasks.clear();
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
            currentFile = file;
            currentFileDisplay.setText(file.getName());
        } catch (FileNotFoundException e) {
            new Alert(Alert.AlertType.ERROR, "File not found, list couldn't be saved").showAndWait();
        }
    }

    /**
     * Opens the window for a task
     * @param task to open
     */
    private void openTask(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditTask.fxml"));
            Parent root = loader.load();
            TaskController controller = loader.getController();
            controller.setTask(task);
            controller.setTasks(tasks);
            controller.setBackups(backups);
            controller.setRedo(redo);

            Stage stage = new Stage();
            stage.setTitle(task.toString());
            stage.setScene(new Scene(root, 300, 200));
            controller.setStage(stage);

            stage.setOnHidden(e -> {
                FXCollections.sort(tasks, taskComparator);
                tableView.refresh();
                save(currentFile);
            });

            stage.show();

        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Error opening task").showAndWait();
        }
    }

    /**
     * Saves list
     */
    private void save(File file) {
        if (file != null) {
            try (PrintWriter pw = new PrintWriter(file)) {
                for (Task task : tasks) {
                    pw.println(task.toString() + " - " + task.completedProperty().get());
                }
            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR, "Failed to save tasks").showAndWait();
            }
        }
    }
}