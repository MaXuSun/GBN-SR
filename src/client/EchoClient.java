package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import utils.StaticData;
import utils.UDPFrame;

public class EchoClient {
  private String remoteHost = "localhost";
  private int remotePort = 8000;
  private DatagramSocket socket;
  private InetAddress remoteIP;
  private byte expect = 0;
  public EchoClient() throws SocketException, UnknownHostException {
    socket = new DatagramSocket();
    remoteIP  = InetAddress.getByName(remoteHost);
  }
  public void talk() {
    try {
      

      BufferedReader localReader = new BufferedReader(
          new InputStreamReader(System.in));
      String msg = null;
      while ((msg = localReader.readLine()) != null) {

        byte[] outputData = msg.getBytes();
        DatagramPacket outputPacket = new DatagramPacket(outputData,
            outputData.length, remoteIP, remotePort);
        socket.send(outputPacket); // 给EchoServer发送数据

        if (msg.equals("-testgbn")) {
          testgbn();
        }

        DatagramPacket inputPacket = new DatagramPacket(new byte[1471], 1471);
        socket.receive(inputPacket); // 接收EchoServer的数据报

        receive(inputPacket);

      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      socket.close();
    }
  }

  public void receive(DatagramPacket packet) {
    String msg = new String(packet.getData(), 0, packet.getLength());
    if (msg.equals(StaticData.SERVER_BYE)) {
      System.out.println("退出");
      System.exit(0);
    }
    System.out.println(msg);
  }

  public void testgbn() throws IOException {    
    while (true) {

      DatagramPacket inputPacket = new DatagramPacket(new byte[1471], 1471);
      
      socket.receive(inputPacket);
      
      UDPFrame frame = new UDPFrame();
      frame.setAllData(inputPacket.getData());
      System.out.println(frame.getSeq()+","+frame.getStrData());
      if(expect == frame.getSeq()) {
        sendAck(frame.getSeq());
        System.out.println("收到分组:"+frame.getSeq()+"内容为:"+frame.getStrData()+",该分组为期待的分组");
        expect++;
      }else {
        System.err.println("收到分组:"+frame.getSeq()+"内容为:"+frame.getStrData()+",该分组不是期待分组:"+expect);
        sendAck(expect-1);
      }  
    }
  }
  
  public void sendAck(int i)throws IOException{
    byte[] response = {(byte)i};
    DatagramPacket packet = new DatagramPacket(response, response.length,remoteIP,remotePort);
    socket.send(packet);
  }
  
  public static void main(String[] args) throws SocketException, UnknownHostException {
    new EchoClient().talk();
  }
}
