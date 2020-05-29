package client.net;

import java.io.IOException;
import java.net.Socket;

public final class TestServerConnection {

  private Socket socket;

  public void start() {
    try {
      System.out.println("test");
      socket = new Socket("127.0.0.1", 1346);
      System.out.println(socket.isConnected());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
