package server.game;

import server.connect.database.DatabaseService;
import server.game.question.QuestionResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class GameService {
  private final List<Game> activeGames = new ArrayList<>();
  private final QuestionResolver questionResolver;

  public GameService(DatabaseService databaseService) {
    this.questionResolver = new QuestionResolver(databaseService);
    this.questionResolver.loadQuestions();
  }

  public void receiveLabeledData(Player player, String label, String data) {
    switch (label) {
      case "ENTER_GAME":
        processGameEnter(player, data);
        break;
      case "EXIT_GAME":
        processGameExit(player);
        break;
      default:
        processGameCommand(player, label, data);
        break;
    }
  }

  private void processGameEnter(Player player, String gamemode) {
    if(gamemode.equalsIgnoreCase("SINGLEPLAYER")) {
      openSingleplayerGame(player);
    } else {
      openMultiplayerGame(player);
    }
  }

  public void openSingleplayerGame(Player player) {
    GameSingleplayer singleplayerGame;
    singleplayerGame = new GameSingleplayer(player, questionResolver);
    singleplayerGame.loadNewQuestion();
    activeGames.add(singleplayerGame);
  }

  public void openMultiplayerGame(Player player) {
    Optional<GameMultiplayer> gameInLobby = searchMultiplayerGameInLobby();
    GameMultiplayer multiplayerGame = gameInLobby.orElseGet(() -> new GameMultiplayer(player, questionResolver));
    multiplayerGame.playerJoin(player);
    activeGames.add(multiplayerGame);
  }

  private Optional<GameMultiplayer> searchMultiplayerGameInLobby() {
    for (Game activeGame : activeGames) {
      if(activeGame instanceof GameMultiplayer) {
        GameMultiplayer activeMultiplayerGame = (GameMultiplayer) activeGame;
        if(activeMultiplayerGame.canBeJoined()) {
          return Optional.of(activeMultiplayerGame);
        }
      }
    }
    return Optional.empty();
  }

  public void processGameExit(Player player) {
    processQuit(player);
    //player.closeConnection();
  }

  private void processGameCommand(Player player, String label, String data) {
    // get active game
    Game activeGame = gameOf(player);
    if(activeGame != null) {
      try {
        // forward data to game
        activeGame.receiveIncomingData(player, label, data);
      } catch (Exception exception) {
        exception.printStackTrace();
        player.closeConnection();
        System.out.println("Player " + player + " sent invalid packet: " + exception.getMessage());
      }
    } else {
      player.closeConnection();
      System.out.println("Player " + player + " sent packet out of game");
    }
  }

  public void killGame(Game game) {
    game.players().clear();
    activeGames.remove(game);
  }

  public void processQuit(Player player) {
    Game playerGame = gameOf(player);
    if(playerGame != null) {
      boolean gameWillBeEmpty = playerGame.players().size() <= 1;
      if(gameWillBeEmpty) {
        activeGames.remove(playerGame);
      } else {
        playerGame.players().remove(player);
      }
    }
  }

  public void processTick() {
    for (Game activeGame : activeGames) {
      activeGame.tick();
    }
  }

  public Game gameOf(Player player) {
    for (Game game : activeGames) {
      if(game.hasPlayer(player)) {
        return game;
      }
    }
    return null;
  }
}
