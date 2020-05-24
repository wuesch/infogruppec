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

public class gui_startbildschirm extends Application
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

     
        
        Button singleplayer = new Button("Singleplayer");
        grid.add(singleplayer, 0, 1);
        
        Button multiplayer = new Button("Multiplayer");
        grid.add(multiplayer, 2, 1);

        
        Scene scene = new Scene(grid, 750, 450);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}