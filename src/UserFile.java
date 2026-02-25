import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * Static methods for keeping track of the last file the user
 * opened, so it can be opened up when the app re-launches
 */
public class UserFile {

    private static final String FILE = "savedList.txt";

    /**
     * Saves the file being used when app is closed
     * @param file to save
     */
    public static void saveLastFile(File file) {
        try (PrintWriter writer = new PrintWriter(FILE)) {
            if (file != null) {
                writer.print(file.toPath());
            } else {
                writer.print("");
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(
                    "File not found, current list couldn't be saved");
        }
    }

    /**
     * Loads saved file when
     * @return file to open
     * @throws IOException if issue with backup file
     */
    public static File loadLastFile() throws IOException {
        File savedFile = new File(FILE);
        if (savedFile.exists()) {
            try (Scanner scanner = new Scanner(savedFile)) {
                if (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (!line.isBlank()) {
                        return new File(line);
                    }
                }
            }
        }
        return null;
    }
}