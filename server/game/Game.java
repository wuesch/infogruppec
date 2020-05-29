package server.game;

import java.io.InputStream;
import java.util.ArrayList;

public abstract class Game {
  public ArrayList<Player> players = new ArrayList<>();
  public Question currentQuestion;

  public abstract void receiveIncomingData(Player player, String dataLabel, String data);

}
