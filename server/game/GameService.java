package server.game;

import server.database.DatabaseService;
import server.question.QuestionResolver;

import java.util.ArrayList;
import java.util.List;

public final class GameService {
  private final List<Game> games = new ArrayList<>();

  private final DatabaseService databaseService;
  private QuestionResolver questionResolver;

  public GameService(DatabaseService databaseService) {
    this.databaseService = databaseService;
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
    GameSingleplayer gameSingleplayer = new GameSingleplayer(player, questionResolver);
    games.add(gameSingleplayer);
    gameSingleplayer.loadNewQuestion();
  }

  public void processGameExit(Player player) {
    processQuit(player);
    player.closeConnection();
  }

  private void processGameCommand(Player player, String label, String data) {
    // label is something els
    // get active game
    Game activeGame = gameOf(player);
    if(activeGame != null) {
      // push data to game
      activeGame.receiveIncomingData(player, label, data);
    } else {
      throw new IllegalStateException("Client sent unknown data out of game");
    }
  }

  public void processQuit(Player player) {
    Game playerGame = gameOf(player);
    if(playerGame != null) {
      boolean gameWillBeEmpty = playerGame.players.size() <= 1;
      if(gameWillBeEmpty) {
        games.remove(playerGame);
      } else {
        playerGame.players.remove(player);
        playerGame.notifyUpdate();
      }
    }
  }

  public Game gameOf(Player player) {
    for (Game game : games) {
      if(game.hasPlayer(player)) {
        return game;
      }
    }
    return null;
  }
}
