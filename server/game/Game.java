package server.game;

import server.question.Question;

import java.util.List;

public abstract class Game {
  public Question currentQuestion;
  public List<Player> players;

  private long started;

  public Game(List<Player> players) {
    this.players = players;
    this.started = System.currentTimeMillis();
  }

  public boolean hasPlayer(Player player) {
    return players.contains(player);
  }

  public void setCurrentQuestion(Question currentQuestion) {
    this.currentQuestion = currentQuestion;
  }

  public abstract void receiveIncomingData(Player player, String dataLabel, String data);

  public void notifyUpdate() {

  }
}
