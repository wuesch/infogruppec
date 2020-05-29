package server.game;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public final class Player {
  private final int identifier;
  private final Socket socket;

  private BufferedReader inputBufferedReader;
  private PrintStream outputWriter;

  public Player(int identifier, Socket socket) {
    this.identifier = identifier;
    this.socket = socket;
  }

  public void prepareSocketIO() {
    try {
      socket.setTcpNoDelay(true);
      socket.setKeepAlive(true);
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

  public int identifier() {
    return identifier;
  }

  public void writeData(String label, String data) {
    outputWriter.println(label + "->" + data);
  }

  public String readData() {
    try {
      if(socket.isConnected() && inputBufferedReader.ready()) {
        return inputBufferedReader.readLine();
      } else {
        return null;
      }
    } catch (IOException exception) {
      throw new IllegalStateException(exception);
    }
  }

  public boolean isConnected() {
    return socket.isConnected();
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

  public BufferedReader inputBufferedReader() {
    return inputBufferedReader;
  }

  public PrintStream printStream() {
    return outputWriter;
  }

  public Socket socket() {
    return socket;
  }
}
