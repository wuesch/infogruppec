package server.game;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

public final class Player {
  private final int identifier;
  private final Socket socket;

  private InputStream inputStream;
  private BufferedReader inputBufferedReader;

  private OutputStream outputStream;
  private PrintStream printStream;

  public Player(int identifier, Socket socket) {
    this.identifier = identifier;
    this.socket = socket;
    try {
      this.inputStream = socket.getInputStream();
      this.inputBufferedReader = new BufferedReader(new InputStreamReader(inputStream));
      this.outputStream = socket.getOutputStream();
      this.printStream = new PrintStream(outputStream, true);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public int identifier() {
    return identifier;
  }

  public void writeData(String label, String data) {
    printStream.println(label + "->" + data);
  }

  public BufferedReader inputBufferedReader() {
    return inputBufferedReader;
  }

  public PrintStream printStream() {
    return printStream;
  }

  public Socket socket() {
    return socket;
  }
}
