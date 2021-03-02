package client;

import commands.Command;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class RegController {

    private Controller controller;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private TextArea textArea;
    @FXML
    private Button signUp_btn;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField nicknameField;

    public void setController(Controller controller) {
        this.controller = controller;
    }

    @FXML
    public void buttonAnimatedSignUp(MouseEvent mouseEvent) {
        signUp_btn.setStyle("-fx-background-color: #42f5f5;" + "-fx-text-fill: #000000;");
    }

    @FXML
    void buttonFree(MouseEvent event) {
        signUp_btn.setStyle("-fx-background-color: #57998d;");
    }


    public void setResultTryToReg(String command) {
        if (command.equals(Command.REG_OK)) {
            textArea.appendText("Регистрация успешна!\n");
        }
        if (command.equals(Command.REG_NO)) {
            textArea.appendText("Логин или никнейм уже заняты!\n");
        }
    }

    public void tryToReg(ActionEvent actionEvent) {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();
        String nickname = nicknameField.getText().trim();

        if (login.length() * password.length() * nickname.length() == 0) {
            return;
        }

        controller.registration(login, password, nickname);
    }
}
