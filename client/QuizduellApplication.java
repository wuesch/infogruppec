package client;

import client.connect.server.ConnectionService;
import client.gui.GUIMainMenu;
import client.gui.image.ImageLocation;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public final class QuizduellApplication extends Application {
  private final ConnectionService serverConnection = new ConnectionService();
  private Stage primaryStage;

  public void start(Stage primaryStage) {
    serverConnection.startConnection();
    serverConnection.startSocketReaderThread();
    primaryStage.setTitle("Quizduell");
    primaryStage.getIcons().add(new Image(new ImageLocation("client/gui/image/quizduell_icon.png").loaded().localPathJXParsed()));
    primaryStage.setScene(new GUIMainMenu(this).fetchScene());
    primaryStage.setResizable(false);
    primaryStage.setOnCloseRequest(event -> System.exit(0));
    primaryStage.show();
    primaryStage.requestFocus();

    this.primaryStage = primaryStage;
  }

  public Stage primaryStage() {
    return primaryStage;
  }

  public ConnectionService serverConnection() {
    return serverConnection;
  }

  public static void main(String[] args) {
    QuizduellApplication.launch(QuizduellApplication.class);
  }
}