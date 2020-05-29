package client.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public final class QuizduellApplication extends Application {
  private Stage stage;

  public void launch() {
    launch(new String[0]);
  }

  @Override
  public void start(Stage primaryStage) {
    stage = primaryStage;
    primaryStage.setTitle("Quizduell");
    primaryStage.getIcons().add(new Image("file:client/image/quizduell_icon.png"));
  }

  public void setScene(QuizduellGUI quizduellGUI) {
    setScene(quizduellGUI.fetchScene());
  }

  public void setScene(Scene scene) {
    stage.setScene(scene);
    stage.show();
  }
}
