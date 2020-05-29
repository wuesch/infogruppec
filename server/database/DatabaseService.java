package server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class DatabaseService {
  private Connection currentConnection;

  public void tryConnect(
    String host, int port, String database,
    String user, String password
  ) {
    String connectionURL = buildConnectionURL(host, port, database, user, password);
    try {
      currentConnection = DriverManager.getConnection(connectionURL);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private String buildConnectionURL(
    String host, int port, String database,
    String user, String password
  ) {
    return "jdbc:mysql://"+host+":"+port+"/"+database+"?user="+user+"&password="+password+"&autoReconnect=true";
  }

  public void execute(String command) {
    try {
      currentConnection.createStatement().execute(command);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public ResultSet search(String command) {
    try {
      return currentConnection.createStatement().executeQuery(command);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }
}
