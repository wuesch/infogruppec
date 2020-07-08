package server.game.question;

import server.connect.database.DatabaseService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class QuestionResolver {
  private final DatabaseService databaseAccess;
  private final List<Question> loadedQuestions = new ArrayList<>();
  private int questionCursor;

  public QuestionResolver(DatabaseService databaseAccess) {
    this.databaseAccess = databaseAccess;
  }

  public void loadQuestions() {
    ResultSet resultSet = databaseAccess.search("SELECT * FROM `questions`");
    if(resultSet == null) {
      throw new IllegalStateException();
    }
    try {
      while (resultSet.next()) {
        Question question = readQuestionFrom(resultSet);
        loadedQuestions.add(question);
      }
    } catch (SQLException exception) {
      throw new IllegalStateException("Couldn't read questions from database", exception);
    }
    Collections.shuffle(loadedQuestions);
  }

  private final static int BEGIN_INDEX = 2;

  private Question readQuestionFrom(ResultSet resultSet) throws SQLException {
    String question = resultSet.getString(BEGIN_INDEX);
    String[] answers = new String[4];
    int answersAvailable = answers.length;
    for (int i = 1; i <= answersAvailable; i++) {
      int sqlAnswerIndex = BEGIN_INDEX + i;
      answers[i - 1] = resultSet.getString(sqlAnswerIndex);
    }
    int correctAnswerIndexSQLIndex = BEGIN_INDEX + answersAvailable + 1;
    int correctAnswerIndex = resultSet.getInt(correctAnswerIndexSQLIndex);
    return new Question(question, answers, correctAnswerIndex);
  }

  public synchronized Question resolveQuestion() {
    if(questionCursor >= loadedQuestions.size()) {
      questionCursor = 0;
    }
    return loadedQuestions.get(questionCursor++);
  }
}
