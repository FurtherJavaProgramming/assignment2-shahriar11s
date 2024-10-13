import java.io.IOException;
import java.sql.SQLException;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.fxml.FXMLLoader;
import model.Model;
import util.WindowManager;
import controller.LoginController;

public class Main extends Application {
    private Model model;

    @Override
    public void init() {
        model = new Model(); // Initialize the model
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            model.setup(); // Setup model (connect to DB)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));

            // Customize controller instance
            LoginController loginController = new LoginController(primaryStage, model);
            loader.setController(loginController);

            Pane root = loader.load();  // Can be either GridPane or VBox
            
            WindowManager.addWindow(primaryStage);

            // Set up the close request handler for the primary stage
            primaryStage.setOnCloseRequest(event -> {
                WindowManager.closeAllWindows();
            });

            loginController.showStage(root); // Show the login stage
        } catch (IOException | SQLException | RuntimeException e) {
            Scene scene = new Scene(new Label(e.getMessage()), 200, 100);
            primaryStage.setTitle("Error");
            primaryStage.setScene(scene);
            primaryStage.show();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}