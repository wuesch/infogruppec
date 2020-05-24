import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.Random;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.RadioButton;

public class gui_fragebildschirm extends Application
{
    @Override
    public void start(Stage primaryStage){
        primaryStage.setTitle("Quizduell");
        primaryStage.getIcons().add(new Image("file:image/quizduell_icon.png"));

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);

                Image logo = new Image("file:image/quizduell_logo.png");
        
        ImageView iv1 = new ImageView();
        iv1.setImage(logo);
        iv1.setFitHeight(118);
        iv1.setFitWidth(370);
        grid.add(iv1, 1, 0);
        
     
        
        Text frage = new Text("frage_platzhalter");
        grid.add(frage, 1, 1);
        
        RadioButton button1ol = new RadioButton("antwort_platzhalter ol");
        grid.add(button1ol, 0, 2);
        RadioButton button2or = new RadioButton("antwort_platzhalter or");
        grid.add(button2or, 2, 2);
        RadioButton button3ul = new RadioButton("antwort_platzhalter ul");
        grid.add(button3ul, 0, 3);
        RadioButton button4ur = new RadioButton("antwort_platzhalter ur");
        grid.add(button4ur, 2, 3);
        
        Button confirm = new Button("Antwort bestätigen");
        grid.add(confirm, 1, 4);
        
        Scene scene = new Scene(grid, 750, 450);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}