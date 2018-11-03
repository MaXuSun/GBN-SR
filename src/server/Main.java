package server;

import java.io.IOException;

import client.ChannelEchoClient;

public class Main {
  public static void main(String[] args) throws IOException {
    new ChannelEchoServer().service();
    int port = 7000;
    if (args.length > 0)
      port = Integer.parseInt(args[0]);
    final ChannelEchoClient client = new ChannelEchoClient(port);
    Thread receiver = new Thread() {
      public void run() {
        client.receiveFromUser();
      }
    };

    receiver.start();
    client.talk();
  }
}
