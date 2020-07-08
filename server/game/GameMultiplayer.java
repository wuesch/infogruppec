package server.game;

import server.game.question.Question;
import server.game.question.QuestionResolver;

import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class GameMultiplayer extends Game {
  public static final int REQUIRED_PLAYERS = 2;

  private final static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
  private final QuestionResolver questionResolver;
  private MultiplayerState state;
  private int rounds = 6;
  private int lobbyTimer = 10;
  private int gameTimer = 30;
  private int ticks;

  public GameMultiplayer(Player initialPlayer, QuestionResolver questionResolver) {
    super(new LinkedList<>(Collections.singletonList(initialPlayer)));
    this.questionResolver = questionResolver;
    this.state = MultiplayerState.LOBBY;
  }

  public void playerJoin(Player player) {
    if(!canBeJoined()) {
      throw new IllegalStateException("Player joined running game");
    }
    if(hasPlayer(player)) {
      return;
    }
    System.out.println(player + " joined game " + this);
    player.reset();
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
      if(ticks % 20 == 0) {
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
          output.deleteCharAt(output.length() - 1);
          for (Player player : players()) {
            if(player.givenAnswerIndex() == correctAnswerIndex) {
              player.setCorrectAnswers(player.correctAnswers() + 1);
            }
            player.sendData("GAME_RESULT", output.toString());
          }
          executor.schedule(() -> {
            if(rounds > 0) {
              // new game
              awaitSynchronizationAndStartGame();
            } else {
              // finish
              finishAndEndGame();
            }
          }, 4, TimeUnit.SECONDS);
        }
      }
    }
    ticks++;
  }

  private void finishAndEndGame() {
    StringBuilder stuff = new StringBuilder();
    for (Player player : players()) {
      stuff.append(player.customName());
      stuff.append(";");
    }
    stuff.append("@");
    for (Player player : players()) {
      stuff.append(player.correctAnswers());
      stuff.append(";");
    }
    String stuffAsString = stuff.toString();
    stuffAsString = stuffAsString.replace(";@", "@");
    for (Player player : players()) {
      player.sendData("GAME_FINISH", stuffAsString);
      player.reset();
    }

    // stop the game
    exit();
  }

  @Override
  public void receiveIncomingData(Player player, String dataLabel, String data) {
    player.setAwaitingResponse(false);
    switch (dataLabel) {
      case "LOBBY_ENTER":
        String playerName = data;
        playerName = stripEverythingElseThanChars(playerName);
        playerName = playerName.replaceAll(" ", "").trim();
        playerName = playerName.length() > 16 ? playerName.substring(0, 15) : playerName;
        playerName = uniqueName(playerName);
        playerName = playerName.replaceAll(" ", "").trim();
        if(!playerName.isEmpty()) {
          player.setCustomName(playerName);
        } else {
          break;
        }
      case "LOBBY_KEEP_ALIVE":
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

  private String uniqueName(String playerName) {
    int search = 0;
    while (true) {
      boolean nameExists = false;
      for (Player otherPlayer : players()) {
        if (
          otherPlayer.customName() != null &&
          otherPlayer.customName().equals(playerName + (search > 0 ? String.valueOf(search) : ""))
        ) {
          nameExists = true;
          break;
        }
      }
      if(nameExists) {
        search++;
      } else {
        break;
      }
    }
    if(search > 0) {
      playerName += search;
    }
    return playerName;
  }

  private String stripEverythingElseThanChars(String input) {
    char[] chars = new char[input.length()];
    int cursor = 0;
    for (char c : input.toCharArray()) {
      if(Character.isAlphabetic(c)) {
        chars[cursor++] = c;
      }
    }
    System.arraycopy(chars, 0, chars, 0, cursor);
    return new String(chars);
  }

  private boolean startRequirementsMet() {
    return players().size() >= REQUIRED_PLAYERS && players().stream().allMatch(Player::ready);
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
    boolean noResponcePending = players().stream().noneMatch(Player::awaitingResponse);
    if(noResponcePending) {
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
      player.setGivenAnswer(false);
      player.setGivenAnswerIndex(-1);
    }

    state = MultiplayerState.WAITING_FOR_ANSWERS;
    gameTimer = 30;
    rounds--;
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
