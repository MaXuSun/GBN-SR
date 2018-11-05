package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import utils.SendWindow;
import utils.StaticData;
import utils.Timer;
import utils.UDPFrame;
import utils.Utils;

public class EchoServer {
  private int port = 8000;
  private DatagramSocket socket;
  private int inGBN = 0;
  private Utils utils = new Utils();
  private Timer timer;
  int time = 3;
  int num = StaticData.num;
  int wsize = 10;
  private SendWindow window;
  private UDPFrame[] frames;
  private DatagramPacket packet;

  public EchoServer() throws SocketException {
    socket = new DatagramSocket(port);
    System.out.println("服务器启动……");
    this.timer = new Timer(this, time);
  }

  public String echo(String msg) {
    return "echo:" + msg;
  }

  public void service() {
    while (true) {
      try {
        DatagramPacket packet = new DatagramPacket(new byte[1471], 1471);
        this.packet = packet;
        socket.receive(packet); // 接收来自任意一个EchoClient的数据报
        if (inGBN == 0) {
          receive();
        }
        if (inGBN == 1) {
          ingbn();
        } else if (inGBN == 2) {
          insr();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void receive() throws IOException {
    String msg = new String(packet.getData(), 0, packet.getLength());
    if (msg.equals("-time")) {
      packet.setData(nowTime().getBytes());
      socket.send(packet);
    } else if (msg.equals("-quit")) {
      packet.setData(StaticData.SERVER_BYE.getBytes());
      socket.send(packet);
    } else if (msg.equals("-testgbn")) {
      this.inGBN = 1;
    } else if (msg.equals("-testsr")) {
      this.inGBN = 2;
    } else {
      packet.setData(msg.getBytes());
      socket.send(packet);
    }

  }

  public void ingbn() throws IOException {
    this.frames = utils.geneFrame((byte) num);
    this.window = new SendWindow(wsize);

    timer.start();
    while (true) {
      if (window.getNextseqnum() + window.getBase() != num) {
        sendData();
      }
      DatagramPacket packet = new DatagramPacket(new byte[1471], 1471);
      socket.receive(packet);
      String msg = new String(packet.getData());
      byte b = msg.getBytes()[0];
      if (b == window.getBase()) {
        window.slipN(1);
        System.out.println("接收到的ack序号:" + b);
        System.out.println("滑动后,base:" + window.getBase() + ",nextseqnum:"
            + window.getNextseqnum());
        timer.setTime(0);
      } else {
        timer.setTime(time);
      }
      if (b == num) {
        timer.interrupt();
        break;
      }
    }
  }

  public void insr() throws IOException {
    this.frames = utils.geneFrame((byte) num);
    this.window = new SendWindow(wsize);

    timer.start();
    while (true) {
      if (window.getNextseqnum() + window.getBase() != num) {
        sendData();
      }
      DatagramPacket packet = new DatagramPacket(new byte[1471], 1471);
      socket.receive(packet);
      String msg = new String(packet.getData());
      byte b = msg.getBytes()[0];
      
      window.setAckBySeq(b);
      if(window.canSlip()) {
        window.slip();
        System.out.println("滑动后,base:" + window.getBase() + ",nextseqnum:"
            + window.getNextseqnum());
        timer.setTime(time);
      }
      
      if (b == num) {
        timer.interrupt();
        break;
      }
    }
  }

  public void sendData() throws IOException {
    for (int i = window.getNextseqnum(); i < window.getWsize()
        && i < num; i++) {
      if (i % 3 == 1) {
        window.setNextseqnum(window.getNextseqnum() + 1);
        System.out.println("首次发送,模拟第" + (i + window.getBase() + "个数据丢失:"
            + frames[i + window.getBase()].getStrData()));
        continue;
      }
      window.setNextseqnum(window.getNextseqnum() + 1);
      packet.setData(frames[i + window.getBase()].getAllData());
      socket.send(packet);

      System.out.println("首次发送，第" + (i + window.getBase()) + "个数据已经发送:"
          + frames[i + window.getBase()].getStrData());
    }
    timer.setTime(time);
  }

  /**
   * 返回现在的时间
   * 
   * @return
   */
  public String nowTime() {
    SimpleDateFormat format = new SimpleDateFormat(StaticData.TIME_FORMAT,
        Locale.ENGLISH);
    return format.format(System.currentTimeMillis());
  }

  public void timeout() throws IOException {
    for (int i = 0; i < window.getNextseqnum(); i++) {
      if(inGBN == 2) {
        if(window.getAckOfn(i) == 1) {
          continue;
        }
      }
      packet.setData(frames[i + window.getBase()].getAllData());
      socket.send(packet);
      System.out.println("重发，第" + (window.getBase() + i) + "个数据已经重发");
    }
    timer.setTime(time);
  }

  public static void main(String[] args) throws SocketException {
    new EchoServer().service();
  }
}
