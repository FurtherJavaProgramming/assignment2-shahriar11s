package controller;

import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Model;
import model.User;
import util.WindowManager;

public class EditProfileController {
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private PasswordField currentPasswordField; // Added current password field
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmNewPasswordField;
    @FXML
    private Button saveBtn;
    @FXML
    private Button cancelBtn;
    @FXML
    private Button changePasswordBtn; // Button to trigger password change fields

    private Stage stage;
    private Model model;
    private User currentUser;

    private boolean passwordChangeRequested = false;

    public EditProfileController(Stage stage, Model model) {
        this.stage = stage;
        this.model = model;
        this.currentUser = model.getCurrentUser();  // Fetch the current user
    }

    @FXML
    public void initialize() {
        // Load the current user details
        firstNameField.setText(currentUser.getFirstName());
        lastNameField.setText(currentUser.getLastName());

        // Initially hide the new password and confirm password fields
        togglePasswordFields(false);

        // Save button action
        saveBtn.setOnAction(event -> {
            String newFirstName = firstNameField.getText();
            String newLastName = lastNameField.getText();
            String currentPassword = currentPasswordField.getText();

            // Validation for current password (required)
            if (currentPassword.isEmpty()) {
                showAlert("Error", "Please enter your current password.");
                return;
            }

            // Validate the current password
            if (!currentUser.getPassword().equals(currentPassword)) {
                showAlert("Error", "Incorrect current password.");
                return;
            }

            // Check if the user wants to change the password
            if (passwordChangeRequested) {
                String newPassword = newPasswordField.getText();
                String confirmNewPassword = confirmNewPasswordField.getText();

                // Validate new password and confirm password
                if (!newPassword.equals(confirmNewPassword)) {
                    showAlert("Error", "New password and confirm password do not match.");
                    return;
                }

                // Ensure new password is not empty
                if (newPassword.isEmpty()) {
                    showAlert("Error", "Please enter a new password.");
                    return;
                }

                // Update the password
                currentUser.setPassword(newPassword);
            }

            // Update the user's name
            currentUser.setFirstName(newFirstName);
            currentUser.setLastName(newLastName);

            // Persist the updated user
            try {
                model.getUserDao().updateUser(currentUser);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            showAlert("Success", "Profile updated successfully!");
            stage.close();
        });

        // Cancel button action
        cancelBtn.setOnAction(event -> stage.close());

        // Change Password button action
        changePasswordBtn.setOnAction(event -> {
            passwordChangeRequested = true;
            togglePasswordFields(true); // Reveal new password fields
        });
    }

    // Helper method to show/hide password fields
    private void togglePasswordFields(boolean visible) {
        newPasswordField.setVisible(visible);
        confirmNewPasswordField.setVisible(visible);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Edit Profile");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(stage.getOwner()); // This will make it modal to its parent
        WindowManager.addWindow(stage);
        stage.setOnCloseRequest(event -> WindowManager.removeWindow(stage));
        stage.showAndWait();
    }
}
