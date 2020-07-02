package server.game;

import server.game.question.Question;
import server.game.question.QuestionResolver;

import java.util.Collections;

public final class GameSingleplayer extends Game {
  private final Player player;
  private final QuestionResolver questionResolver;

  public GameSingleplayer(Player player, QuestionResolver questionResolver) {
    super(Collections.singletonList(player));
    this.player = player;
    this.questionResolver = questionResolver;
  }

  public void loadNewQuestion() {
    // resolve new question
    Question newQuestion = questionResolver.resolveQuestion();
    setCurrentQuestion(newQuestion);
    // write question to string
    // question*^*answer*^*answer*^*answer*^*answer
    StringBuilder output = new StringBuilder(newQuestion.question());
    for (String answer : newQuestion.answers()) {
      output.append("*^*").append(answer);
    }
    // send string to client
    player.sendData("GIVE_QUESTION", output.toString());
  }

  @Override
  public void receiveIncomingData(Player player, String dataLabel, String data) {
    if(dataLabel.equals("TRY_ANSWER")) {
      int receivedAnswerIndex = Integer.parseInt(data);
      int correctAnswerIndex = currentQuestion.correctAnswerIndex();
      boolean correctAnswer = receivedAnswerIndex == correctAnswerIndex;
      player.sendData("RESULT", correctAnswerIndex + "::" + correctAnswer);
      loadNewQuestion();
    }
  }
}