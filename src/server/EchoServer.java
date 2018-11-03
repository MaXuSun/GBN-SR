package server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class EchoServer {
  private int port = 8000;
  private DatagramSocket socket;
  
  public EchoServer() throws SocketException {
    socket = new DatagramSocket(port);
    System.out.println("服务器启动");
  }
  
  public String echo(String msg) {
    return "echo:"+msg;
  }
  
  public void service() {
    while(true) {
      try {
        DatagramPacket packet= new DatagramPacket(new byte[512], 512);
        socket.receive(packet);                                                       //接收来自任意一个EchoClient的数据报
        String msg  = new String(packet.getData(),0,packet.getLength());
        System.out.println(packet.getAddress()+":"+packet.getPort()+">"+msg);
        packet.setData(echo(msg).getBytes());                                         //给EchoClient回复一个数据报
        socket.send(packet);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  public static void main(String[] args) throws SocketException {
    new EchoServer().service();
  }
}
