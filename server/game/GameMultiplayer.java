package server.game;

import server.game.question.Question;
import server.game.question.QuestionResolver;

import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class GameMultiplayer extends Game {
  public static final int REQUIRED_PLAYERS = 1;

  private final static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
  private final QuestionResolver questionResolver;
  private MultiplayerState state;
  private int rounds = 8;
  private int lobbyTimer = 10;
  private int gameTimer = 30;
  private int ticks;

  public GameMultiplayer(Player initialPlayer, QuestionResolver questionResolver) {
    super(new LinkedList<>(Collections.singletonList(initialPlayer)));
    this.questionResolver = questionResolver;
    state = MultiplayerState.LOBBY;
  }

  public void playerJoin(Player player) {
    if(!canBeJoined()) {
      throw new IllegalStateException("Player joined running game");
    }
    if(hasPlayer(player)) {
      return;
    }
    System.out.println(player + " joined game " + this);
    players().add(player);
  }

  @Override
  public void tick() {
    if(canBeJoined()) {
      if(startRequirementsMet()) {
        state = MultiplayerState.LOBBY_PREPARE;
        awaitSynchronizationAndStartGame();
      }
    } else if(waitingForAnswers()) {
      if(ticks % 10 == 0) {
        broadcastGameInformation();
        boolean everybodyGaveAnswer = players().stream().allMatch(Player::givenAnswer);
        if(gameTimer-- <= 0 || everybodyGaveAnswer) {
          // game finished
          state = MultiplayerState.REVEAL;
          int correctAnswerIndex = currentQuestion().correctAnswerIndex();

          StringBuilder output = new StringBuilder();
          output.append(correctAnswerIndex);
          output.append("@");
          for (Player player : players()) {
            boolean wasCorrect = correctAnswerIndex == player.givenAnswerIndex();
            output.append(wasCorrect ? "1" : "0");
            output.append(";");
          }
          output.deleteCharAt(output.length());

          for (Player player : players()) {
            player.sendData("GAME_RESULT", output.toString());
          }

          executor.schedule(() -> {
            if(rounds-- > 0) {
              // new game
              awaitSynchronizationAndStartGame();
            } else {
              // finish
            }
          }, 3, TimeUnit.SECONDS);
        }
      }
    }
    ticks++;
  }

  @Override
  public void receiveIncomingData(Player player, String dataLabel, String data) {
    player.setAwaitingResponse(false);
    switch (dataLabel) {
      case "LOBBY_ENTER":
        //noinspection UnnecessaryLocalVariable
        String playerName = data;
        player.setCustomName(playerName);
      case "LOBBY_KEEP_ALIVE":
        System.out.println(player.customName() + " " + data + " " + player.ready());
        boolean ready = data.equals("1");
        player.setReady(ready);

        if(canBeJoined()) {
          sendLobbyInfo(player);
          player.setAwaitingResponse(true);
        }
        break;

      case "GAME_ANSWER":
        int answerIndex = Integer.parseInt(data);
        player.setGivenAnswer(true);
        player.setGivenAnswerIndex(answerIndex);
        break;

    }
  }

  private boolean startRequirementsMet() {
    if(players().size() >= REQUIRED_PLAYERS) {
      return players().stream().allMatch(Player::ready);
    } else {
      return false;
    }
  }

  private void sendLobbyInfo(Player player) {
    executor.schedule(() -> {
      StringBuilder dataToSend = new StringBuilder();
      for (Player player1 : players()) {
        String name = player1.customName() == null ? "Unknown" : player1.customName();
        dataToSend.append(name).append(";");
      }
      dataToSend.append("@");
      for (Player player1 : players()) {
        dataToSend.append(player1.ready() ? "1" : "0").append(";");
      }
      dataToSend.append("@");
      dataToSend.append(lobbyTimer);
      String output = dataToSend.toString();
      output = output.replace(";@", "@");
      player.sendData("LOBBY_INFO", output);
    }, 1, TimeUnit.SECONDS);
  }

  private void awaitSynchronizationAndStartGame() {
    boolean allReady = players().stream().noneMatch(Player::awaitingResponse);
    if(allReady) {
      try {
        beginGame();
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      executor.schedule(this::awaitSynchronizationAndStartGame, 1, TimeUnit.SECONDS);
    }
  }

  private void beginGame() {
    Question question = questionResolver.resolveQuestion();
    setCurrentQuestion(question);
    // write question to string
    // question*^*answer*^*answer*^*answer*^*answer
    StringBuilder output = new StringBuilder(question.question());
    for (String answer : question.answers()) {
      output.append("*^*").append(answer);
    }

    for (Player player : players()) {
      StringBuilder dataToSend = new StringBuilder();
      for (Player player1 : players()) {
        dataToSend.append(player1.customName()).append(";");
      }
      dataToSend.deleteCharAt(dataToSend.length() - 1);
      player.sendData("GAME_PREPARE", dataToSend.toString() + "@" + output);
    }

    state = MultiplayerState.WAITING_FOR_ANSWERS;
    gameTimer = 30;
  }

  private void broadcastGameInformation() {
    StringBuilder dataToSend = new StringBuilder();
    for (Player player : players()) {
      dataToSend.append(player.givenAnswer() ? "1" : "0").append(";");
    }
    dataToSend.append("@");
    dataToSend.append(gameTimer);
    String data = dataToSend.toString();
    data = data.replace(";@", "@");
    for (Player player : players()) {
      player.sendData("GAME_STATUS", data);
    }

  }

  public boolean canBeJoined() {
    return state == MultiplayerState.LOBBY;
  }

  public boolean waitingForAnswers() {
    return state == MultiplayerState.WAITING_FOR_ANSWERS;
  }
}
