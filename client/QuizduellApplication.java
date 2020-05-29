package client;

import client.gui.GUIMainMenu;
import client.connect.server.ConnectionService;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public final class QuizduellApplication extends Application {
  private final ConnectionService serverConnection = new ConnectionService();
  private Stage primaryStage;

  public void start(Stage primaryStage) {
    serverConnection.startConnection();
    primaryStage.setTitle("Quizduell");
    primaryStage.getIcons().add(new Image("file:client/gui/image/quizduell_icon.png"));
    primaryStage.setScene(new GUIMainMenu(this).fetchScene());
    primaryStage.show();

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