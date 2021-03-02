package client;


import commands.Command;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public HBox authPanel;
    @FXML
    public HBox msgPanel;
    @FXML
    private HBox hbox_listview;
    @FXML
    private ListView<String> clientList;
    @FXML
    private TextField nameSetText;
    @FXML
    private Button signUp_btn;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Button closeBTN;
    @FXML
    private TextArea textArea;
    @FXML
    private TextField textField;
    @FXML
    private Button buttonSend;
    @FXML
    private TextField login_field;
    @FXML
    private PasswordField password_field;
    @FXML
    private Button signIn_btn;

    private final int PORT = 8787;
    private final String IP_ADDRESS = "localhost";
    private static Socket socket;
    private static DataOutputStream out;
    private static DataInputStream in;

    private boolean authenticated;
    private String nickname;
    private Stage stage;
    private Stage regStage;
    private RegController regController;





    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;

        msgPanel.setVisible(authenticated);
        msgPanel.setManaged(authenticated);
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        hbox_listview.setVisible(authenticated);
        hbox_listview.setManaged(authenticated);


        if (!authenticated) {
            nickname = "";
        }
        textArea.clear();
        setTitle(nickname);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            stage = (Stage) textArea.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                System.out.println("bye");
                if (socket != null && !socket.isClosed()) {
                    try {
                        out.writeUTF(Command.END);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        setAuthenticated(false);

    }

    private void connect() {
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //authorization loop
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals(Command.END)) {
                                throw new RuntimeException("Нас выпнул Сервер!");
                            }
                            if (str.startsWith(Command.AUTH_OK)) {
                                String[] token = str.split("\\s");
                                nickname = token[1];
                                setAuthenticated(true);
                                break;
                            }
                            if(str.equals(Command.REG_OK)){
                                regController.setResultTryToReg(Command.REG_OK);
                            }
                            if(str.equals(Command.REG_NO)){
                                regController.setResultTryToReg(Command.REG_NO);
                            }
                        } else {
                            textArea.appendText(str + "\n");
                        }
                    }
                    //work loop
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals(Command.END)) {
                                System.out.println("Client disconnected!");
                                break;
                            }
                            if (str.startsWith(Command.CLIENT_LIST)) {
                                String[] token = str.split("\\s");
                                Platform.runLater(() -> {
                                    clientList.getItems().clear();
                                    for (int i = 1; i < token.length; i++) {
                                        clientList.getItems().add(token[i]);
                                    }
                                });
                            }
                        } else {
                            textArea.appendText(str + "\n");
                        }
                    }
                } catch (RuntimeException e) {
                    System.out.println(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    setAuthenticated(false);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(ActionEvent actionEvent) {
        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void enterSend() {
        textField.setOnKeyPressed(keyEvent -> {
            if (textField != null) {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    try {
                        out.writeUTF(textField.getText() + "\n");
                        textField.clear();
                        textField.requestFocus();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    textField.clear();

                }
            }
        });

    }

    @FXML
    void sendMsgButton() {

        if (textField != null) {

            try {
                out.writeUTF(textField.getText() + "\n");
                textField.clear();
                textField.requestFocus();
            } catch (Exception e) {
                System.out.println("Регистрация не загружается");
            }

        }
    }

    @FXML
    void closeChat() {
        try {
            Stage stage = (Stage) closeBTN.getScene().getWindow();
            out.writeUTF(Command.END);
            stage.close();
            stage.setOnCloseRequest(e -> Platform.exit());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void buttonAnimatedClose(MouseEvent mouseEvent) {
        closeBTN.setStyle("-fx-background-color: #ab0000;" + "-fx-text-fill: #000000;");
    }

    public void buttonAnimated(MouseEvent mouseEvent) {
        buttonSend.setStyle("-fx-background-color: #d9FFbf;" + "-fx-text-fill: #000000;");
    }

    public void buttonAnimatedSignIn(MouseEvent mouseEvent) {
        signIn_btn.setStyle("-fx-background-color: #42f5f5;" + "-fx-text-fill: #000000;");
    }

    public void buttonAnimatedSignUp(MouseEvent mouseEvent) {
        signUp_btn.setStyle("-fx-background-color: #42f5f5;" + "-fx-text-fill: #000000;");
    }


    public void buttonFree(MouseEvent mouseEvent) {
        buttonSend.setStyle("-fx-background-color: #57998d;");
        closeBTN.setStyle("-fx-background-color: #57998d;");
        signIn_btn.setStyle("-fx-background-color: #57998d;");
        signUp_btn.setStyle("-fx-background-color: #57998d;");

    }

    public void tryToAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            out.writeUTF(String.format("%s %s %s", Command.AUTH, login_field.getText().trim(), password_field.getText().trim()));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            password_field.clear();
        }
    }

    private void setTitle(String nickname) {
        Platform.runLater(() -> {
            if (nickname.equals("")) {
                stage.setTitle("TalkY");
            } else {
                nameSetText.setText(String.format("Talky - [ %s ]", nickname));
            }
        });
    }

    public void clientListMouseReleased(MouseEvent mouseEvent) {
        System.out.println(clientList.getSelectionModel().getSelectedItem());
        String msg = String.format("%s %s ", Command.PERSONAL_MSG, clientList.getSelectionModel().getSelectedItems());
        textField.setText(msg);
    }

    public void showRegWindow(ActionEvent actionEvent) {
        if (regStage == null) {
            initRegWindow();
        }
        regStage.show();
    }

    private void initRegWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/reg.fxml"));
            Parent root = fxmlLoader.load();

            regController = fxmlLoader.getController();
            regController.setController(this);

            regStage = new Stage();
            regStage.initStyle(StageStyle.UTILITY);
            regStage.setTitle("ЧатиК - Регистрация");
            regStage.setScene(new Scene(root, 405, 500));
            regStage.getIcons().add(new Image("/Tblack.png"));
            regStage.setResizable(false);
            regStage.initModality(Modality.APPLICATION_MODAL);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registration(String login, String password, String nickname) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        try {
            out.writeUTF(String.format("%s %s %s %s", Command.REG, login, password, nickname));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


