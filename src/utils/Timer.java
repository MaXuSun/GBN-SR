package utils;

import java.io.IOException;

import server.EchoServer;

public class Timer extends Thread{
  public EchoServer server;
  int time = 0;
  public Timer(EchoServer server,int time) {
    this.server = server;
    this.time = time;
  }
  
  @Override
  public void run() {
    do {
      if(time>0) {
        try {
          Thread.sleep(time*1000);
          server.timeout();
          System.out.println("时间超时，已重新发送……");
        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }while(true);
  }
  
  public void setTime(int time) {
    this.time = time;
  }
}
