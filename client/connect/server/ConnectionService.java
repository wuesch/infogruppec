package client.connect.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class ConnectionService {
  private Socket socket;
  private BufferedReader inputBufferedReader;
  private PrintStream outputWriter;

  private BlockingQueue<String> incomingPackets = new ArrayBlockingQueue<>(50);

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

  public void startSocketReaderThread() {
    new Thread(() -> {
      Thread thread = Thread.currentThread();
      while (!thread.isInterrupted()) {
        try {
          while (socket.isConnected() && inputBufferedReader.ready()) {
            String packetData = inputBufferedReader.readLine();
            if(Serialization.labelFromPacket(packetData).equals("PING")) {
              receivePingPacket(packetData);
              break;
            }
            incomingPackets.add(packetData);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }, "SocketReader").start();
  }

  private void receivePingPacket(String packet) {
    String packetData = Serialization.dataFromPacket(packet);
    writeData("PONG", packetData);
  }

  public void writeData(String label, String data) {
    outputWriter.println(label + "->" + data);
  }

  public String requireRawData() {
    try {
      return incomingPackets.take();
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
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
