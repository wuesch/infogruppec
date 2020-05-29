package client;

import client.gui.QuizduellApplication;
import client.net.TestServerConnection;

public final class Quizduell {

  private GameProcessor game;
  private TestServerConnection serverConnection = new TestServerConnection();
  private QuizduellApplication guiService = new QuizduellApplication();

  public void setup() {
    serverConnection.start();
    guiService.launch();
  }

  public static void main(String[] args) {
    new Quizduell().setup();
  }
}