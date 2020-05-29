package server.game;

import server.database.DatabaseService;

import java.util.ArrayList;

public final class GameService {
  private final ArrayList<Game> games = new ArrayList<>();

  private final DatabaseService databaseService;
  private QuestionResolver questionResolver;

  public GameService(DatabaseService databaseService) {
    this.databaseService = databaseService;
    this.questionResolver = new QuestionResolver(databaseService);
    this.questionResolver.loadQuestions();
  }


}
