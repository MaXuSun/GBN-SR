package server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Locale;

import utils.SendWindow;
import utils.StaticData;
import utils.UDPFrame;
import utils.Utils;

public class ChannelEchoServer {
  private int port = 8000;
  private DatagramChannel channel;
  private final int MAX_SIZE = 1024;
  private boolean inGBN = false;
  private InetSocketAddress cliAddr = null;
  private ByteBuffer buffer;

  private Utils utils = new Utils();

  public ChannelEchoServer() throws IOException {
    channel = DatagramChannel.open();
    DatagramSocket socket = channel.socket();
    SocketAddress localAdd = new InetSocketAddress(port);
    socket.bind(localAdd);
    buffer = ByteBuffer.allocate(MAX_SIZE);

    System.out.println("Open the server……");
  }

  /**
   * 该服务器的主要功能
   */
  public void service() {
    while (true) {
      try {
        
        String msg = this.receive(buffer);
        // 开始判断进入是否正处于GBN状态，如果处于就直接执行GBN模拟

        if (reply(msg, cliAddr)) {
          simuGBN(cliAddr);
        }

      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  public String receive(ByteBuffer buffer) throws IOException {
    buffer.clear();
    
    // 接收来自任意一个client的数据报
    cliAddr = (InetSocketAddress)channel.receive(buffer);
    buffer.flip();
    String msg = Charset.forName(StaticData.CHAR_FORMAT_NAME).decode(buffer).toString();
    System.out.println(cliAddr.getAddress()+":"+cliAddr.getPort()+">"+msg);
    
    return msg;
  }

  /**
   * 处理回复功能 客户端:"-time" --> 向客户端端发送现在的时间,时间的格式化方式存储在StaticData.TIME_FORMAT中
   * 客户端:"-quit" --> 向客户端发送"Good
   * bye!"并断开与该客户端得连接退出;该变量存储在StaticData.SERVER_BYE里面 客户端:"-testgbn" -->
   * 直接return true并 进入GBN协议测试状态 客户端:"-testsr" --> 进入SR协议测试状态 客户端:"其他数据" -->
   * 直接转发该数据给客户端
   * 
   * @param msg
   *          客户端发送的数据
   * @param cliAddress
   *          客户端的Socket地址
   * @throws UnsupportedEncodingException
   * @throws IOException
   */
  public boolean reply(String msg, InetSocketAddress cliAddress)
      throws UnsupportedEncodingException, IOException {
    // 在进行判断时在判断的字符串后面添加一个系统换行符

    if (msg.equals("-time" + System.lineSeparator())) {
      channel.send(
          ByteBuffer.wrap(nowTime().getBytes(StaticData.CHAR_FORMAT_NAME)),
          cliAddress);
    } else if (msg.equals("-quit" + System.lineSeparator())) {

      channel.send(
          ByteBuffer.wrap(
              StaticData.SERVER_BYE.getBytes(StaticData.CHAR_FORMAT_NAME)),
          cliAddress);
    } else if (msg.equals("-testgbn" + System.lineSeparator())) {
      this.inGBN = true;
      return true;
    } else if (msg.equals("-testsr" + System.lineSeparator())) {
      channel.send(ByteBuffer.wrap(msg.getBytes(StaticData.CHAR_FORMAT_NAME)),
          cliAddress);
    } else {
      channel.send(ByteBuffer.wrap(msg.getBytes(StaticData.CHAR_FORMAT_NAME)),
          cliAddress);
    }

    return false;

  }

  public void simuGBN(InetSocketAddress cliAddr) throws IOException {
    UDPFrame[] frames = utils.geneFrame((byte) 100);
    SendWindow window = new SendWindow(50);
    ByteBuffer tempBuffer = ByteBuffer.allocate(MAX_SIZE*2);
    byte ack = 0;
   
    for (byte i = 0; i < frames.length; i++) {
      tempBuffer.clear();
      tempBuffer = ByteBuffer.wrap(frames[i].getAllData());
      
      System.err.println(frames[i].getSeq()+","+frames[i].getStrData());
      
      channel.send(tempBuffer, cliAddr);
      
      System.err.println("发送成功");
      
      String get = receive(buffer);
      ack = get.getBytes(StaticData.CHAR_FORMAT_NAME)[0];
      System.out.println(ack);
      
      if(ack == frames[i].getSeq()) {
        window.slipN(1);
      }
    }
    for(byte i = 0;i < frames.length;i++) {
      while (true) {
        
      }
    }

  }

  /**
   * 返回现在的时间
   * 
   * @return
   */
  public String nowTime() {
    SimpleDateFormat format = new SimpleDateFormat(StaticData.TIME_FORMAT,
        Locale.ENGLISH);
    return format.format(System.currentTimeMillis()) + System.lineSeparator();
  }

  public static void main(String[] args) throws IOException {
    new ChannelEchoServer().service();
    // System.out.println(Byte.MAX_VALUE);
  }
}
