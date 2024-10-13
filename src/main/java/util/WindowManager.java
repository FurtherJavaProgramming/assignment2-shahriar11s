package util;

import javafx.stage.Stage;
import java.util.HashSet;
import java.util.Set;

import dao.BookDao;

public class WindowManager {
    private static final Set<Stage> openWindows = new HashSet<>();

    public static void addWindow(Stage stage) {
        openWindows.add(stage);
    }

    public static void removeWindow(Stage stage) {
        openWindows.remove(stage);
    }

    public static void closeAllWindows() {
        BookDao.clearTempStock();  // Clear temp_stock when closing
        BookDao.setIsRunningFlag(0);  // Reset the is_running flag to 0

        for (Stage stage : openWindows) {
            stage.close();
        }
        openWindows.clear();
    }


}
