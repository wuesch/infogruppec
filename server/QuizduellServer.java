package server;

import server.database.DatabaseService;
import server.game.GameService;

public final class QuizduellServer {
  private GameService gameService;
  private DatabaseService databaseService;
  private ConnectionService connectionService;

  public QuizduellServer() {
  }

  public void bootBlocking() {
    try {
      databaseService = new DatabaseService();
      databaseService.tryConnect("localhost", 3306, "schule", "tess", "12345678");
      connectionService = new ConnectionService(this, 8749);
      connectionService.startServer();
      gameService = new GameService(databaseService);
    } catch (Exception exception) {
      throw new IllegalStateException("Unable to start server", exception);
    }
  }

  public DatabaseService databaseService() {
    return databaseService;
  }

  public GameService gameService() {
    return gameService;
  }

  public ConnectionService connectionService() {
    return connectionService;
  }

  public static void main(String[] args) {
    QuizduellServer quizduellServer = new QuizduellServer();
    quizduellServer.bootBlocking();
  }
}
