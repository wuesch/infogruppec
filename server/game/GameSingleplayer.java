package server.game;

import server.question.Question;
import server.question.QuestionResolver;

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
    // question:answer:answer:answer:answer
    String output = newQuestion.question();
    for (String answer : newQuestion.answers()) {
      output += ":" + answer;
    }
    // send string to client
    player.writeData("GIVE_QUESTION", output);
  }

  @Override
  public void receiveIncomingData(Player player, String dataLabel, String data) {
    if(dataLabel.equals("TRY_ANSWER")) {
      int receivedAnswerIndex = Integer.parseInt(data);
      int correctAnswerIndex = currentQuestion.correctAnswerIndex();
      boolean correctAnswer = receivedAnswerIndex == correctAnswerIndex;
      player.writeData("RESULT", correctAnswerIndex + "::" + correctAnswer);
      loadNewQuestion();
    }
  }
}
