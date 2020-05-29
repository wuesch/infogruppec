package server.game;

import server.connect.database.DatabaseService;
import server.game.question.QuestionResolver;

import java.util.ArrayList;
import java.util.List;

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
      throw new UnsupportedOperationException("Client requested game of invalid type: " + gamemode);
    }
  }

  public void openSingleplayerGame(Player player) {
    GameSingleplayer singleplayerGame;
    singleplayerGame = new GameSingleplayer(player, questionResolver);
    singleplayerGame.loadNewQuestion();
    activeGames.add(singleplayerGame);
  }

  public void processGameExit(Player player) {
    processQuit(player);
    player.closeConnection();
  }

  private void processGameCommand(Player player, String label, String data) {
    // get active game
    Game activeGame = gameOf(player);
    if(activeGame != null) {
      // forward data to game
      activeGame.receiveIncomingData(player, label, data);
    } else {
      throw new IllegalStateException("Client sent unknown data out of game");
    }
  }

  public void processQuit(Player player) {
    Game playerGame = gameOf(player);
    if(playerGame != null) {
      boolean gameWillBeEmpty = playerGame.players().size() <= 1;
      if(gameWillBeEmpty) {
        activeGames.remove(playerGame);
      } else {
        playerGame.players().remove(player);
        playerGame.notifyUpdate();
      }
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
