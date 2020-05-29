package server;

import server.game.Player;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public final class ConnectionService {

  /**
   *  0. Verbindung wird erstellt
   *  1. Spieler sagt Server den gewünschten Spielmodus (Single oder Multiplayer) | (REQUEST_GAMEMODE)
   *
   *  Singleplayer
   *    2. Server antwortet mit Frage und 4 Antworten, aber nicht mit richtiger Frage! | (GIVE_QUESTION)
   *    3. Spieler nennt Antwort | (TRY_ANSWER)
   *    4. Server nennt richtige Antwort und ob richtig und geht wieder zu 2. über | (RESULT)
   *
   *  Multiplayer
   *   Später mal in 100 Jahren juckt es uns vielleicht
   *
   *
   */

  private int cursor;
  private int port;

  public ConnectionService(int port) {
    this.port = port;
  }

  public void startServer() {
    new Thread(() -> {
      try {
        ServerSocket serverSocket = new ServerSocket(port);
        while(!Thread.currentThread().isInterrupted()) {
          acceptNewConnection(serverSocket.accept());
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }, "connection-thread").start();
  }

  public void acceptNewConnection(Socket socket) {
    Player player = new Player(cursor++, socket);

    try {
      socket.setTcpNoDelay(true);
      socket.setKeepAlive(true);
    } catch (SocketException e) {
      e.printStackTrace();
    }





  }
}
