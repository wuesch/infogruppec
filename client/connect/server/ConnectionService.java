package client.connect.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public final class ConnectionService {
  private Socket socket;
  private BufferedReader inputBufferedReader;
  private PrintStream outputWriter;

  public void startConnection() {
    try {
      socket = new Socket("127.0.0.1", 8749);
      socket.setTcpNoDelay(true);
      socket.setKeepAlive(true);
      inputBufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      outputWriter        = new PrintStream(socket.getOutputStream(), true);
      System.out.println("Connected successfully");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void writeData(String label, String data) {
    outputWriter.println(label + "->" + data);
  }

  public String readRawData() {
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

  public String requireRawData() {
    try {
      if(socket.isConnected()) {
        return inputBufferedReader.readLine();
      }
      throw new IllegalStateException("Socket closed unexpectedly");
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
}
