package server.connect.client;

import server.QuizduellServer;
import server.game.GameService;
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
   *  1. Spieler sagt Server den gewünschten Spielmodus (Single oder Multiplayer) (ENTER_GAME)
   *
   *  Singleplayer
   *    2. Server nennt Frage und 4 Antworten, aber nicht die richtiger Frage! (GIVE_QUESTION)
   *    3. Spieler nennt Antwort (TRY_ANSWER)
   *    4. Server nennt richtige Antwort und ob richtig oder falsch und geht wieder zu 2. über (RESULT)
   *
   *  Multiplayer
   *    2. Client schickt dem Server den Spielernamen (LOBBY_ENTER)
   *    3. Server antwortet mit Lobby Informationen (zz. Bestätigung) (LOBBY_INFO)
   *    4. Client muss mit LOBBY_KEEP_ALIVE antworten (mit Bestätiungsinformation) um dann wieder auf 3. zu warten
   *    5. Server schickt initialisierende Spieldaten mit Index -> Name Spieler Map und nennt Frage und 4 mögliche Antworten (GAME_PREPARE)
   *    6. Server schickt jede Sekunde einen Countdown zusätzlich mit den Indices den Spieler, die eine Antwort gegeben haben (GAME_STATUS)
   *    7. Client schickt dem Server gegebene Antwort (GAME_ANSWER) (6 still applies)
   *    8. Server nennt wer richtig lag und wer nicht, mit der richtigen Antwort. (Index -> Boolean Mapping) (GAME_RESULT)
   *    9. Server geht wieder zu 5. über, wenn weniger als X Spiele
   *    10. Server schickt Rangliste (GAME_FINISH)
   *
   */

  private final List<Player> players = new CopyOnWriteArrayList<>();
  private final QuizduellServer quizduellServer;
  private final int port;
  private int keepAlivePacketIntervalCounter;
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
        System.out.println("Awaiting connection");
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
        tick();
        // wait 100ms before checking for data again
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }, "net/io-processor").start();
  }

  private void tick() {
    packetFlushTick();
    keepAliveTick();
    logicalTick();
  }

  private void packetFlushTick() {
    for (Player player : players) {
      player.receiveIncomingPackets();

      // check if connection was dropped
      if(!player.isConnected() || player.isTimedOut()) {
        // process connection drop as exit
        removePlayer(player);
        continue;
      }
      try {
        // read input
        String rawData = player.receiveData();
        // check if player has sent something
        if(rawData != null) {
          // forward data
          processRawData(player, rawData);
        }
      } catch (Exception exception) {
        exception.printStackTrace();
        player.closeConnection();
      }
    }
  }

  private void keepAliveTick() {
    if(keepAlivePacketIntervalCounter++ > 10 * 8) {
      keepAlivePacketIntervalCounter = 0;
      for (Player player : players) {
        player.sendData("PING", String.valueOf(System.currentTimeMillis()));
      }
    }
  }

  private void acceptNewConnection(Socket socket) {
    int nextPlayerIdentifier = playerIdCounter.getAndIncrement();
    Player player = new Player(nextPlayerIdentifier, socket);
    player.prepareSocketIO();
    System.out.println(player + " has connected");
    players.add(player);
  }

  private void removePlayer(Player player) {
    player.closeConnection();
    players.remove(player);

    GameService gameService = quizduellServer.gameService();
    if(gameService != null) {
      gameService.processGameExit(player);
    }
  }

  private void logicalTick() {
    GameService gameService = quizduellServer.gameService();
    if(gameService != null) {
      gameService.processTick();
    }
  }

  private void processRawData(Player player, String inputData) {
    String[] inputDataSplit = inputData.split("->");
    String label = inputDataSplit[0]; // the name/label of the sent data
    String data  = inputDataSplit[1]; // the data itself, packed into a string
    // internal data processing
    //System.out.println("Received " + label + " from " + player);
    quizduellServer.gameService().receiveLabeledData(player, label, data);
  }
}
