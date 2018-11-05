package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

import utils.ACK;
import utils.StaticData;
import utils.UDPFrame;
import utils.Utils;

public class ChannelEchoClient {
  private DatagramChannel channel = null; // 定义的一个channel,相当于一个Socket的包装
  private ByteBuffer sendBuffer = ByteBuffer.allocate(1024 * 2); // 发送消息的buffer
  private ByteBuffer receiveBuffer = ByteBuffer.allocate(1024 * 2); // 接收消息的buffer
  private Charset charset = Charset.forName("utf-8"); // 字符集编码,用来将接收的数据进行编解码
  private Selector selector;
  private boolean inGBN = false;
  Utils utils = new Utils();

  public ChannelEchoClient() throws IOException {
    this(7000);
  }

  public ChannelEchoClient(int port) throws IOException {
    channel = DatagramChannel.open();
    InetAddress ia = InetAddress.getLocalHost();
    InetSocketAddress isa = new InetSocketAddress(ia, port);
    channel.configureBlocking(false); // 设置为非阻塞模式
    channel.socket().bind(isa); // 与本地地址绑定
    isa = new InetSocketAddress(ia, 8000);
    channel.connect(isa); // 与远程地址连接
    selector = Selector.open();
  }

  public static void main(String[] args) throws IOException {
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

  public void receiveFromUser() { // 读取用户从键盘的输入
    try {
      BufferedReader localReader = new BufferedReader(
          new InputStreamReader(System.in));
      String msg = null;
      while ((msg = localReader.readLine()) != null) {
        synchronized (sendBuffer) {
          sendBuffer.put(encode(msg + System.lineSeparator()));
        }

        if (msg.equals("bye")) {
          break;
        } else if (msg.equals("-testgbn")) { // 如果用户输入了testGBN则客户端也开启GBN模式
          this.inGBN = true;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void talk() throws IOException {
    // channel向selector进行注册，注册事件为读和写事件，然后进行轮询，
    // 当哪个事件好了就进行相应事件处理
    channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

    // 对事件进行轮询处理
    while (selector.select() > 0) {
      Set<SelectionKey> readyKeys = selector.selectedKeys();
      Iterator<SelectionKey> it = readyKeys.iterator();
      while (it.hasNext()) {
        SelectionKey key = null;
        try {
          key = it.next();
          it.remove();

          if (key.isReadable()) {
            receive(key);
          }
          if (key.isWritable()) {
            send(key);
          }
        } catch (Exception e) {
          e.printStackTrace();
          try {
            if (key != null) {
              key.cancel();
              key.channel().close();
            }
          } catch (Exception e2) {
            e2.printStackTrace();
          }
        }
      }
    }
  }

  public void send(SelectionKey key) throws IOException {
    DatagramChannel datagramChannel = (DatagramChannel) key.channel();
    synchronized (sendBuffer) {
      sendBuffer.flip();
      datagramChannel.write(sendBuffer);
      sendBuffer.compact();
    }
  }

  public void receive(SelectionKey key) throws IOException {
    DatagramChannel datagramChannel = (DatagramChannel) key.channel();
    receiveBuffer.clear();
    datagramChannel.read(receiveBuffer);
    receiveBuffer.flip();
    
    
    if(this.inGBN) {
      UDPFrame frame = new UDPFrame();
      if(receiveBuffer.remaining() >1) {
        receiveBuffer.get(frame.getAllData());
        System.out.println(frame.getSeq() + "," + frame.getStrData());
      }
      //ACK ack = new ACK(frame.getSeq());
      
      if (this.inGBN) {
        synchronized (sendBuffer) {
          sendBuffer.put(frame.getSeq());
        }
      }
    }else {
      String receiveData = utils.decode(receiveBuffer);
      System.out.println(receiveData);

      // 收到来自服务的Good bye!就断开与其的连接
      if (receiveData.equals(StaticData.SERVER_BYE)) {
        key.cancel();
        datagramChannel.close();
        System.out.println("Disconnect from the server");
        selector.close();
        System.exit(0);
      }
    }
    
    // ByteBuffer tempBuffer = encode(receiveData);
    // receiveBuffer.position(tempBuffer.limit());
    // receiveBuffer.compact();
  }
  
  public String decode(ByteBuffer buffer) { // 解码
    CharBuffer charBuffer = charset.decode(buffer);
    return charBuffer.toString();
  }

  public ByteBuffer encode(String str) { // 编码
    return charset.encode(str);
  }
}
