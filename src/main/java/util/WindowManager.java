package util;

import javafx.stage.Stage;
import java.util.HashSet;
import java.util.Set;

public class WindowManager {
    private static final Set<Stage> openWindows = new HashSet<>();

    public static void addWindow(Stage stage) {
        openWindows.add(stage);
    }

    public static void removeWindow(Stage stage) {
        openWindows.remove(stage);
    }

    public static void closeAllWindows() {
        for (Stage stage : openWindows) {
            stage.close();
        }
        openWindows.clear();
    }
}