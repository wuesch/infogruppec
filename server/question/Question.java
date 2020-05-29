package server.question;

import java.util.Arrays;

public final class Question {
  private final String question;
  private final String[] answers;
  private final int correctAnswerIndex;

  public Question(String question, String[] answers, int correctAnswerIndex) {
    this.question = question;
    this.answers = answers;
    this.correctAnswerIndex = correctAnswerIndex;
  }

  public String question() {
    return question;
  }

  public String[] answers() {
    return answers;
  }

  public String correctAnswer() {
    return answers[correctAnswerIndex];
  }

  public int correctAnswerIndex() {
    return correctAnswerIndex;
  }
}
