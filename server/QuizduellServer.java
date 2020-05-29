package server;

import server.database.DatabaseService;
import server.game.GameService;

public final class QuizduellServer {
  private DatabaseService databaseService;
  private GameService gameService;
  private ConnectionService connectionService;

  public QuizduellServer() {
  }

  public void setup() {
    databaseService = new DatabaseService();
    databaseService.tryConnect("localhost", 3306, "schule", "tess", "12345678");
    connectionService = new ConnectionService(1346);
    connectionService.startServer();
    gameService = new GameService(databaseService);
  }

  public void start() {

  }

  public static void main(String[] args) {
    QuizduellServer quizduellServer = new QuizduellServer();
    quizduellServer.setup();
    quizduellServer.start();
  }
}
