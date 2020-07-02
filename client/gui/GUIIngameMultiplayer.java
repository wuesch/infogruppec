package client.gui;

import client.QuizduellApplication;
import client.connect.server.ConnectionService;
import client.connect.server.Serialization;
import javafx.concurrent.Task;
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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GUIIngameMultiplayer implements Scenebuilder {
  private final QuizduellApplication quizduellApplication;
  private final Executor executor = Executors.newFixedThreadPool(1);
  private String frage;
  private String[] antworten;
  private String[] playerNames;
  private boolean[] finished;
  private int countdown;

  private Text infoText;

  public GUIIngameMultiplayer(QuizduellApplication quizduellApplication) {
    this.quizduellApplication = quizduellApplication;
  }

  public void setupWithGamePrepareData(String gamePrepare) {
    String[] split = gamePrepare.split("@");

    String questionStruct = split[0];
    String[] inputSplit = questionStruct.split("->")[1].split("\\*\\^\\*");
    String question = inputSplit[0];
    String[] answers = new String[4];
    System.arraycopy(inputSplit, 1, answers, 0, 4);

    String playerStruct = split[1];
    String[] playerNames = playerStruct.split(";");

    this.frage = question;
    this.antworten = answers;
    this.playerNames = playerNames;
    this.finished = new boolean[playerNames.length];
  }

  public void pushDataRequirement() {
    Task<String> aquirePacket = new Task<String>() {
      @Override
      protected String call() {
        return awaitData();
      }
    };
    aquirePacket.setOnSucceeded(event -> {
      String packet = (String) event.getSource().getValue();
      if(processData(packet)) {
        pushDataRequirement();
      }
    });
    executor.execute(aquirePacket);
  }

  private String awaitData() {
    return quizduellApplication.serverConnection().requireRawData();
  }

  private boolean processData(String packet) {
    String label = Serialization.labelFromPacket(packet);
    String data = Serialization.dataFromPacket(packet);

    switch (label) {
      case "GAME_STATUS":
        // display game info
        String[] split = data.split("@");
        String givenAnswerStruct = split[0];
        String countdown = split[1];

        String[] playerAnswered = givenAnswerStruct.split(";");

        for (int i = 0; i < playerAnswered.length; i++) {
          this.finished[i] = playerAnswered[i].equals("1");
        }

        this.countdown = Integer.parseInt(countdown);

        return true;

      case "GAME_RESULT":
        // show answer

        return true;

      case "GAME_PREPARE":
        // next game

        return false;

      case "GAME_FINISH":
        // show endscreen


        return false;
    }


    return false;
  }

  private void updateInfoText() {
    sync(() -> {
      int ready = 0;
      for (boolean b : finished) {
        ready = b ? ready + 1 : ready;
      }
      infoText.setText(ready + "/"+finished.length + " have given an answer ");
    });
  }

  private void sync(Runnable runnable) {
    Task<Void> voidTask = new Task<Void>() {
      @Override
      protected Void call() {
        return null;
      }
    };
    voidTask.setOnSucceeded(event -> runnable.run());
    voidTask.run();
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
      while (frage.charAt(goodIndex++) != ' ' && goodIndex < frage.length());
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


    });
    grid.add(confirm, 1, horizontalStartIndex + 2);
    return new Scene(grid, 750, 450);
  }
}