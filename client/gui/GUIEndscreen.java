package client.gui;

import client.QuizduellApplication;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.util.*;
import java.util.stream.Collectors;

public class GUIEndscreen implements Scenebuilder {
  private final QuizduellApplication quizduellApplication;
  private Map<String, Integer> rankList = new HashMap<>();

  public GUIEndscreen(QuizduellApplication quizduellApplication) {
    this.quizduellApplication = quizduellApplication;
  }

  public void applyFrom(String packetData) {
    String[] split = packetData.split("@");
    String[] playersUnordered = split[0].split(";");
    String[] scores = split[1].split(";");
    for (int i = 0; i < scores.length; i++) {
      String name = playersUnordered[i];
      int score = Integer.parseInt(scores[i]);
      rankList.put(name, score);
    }
    rankList = rankList
      .entrySet()
      .stream()
      .sorted(Collections.reverseOrder(Map.Entry.comparingByValue(Integer::compareTo)))
      .collect(Collectors.toMap(
        Map.Entry::getKey,
        Map.Entry::getValue,
        (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); },
        LinkedHashMap::new
      ));
  }

  @Override
  public Scene fetchScene() {
    GridPane grid = new GridPane();
    grid.setAlignment(Pos.CENTER);
    grid.setHgap(10);
    grid.setVgap(10);

    Text titel = new Text("Rangliste");
    grid.add(titel, 1, 0);

    int index = 0;
    for (Map.Entry<String, Integer> playerRankEntry : rankList.entrySet()) {
      grid.add(new Text(playerRankEntry.getKey()), 1, index + 2);
      grid.add(new Text(String.valueOf(playerRankEntry.getValue())), 2, index + 2);
      index++;
    }

    Text rang1 = new Text("1. Platz");
    grid.add(rang1, 0, 2);

    Text rang2 = new Text("2. Platz");
    grid.add(rang2, 0, 3);

    Text rang3 = new Text("3. Platz");
    grid.add(rang3, 0, 4);

    Button returnButton = new Button("Back");
    returnButton.setOnAction(event -> {
      quizduellApplication.primaryStage().setScene(new GUIMainMenu(quizduellApplication).fetchScene());
    });
    grid.add(returnButton, 0, 0);
    return new Scene(grid, 750, 450);
  }
}