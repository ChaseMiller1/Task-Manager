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
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.ResourceBundle;

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
    private UndoManager undoManager;
    private File currentFile;
    private final Comparator<Task> taskComparator =
            Comparator.comparing(Task::getDate)
                    .thenComparing(Task::getTask);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // instantiate tasks and backup stack
        tasks = FXCollections.observableArrayList();
        undoManager = new UndoManager();

        // Load saved backup list
        try {
            currentFile = UserFile.loadLastFile();
            if (currentFile != null) {
                openFile(currentFile);
            }
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR,
                    "File not found, list couldn't be retrieved").showAndWait();
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
            save();
        });

        // Save on close
        Platform.runLater(() -> {
            Stage stage = (Stage) tableView.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                try {
                    UserFile.saveLastFile(currentFile);
                } catch (RuntimeException e) {
                    new Alert(Alert.AlertType.ERROR, "Error saving file").showAndWait();
                }
            });
        });
    }

    private void attachCompletionListener(Task task) {
        task.completedProperty().addListener(
                (obs, oldValue, newValue) -> {
                    if (!undoManager.isPerforming()) {
                        undoManager.push(new Action(
                                () -> task.completedProperty().set(oldValue),
                                () -> task.completedProperty().set(newValue)
                        ));
                    }
                    save();
        });
    }

    @FXML
    private void undo() {
        undoManager.undo();
        refresh();
    }

    @FXML
    private void redo() {
        undoManager.redo();
        refresh();
    }

    @FXML
    private void clearChecked() {
        ObservableList<Task> removedTasks = FXCollections.observableArrayList(
                tasks.filtered(task -> task.completedProperty().get())
        );

        if (!removedTasks.isEmpty()) {
            undoManager.push(new Action(
                    () -> tasks.addAll(removedTasks),
                    () -> tasks.removeAll(removedTasks)
            ));
            tasks.removeAll(removedTasks);
            save();
        }
    }

    @FXML
    private void clearList() {
        if (!tasks.isEmpty()) {
            // Backup the entire list
            ObservableList<Task> removedTasks = FXCollections.observableArrayList(tasks);
            undoManager.push(new Action(
                    () -> tasks.addAll(removedTasks),
                    () -> tasks.removeAll(removedTasks)
            ));

            // Clear list
            tasks.clear();
            save();
        }
    }

    @FXML
    private void add() {
        if (date.getValue() != null) {
            Task newTask = new Task(taskDescription.getText(), date.getValue());
            undoManager.push(new Action(
                    () -> tasks.remove(newTask),
                    () -> tasks.add(newTask)
            ));
            tasks.add(newTask);
            refresh();
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
        undoManager.clear();
    }

    @FXML
    private void open() {
        File file = createTextFileChooser("Open Task File")
                .showOpenDialog(tableView.getScene().getWindow());
        if (file != null) {
            openFile(file);
        }
    }

    @FXML
    private void saveAs() {
        File file = createTextFileChooser("Save Task File")
                .showSaveDialog(tableView.getScene().getWindow());
        if (file != null) {
            currentFile = file;
            currentFileDisplay.setText(file.getName());
            save();
        }
    }

    @FXML
    private void commands() {

    }

    /**
     * Get file chooser for text files only
     * @param title of file action
     * @return chooser for text files
     */
    private FileChooser createTextFileChooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        return chooser;
    }

    /**
     * Opens a txt file to table view
     * @param file to open
     */
    private void openFile(File file) {
        tasks.clear();
        try {
            tasks.addAll(FileUtilities.load(file));
            currentFile = file;
            currentFileDisplay.setText(file.getName());
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    /**
     * Refresh table view
     */
    private void save() {
        if (currentFile != null) {
            save(currentFile);
        }
    }

    /**
     * Saves list
     * @param file to save
     */
    private void save(File file) {
        try {
            FileUtilities.save(file, tasks);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to save tasks").showAndWait();
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
            controller.setUndoManager(undoManager);

            Stage stage = new Stage();
            stage.setTitle(task.toString());
            stage.setScene(new Scene(root, 300, 200));
            controller.setStage(stage);

            stage.setOnHidden(e -> refresh());

            stage.show();

        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Error opening task").showAndWait();
        }
    }

    private void refresh() {
        FXCollections.sort(tasks, taskComparator);
        tableView.refresh();
        save();
    }
}