package client.connect.server;

public class Serialization {

  public static String labelFromPacket(String packet) {
    return packetEntity(packet)[0];
  }

  public static String dataFromPacket(String packet) {
    return packetEntity(packet)[1];
  }

  private static String[] packetEntity(String packet) {
    return packet.split("->");
  }

}
