package client.gui;

import client.QuizduellApplication;
import client.connect.server.ConnectionService;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class GUINameChoose implements Scenebuilder {
  private final QuizduellApplication quizduellApplication;

  public GUINameChoose(QuizduellApplication quizduellApplication) {
    this.quizduellApplication = quizduellApplication;
  }

  @Override
  public Scene fetchScene() {
    GridPane grid = new GridPane();
    grid.setAlignment(Pos.CENTER);
    grid.setHgap(10);
    grid.setVgap(10);
    Label name = new Label("Trage hier deinen Namen ein.");
    grid.add(name, 0, 0);
    TextField nameFeld = new TextField();
    Button btnconfirm = new Button("Bestätigen");
    nameFeld.textProperty().addListener((observable, oldValue, newValue) -> {
      btnconfirm.setDisable(newValue == null || newValue.isEmpty());
//      if(newValue != null && !newValue.isEmpty()) {
//        newValue = stripEverythingElseThanChars(newValue);
//        if (newValue.equals(oldValue)) {
//          nameFeld.textProperty().setValue(newValue);
//        }
//      }
    });
    grid.add(nameFeld, 0, 1);
    btnconfirm.setDisable(true);
    grid.add(btnconfirm, 0, 2);
    btnconfirm.setOnAction(event -> {
      String setName = nameFeld.textProperty().get();
      ConnectionService connectionService = quizduellApplication.serverConnection();
      connectionService.writeData("LOBBY_ENTER", setName);

      Task<String> stupidTask = new Task<String>(){
        @Override
        protected String call() {
          String x = connectionService.requireRawData();
          return x.split("->")[1];
        }
      };

      stupidTask.setOnSucceeded(event1 -> {
        String packetData = (String) event1.getSource().getValue();

        GUILobby guiLobby = new GUILobby(quizduellApplication);
        guiLobby.applyData(packetData);
        quizduellApplication.primaryStage().setScene(guiLobby.fetchScene());
      });

      new Thread(stupidTask).start();
    });
    return new Scene(grid, 750, 450);
  }

  private String stripEverythingElseThanChars(String input) {
    char[] chars = new char[input.length()];
    int cursor = 0;
    for (char c : input.toCharArray()) {
      if(Character.isAlphabetic(c)) {
        chars[cursor++] = c;
      }
    }
    System.arraycopy(chars, 0, chars, 0, cursor + 1);
    return new String(chars);
  }
}