package client.gui;

import client.QuizduellApplication;
import client.connect.server.ConnectionService;
import client.game.GameMode;
import client.gui.image.ImageLocation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

public class GUIMainMenu implements Scenebuilder {
  private final QuizduellApplication quizduellApplication;

  public GUIMainMenu(QuizduellApplication quizduellApplication) {
    this.quizduellApplication = quizduellApplication;
  }

  public Scene fetchScene() {
    GridPane grid = new GridPane();
    grid.setAlignment(Pos.CENTER);
    grid.setHgap(10);
    grid.setVgap(10);

    Image logo = new Image(new ImageLocation("client/gui/image/quizduell_logo.png").loaded().localPathJXParsed());

    ImageView iv1 = new ImageView();
    iv1.setImage(logo);
    iv1.setFitHeight(118);
    iv1.setFitWidth(370);
    grid.add(iv1, 1, 0);

    Button singleplayer = new Button("Singleplayer");
    singleplayer.setOnAction(event -> {
      startGame(GameMode.SINGLEPLAYER);
    });
    grid.add(singleplayer, 0, 1);

    Button multiplayer = new Button("Multiplayer");
    multiplayer.setOnAction(event -> {
      startGame(GameMode.MULTIPLAYER);
    });
    grid.add(multiplayer, 2, 1);
    return new Scene(grid, 750, 450);
  }

  private void startGame(GameMode gameMode) {
    ConnectionService connectionService = quizduellApplication.serverConnection();
    connectionService.writeData("ENTER_GAME", gameMode.name());

    if(gameMode == GameMode.SINGLEPLAYER) {
      startSingleplayerGame();
    } else {
      startMultiplayerGame();
    }
  }

  private void startSingleplayerGame() {
    ConnectionService connectionService = quizduellApplication.serverConnection();
    String input = connectionService.requireRawData();
    String[] inputSplit = input.split("->")[1].split("\\*\\^\\*");
    String question = inputSplit[0];
    String[] answers = new String[4];
    System.arraycopy(inputSplit, 1, answers, 0, 4);
    GUIIngameSingleplayer guiIngameSingleplayer = new GUIIngameSingleplayer(quizduellApplication, question, answers);
    quizduellApplication.primaryStage().setScene(guiIngameSingleplayer.fetchScene());
  }

  private void startMultiplayerGame() {
    GUINameChoose guiNameChoose = new GUINameChoose(quizduellApplication);
    quizduellApplication.primaryStage().setScene(guiNameChoose.fetchScene());
  }
}