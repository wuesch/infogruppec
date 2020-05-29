package client.gui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

public class GUIMainMenu implements QuizduellGUI {

  public Scene fetchScene() {
    GridPane grid = new GridPane();
    grid.setAlignment(Pos.CENTER);
    grid.setHgap(10);
    grid.setVgap(10);

    Image logo = new Image("file:client/image/quizduell_logo.png");

    ImageView iv1 = new ImageView();
    iv1.setImage(logo);
    iv1.setFitHeight(118);
    iv1.setFitWidth(370);
    grid.add(iv1, 1, 0);

    Button singleplayer = new Button("Singleplayer");
    grid.add(singleplayer, 0, 1);

    Button multiplayer = new Button("Multiplayer");
    grid.add(multiplayer, 2, 1);

    return new Scene(grid, 750, 450);
    //primaryStage.setScene(scene);
    //primaryStage.show();
  }
}