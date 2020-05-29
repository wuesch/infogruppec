package client.gui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

public class GUIIngame implements QuizduellGUI {

  private final String frage;
  private final String[] antworten;

  public GUIIngame(String frage, String[] antworten) {
    this.frage = frage;
    this.antworten = antworten;
  }

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

    Text frageText = new Text(frage);
    grid.add(frageText, 1, 1);

    RadioButton button1ol = new RadioButton(antworten[0]);
    grid.add(button1ol, 0, 2);
    RadioButton button2or = new RadioButton(antworten[1]);
    grid.add(button2or, 2, 2);
    RadioButton button3ul = new RadioButton(antworten[2]);
    grid.add(button3ul, 0, 3);
    RadioButton button4ur = new RadioButton(antworten[3]);
    grid.add(button4ur, 2, 3);

    Button confirm = new Button("Antwort bestätigen");
    grid.add(confirm, 1, 4);

    return new Scene(grid, 750, 450);
  }
}