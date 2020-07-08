package server.game;


import server.connect.client.Serialization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class Player {
  private final int identifier;
  private final Socket socket;

  private BufferedReader inputBufferedReader;
  private PrintStream outputWriter;

  private BlockingQueue<String> packetQueue = new ArrayBlockingQueue<>(50);

  private String customName;
  private boolean isReady;
  private boolean awaitingResponse;
  private boolean givenAnswer;
  private int givenAnswerIndex;
  private int correctAnswers = 0;
  private long lastTimeAnswered = System.currentTimeMillis();


  public Player(int identifier, Socket socket) {
    this.identifier = identifier;
    this.socket = socket;
  }

  public void prepareSocketIO() {
    try {
      socket.setTcpNoDelay(true);
//      socket.setKeepAlive(true);
    } catch (SocketException e) {
      e.printStackTrace();
    }
    try {
      this.inputBufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.outputWriter = new PrintStream(socket.getOutputStream(), true);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void receiveIncomingPackets() {
    try {
      while (inputBufferedReader.ready()) {
        String packet = inputBufferedReader.readLine();
        receiveIncomingPacket(packet);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void receiveIncomingPacket(String packet) {
    if(!Serialization.labelFromPacket(packet).equals("PONG")) {
      receiveGamePacket(packet);
    }
    lastTimeAnswered = System.currentTimeMillis();
  }

  private void receiveGamePacket(String gamePacket) {
    packetQueue.add(gamePacket);
  }

  public int identifier() {
    return identifier;
  }

  public void sendData(String label, String data) {
    outputWriter.println(label + "->" + data);
    //System.out.println("Sent " + label + " to " + this);
  }

  public String receiveData() {
    return packetQueue.poll();
  }

  public boolean isConnected() {
    return socket.isConnected();
  }

  private final static int KEEP_ALIVE_TIME = 20 * 1000;

  public boolean isTimedOut() {
    long difference = System.currentTimeMillis() - lastTimeAnswered;
    return difference > KEEP_ALIVE_TIME;
  }

  public void closeConnection() {
    if(isConnected()) {
      try {
        socket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public String customName() {
    return customName;
  }

  public void setCustomName(String customName) {
    this.customName = customName;
  }

  public boolean ready() {
    return isReady;
  }

  public void setReady(boolean ready) {
    isReady = ready;
  }

  public boolean awaitingResponse() {
    return awaitingResponse;
  }

  public void setAwaitingResponse(boolean awaitingResponse) {
    this.awaitingResponse = awaitingResponse;
  }

  public boolean givenAnswer() {
    return givenAnswer;
  }

  public void setGivenAnswer(boolean givenAnswer) {
    this.givenAnswer = givenAnswer;
  }

  public int givenAnswerIndex() {
    return givenAnswerIndex;
  }

  public void setGivenAnswerIndex(int givenAnswerIndex) {
    this.givenAnswerIndex = givenAnswerIndex;
  }

  public int correctAnswers() {
    return correctAnswers;
  }

  public void setCorrectAnswers(int correctAnswers) {
    this.correctAnswers = correctAnswers;
  }

  public void reset() {
    setAwaitingResponse(false);
    setCorrectAnswers(0);
    setCustomName(null);
    setGivenAnswer(false);
    setGivenAnswerIndex(-1);
    setReady(false);
  }

  public BufferedReader inputBufferedReader() {
    return inputBufferedReader;
  }

  public PrintStream printStream() {
    return outputWriter;
  }

  public Socket socket() {
    return socket;
  }

  @Override
  public String toString() {
    return "Player{" +
      "identifier=" + identifier +
      ", socket=" + socket +
      ", customName='" + customName + '\'' +
      '}';
  }
}
