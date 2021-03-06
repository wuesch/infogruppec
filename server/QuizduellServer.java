package server;

import server.connect.client.ConnectionService;
import server.connect.database.DatabaseService;
import server.game.GameService;

public final class QuizduellServer {
  private static QuizduellServer singletonInstance;

  private GameService gameService;
  private DatabaseService databaseService;
  private ConnectionService connectionService;

  public QuizduellServer() {
  }

  public void boot() {
    try {
      databaseService = new DatabaseService();
      databaseService.tryConnect("localhost", 3306, "schule", "root", "");
      connectionService = new ConnectionService(this, 8749);
      connectionService.startServer();
      gameService = new GameService(databaseService);
    } catch (Exception exception) {
      throw new IllegalStateException("Unable to start server", exception);
    }
  }

  public void awaitIncomingConnections() {
    connectionService.awaitIncomingConnections();
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
    singletonInstance = new QuizduellServer();
    singletonInstance.boot();
    singletonInstance.awaitIncomingConnections();
  }

  public static QuizduellServer singletonInstance() {
    return singletonInstance;
  }
}
