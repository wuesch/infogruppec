package client.gui;

import client.QuizduellApplication;
import client.connect.server.ConnectionService;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class GUIIngameSingleplayer implements Scenebuilder {

  private final QuizduellApplication quizduellApplication;
  private final String frage;
  private final String[] antworten;

  public GUIIngameSingleplayer(
    QuizduellApplication quizduellApplication,
    String frage, String[] antworten
  ) {
    this.quizduellApplication = quizduellApplication;
    this.frage = frage;
    this.antworten = antworten;
  }

  public Scene fetchScene() {
    GridPane grid = new GridPane();
    grid.setAlignment(Pos.CENTER);
    grid.setHgap(10);
    grid.setVgap(10);
    Image logo = new Image("file:client/gui/image/quizduell_logo.png");
    ImageView iv1 = new ImageView();
    iv1.setImage(logo);
    iv1.setFitHeight(118);
    iv1.setFitWidth(370);
    grid.add(iv1, 1, 0);

    int horizontalStartIndex;
    if(frage.length() > 100) {
      horizontalStartIndex = 4;

      int goodIndex = 100;
      while (frage.charAt(goodIndex++) != ' ' && goodIndex < frage.length()) {}

      Text frageText = new Text(frage.substring(0, goodIndex).trim());
      grid.add(frageText, 1, 1);

      Text frageText2 = new Text(frage.substring(goodIndex).trim() + "?");
      grid.add(frageText2, 1, 2);
    } else {
      horizontalStartIndex = 2;
      Text frageText = new Text(frage + "?");
      grid.add(frageText, 1, 1);
    }

    Button confirm = new Button("Antwort bestätigen");
    confirm.setDisable(true);

    List<RadioButton> buttons = new ArrayList<>();

    int buttonsPerColumn = 2;
    for (int i = 0; i < 4; i++) {
      RadioButton radioButton = new RadioButton(antworten[i]);
      radioButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
        if(newValue) {
          for (RadioButton button : buttons) {
            if(button != radioButton) {
              button.selectedProperty().setValue(false);
            }
          }
          confirm.setDisable(false);
        } else {
          boolean hasEnabled = false;
          for (RadioButton button : buttons) {
            if(button.selectedProperty().get()) {
              hasEnabled = true;
            }
          }
          if(!hasEnabled) {
            confirm.setDisable(true);
          }
        }
      });
      grid.add(radioButton, i % 2 + 1, i / buttonsPerColumn + horizontalStartIndex);
      buttons.add(radioButton);
    }

    Text loesungsInfo = new Text("");
    grid.add(loesungsInfo, 1, horizontalStartIndex + 3);

    confirm.setOnAction(event -> {
      int selectedIndex = -1;

      for (int i = 0; i < 4; i++) {
        RadioButton radioButton = buttons.get(i);
        if(radioButton.selectedProperty().get()) {
          selectedIndex = i;
          break;
        }
      }
      if(selectedIndex < 0) {
        throw new IllegalStateException();
      }

      ConnectionService connectionService = quizduellApplication.serverConnection();
      connectionService.writeData("TRY_ANSWER", String.valueOf(selectedIndex));

      String result = connectionService.requireRawData();
      String[] resultSplit = result.split("->")[1].split("::");

      int correctIndex = Integer.parseInt(resultSplit[0]);
      boolean correct = Boolean.parseBoolean(resultSplit[1]);

      loesungsInfo.setFill(correct ? Color.GREEN : Color.RED);
      loesungsInfo.setText(correct ? "Richtig" : "Falsch. Richtige Antwort: " + antworten[correctIndex]);

      String input = connectionService.requireRawData();

      for (RadioButton button : buttons) {
        button.setDisable(true);
      }
      confirm.setDisable(true);

      Task<Void> sleeper = new Task<Void>() {
        @Override
        protected Void call() {
          try {
            Thread.sleep(2000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          return null;
        }
      };

      sleeper.setOnSucceeded(event1 -> {
        String[] inputSplit = input.split("->")[1].split("\\*\\^\\*");

        String question = inputSplit[0];
        String[] answers = new String[4];
        System.arraycopy(inputSplit, 1, answers, 0, 4);
        GUIIngameSingleplayer guiIngameSingleplayer = new GUIIngameSingleplayer(quizduellApplication, question, answers);
        quizduellApplication.primaryStage().setScene(guiIngameSingleplayer.fetchScene());
      });

      new Thread(sleeper).start();
    });

    grid.add(confirm, 1, horizontalStartIndex + 2);
    return new Scene(grid, 750, 450);
  }
}