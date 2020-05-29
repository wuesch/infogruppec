package server.game;

import server.database.DatabaseService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public final class QuestionResolver {
  private final DatabaseService databaseAccess;
  private ArrayList<Question> loadedQuestions = new ArrayList<>();
  private int cursor;

  public QuestionResolver(DatabaseService databaseAccess) {
    this.databaseAccess = databaseAccess;
  }

  public void loadQuestions() {
    ResultSet result = databaseAccess.search("SELECT * FROM `questions`");
    if(result == null) {
      throw new IllegalStateException();
    }
    try {
      while (result.next()) {
        String question = result.getString(1);
        String[] answers = new String[4];
        for (int i = 0; i < answers.length; i++) {
          answers[i] = result.getString(2 + i);
        }
        int correctAnswerIndex = result.getInt(3 + answers.length);
        Question questionObj = new Question(question, answers, correctAnswerIndex);
        loadedQuestions.add(questionObj);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public Question resolveQuestion() {
    if(cursor >= loadedQuestions.size()) {
      cursor = 0;
    }
    return loadedQuestions.get(cursor++);
  }
}
