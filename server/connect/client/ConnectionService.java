package server.connect.client;

import server.QuizduellServer;
import server.game.Player;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public final class ConnectionService {
  private final static AtomicInteger playerIdCounter = new AtomicInteger();

  /**
   *  0. Verbindung wird erstellt
   *  1. Spieler sagt Server den gewünschten Spielmodus (Single oder Multiplayer) | (ENTER_GAME)
   *
   *  Singleplayer
   *    2. Server antwortet mit Frage und 4 Antworten, aber nicht mit richtiger Frage! | (GIVE_QUESTION)
   *    3. Spieler nennt Antwort | (TRY_ANSWER)
   *    4. Server nennt richtige Antwort und ob richtig oder falsch und geht wieder zu 2. über | (RESULT)
   *
   *  Multiplayer
   *   yet to come
   *
   *
   */

  private final List<Player> players = new CopyOnWriteArrayList<>();
  private final QuizduellServer quizduellServer;
  private final int port;
  private ServerSocket coreServerSocket;

  public ConnectionService(QuizduellServer quizduellServer, int port) {
    this.quizduellServer = quizduellServer;
    this.port = port;
  }

  public void startServer() throws IOException {
    prepareServerSocket();
    bootProcessorThread();
  }

  private void prepareServerSocket() throws IOException {
    coreServerSocket = new ServerSocket(port);
  }

  public void awaitIncomingConnections() {
    try {
      Thread thisThread = Thread.currentThread();
      while(!thisThread.isInterrupted()) {
        acceptNewConnection(coreServerSocket.accept());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void bootProcessorThread() {
    // new thread (runs in parallel)
    new Thread(() -> {
      Thread thisThread = Thread.currentThread();
      // while thread is not dead
      while (!thisThread.isInterrupted()) {
        // loop through all players
        for (Player player : players) {
          // check if connection was dropped
          if(!player.isConnected()) {
            // process connection drop as exit
            quizduellServer.gameService().processGameExit(player);
            continue;
          }
          // read input
          String rawData = player.readRawData();
          // check if player has sent something
          if(rawData != null) {
            // forward data
            processRawData(player, rawData);
          }
        }
        // wait 100ms before checking for data again
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }, "net/io-processor").start();
  }

  public void acceptNewConnection(Socket socket) {
    int nextPlayerIdentifier = playerIdCounter.getAndIncrement();
    Player player = new Player(nextPlayerIdentifier, socket);
    player.prepareSocketIO();
    players.add(player);
  }

  private void processRawData(Player player, String inputData) {
    String[] inputDataSplit = inputData.split("->");
    String label = inputDataSplit[0]; // the name/label of the sent data
    String data = inputDataSplit[1]; // the data itself, packed into a string
    // internal data processing
    quizduellServer.gameService().receiveLabeledData(player, label, data);
  }
}
