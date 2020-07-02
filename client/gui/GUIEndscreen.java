package client.gui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

public class GUIEndscreen implements Scenebuilder {

  @Override
  public Scene fetchScene() {
    GridPane grid = new GridPane();
    grid.setAlignment(Pos.CENTER);
    grid.setHgap(10);
    grid.setVgap(10);

    Text titel = new Text("Rangliste");
    grid.add(titel, 1, 0);

    Text spieler1 = new Text("platzhalter");
    grid.add(spieler1, 1, 2);

    Text spieler2 = new Text("platzhalter");
    grid.add(spieler2, 1, 3);

    Text spieler3 = new Text("platzhalter");
    grid.add(spieler3, 1, 4);

    Text spieler4 = new Text("platzhalter");
    grid.add(spieler4, 1, 5);

    Text spieler5 = new Text("platzhalter");
    grid.add(spieler5, 1, 6);

    Text spieler6 = new Text("platzhalter");
    grid.add(spieler6, 1, 7);

    Text spieler7 = new Text("platzhalter");
    grid.add(spieler7, 1, 8);

    Text spieler8 = new Text("platzhalter");
    grid.add(spieler8, 1, 9);

    Text spieler9 = new Text("platzhalter");
    grid.add(spieler9, 1, 10);

    Text spieler10 = new Text("platzhalter");
    grid.add(spieler10, 1, 11);


    Text rang1 = new Text("1. Platz");
    grid.add(rang1, 0, 2);

    Text rang2 = new Text("2. Platz");
    grid.add(rang2, 0, 3);

    Text rang3 = new Text("3. Platz");
    grid.add(rang3, 0, 4);


    Text punkte1 = new Text("platzhalter punkte");
    grid.add(punkte1, 2, 2);

    Text punkte2 = new Text("platzhalter punkte");
    grid.add(punkte2, 2, 3);

    Text punkte3 = new Text("platzhalter punkte");
    grid.add(punkte3, 2, 4);

    Text punkte4 = new Text("platzhalter punkte");
    grid.add(punkte4, 2, 5);

    Text punkte5 = new Text("platzhalter punkte");
    grid.add(punkte5, 2, 6);

    Text punkte6 = new Text("platzhalter punkte");
    grid.add(punkte6, 2, 7);

    Text punkte7 = new Text("platzhalter punkte");
    grid.add(punkte7, 2, 8);

    Text punkte8 = new Text("platzhalter punkte");
    grid.add(punkte8, 2, 9);

    Text punkte9 = new Text("platzhalter punkte");
    grid.add(punkte9, 2, 10);

    Text punkte10 = new Text("platzhalter punkte");
    grid.add(punkte10, 2, 11);


    return new Scene(grid, 750, 450);
  }
}