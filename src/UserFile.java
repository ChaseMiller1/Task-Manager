import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Scanner;

public class UserFile {

    private static final String FILE = "savedList.txt";

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