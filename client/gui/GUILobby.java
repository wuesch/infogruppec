package client.gui;

import client.QuizduellApplication;
import client.connect.server.ConnectionService;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GUILobby implements Scenebuilder {
  public static boolean playerIsReady;
  private final Executor executor = Executors.newFixedThreadPool(1);
  private final QuizduellApplication quizduellApplication;

  private int timerInSeconds;
  private String[] players;
  private boolean[] readyStates;

  public GUILobby(QuizduellApplication quizduellApplication) {
    this.quizduellApplication = quizduellApplication;
  }

  // Richy;Julia;Max@1;0;1@30
  public void applyData(String data) {
    String[] layout = data.split("@");
    String rawNames = layout[0];
    this.players = rawNames.split(";");

    String rawReadies = layout[1];
    String[] readiesUnparsed = rawReadies.split(";");
    boolean[] readies = new boolean[readiesUnparsed.length];
    for (int i = 0; i < readiesUnparsed.length; i++) {
      String s = readiesUnparsed[i];
      readies[i] = s.equals("1");
    }
    this.readyStates = readies;

    String countdown = layout[2];
    this.timerInSeconds = Integer.parseInt(countdown);

    ConnectionService connectionService = quizduellApplication.serverConnection();
    connectionService.writeData("LOBBY_KEEP_ALIVE", String.valueOf(playerIsReady ? 1 : 0));

    Task<String> stupidTask = new Task<String>(){
      @Override
      protected String call() {
        return connectionService.requireRawData();
      }
    };

    stupidTask.setOnSucceeded(event1 -> {
      String packet = (String) event1.getSource().getValue();
      String[] packetSplit = packet.split("->");
      String packetName = packetSplit[0];
      String packetData = packetSplit[1];

      if(packetName.equals("LOBBY_INFO")) {
        GUILobby guiLobby = new GUILobby(quizduellApplication);
        guiLobby.applyData(packetData);
        quizduellApplication.primaryStage().setScene(guiLobby.fetchScene());
      } else if (packetName.equals("GAME_PREPARE")){
        GUIIngameMultiplayer guiMP = new GUIIngameMultiplayer(quizduellApplication);
        guiMP.setupWithGamePrepareData(packetData);
        guiMP.pushDataRequirement();
        quizduellApplication.primaryStage().setScene(guiMP.fetchScene());
      }
    });

    executor.execute(stupidTask);
  }

  @Override
  public Scene fetchScene() {
    GridPane grid = new GridPane();
    grid.setAlignment(Pos.CENTER);
    grid.setHgap(10);
    grid.setVgap(10);

    Text titel = new Text("Quizduell Lobby");
    grid.add(titel, 0, 0);

    Label spieler = new Label("Alle Mitspieler:");
    grid.add(spieler, 0, 1);

    for (int i = 0; i < players.length; i++) {
      boolean playerReady = readyStates[i];
      String readyDisplay = playerReady ? "Bereit" : "Noch nicht bereit";
      Text spielerX = new Text(players[i] + " " + readyDisplay);
      grid.add(spielerX, 0, 2 + i);
    }

    Button btnready = new Button("Bereit");
    btnready.setDisable(playerIsReady);
    btnready.setOnAction(event -> {
      btnready.setDisable(true);
      playerIsReady = true;
    });
    grid.add(btnready, 0, 2 + players.length);
    return new Scene(grid, 750, 450);
  }
}