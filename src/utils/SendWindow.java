package utils;

import com.sun.org.apache.regexp.internal.recompile;
import com.sun.xml.internal.bind.util.Which;

/**
 * 该类为发送窗口的类，用来模拟在使用GBN或者SR协议时发送方维护的窗口
 * 其中由一个byte[]数组用来记录数据，一个Wsize用来记录窗口的大小，一个base用来标注等待Ack确认的最小序号
 * 一个nextseqnum用来记录窗口中还未发送的最小序号在window窗口中的位置，即索引，而不是像base一样为序列号
 * 
 * @author MaXU
 *
 */
public class SendWindow {
  int seq = StaticData.MAX_SEQ;

  private byte[] window; // 用来记录窗口；1表示发送且已经确认；其他情况为0

  private int Wsize = 0; // 用来记录记录窗口的大小
  private int base = 0; // 正在等待ack确认的最小的序号
  private int nextseqnum = 0; // 用来记录窗口可用还未发送的最小序号,但是在是现实时记录与base的相对距离

  public SendWindow(int size) {
    this.Wsize = size;
    this.window = new byte[size];
  }

  /**
   * 窗口向有滑动 n 步
   * 
   * @param n
   *          窗口滑动的步数
   */
  public void slipN(int n) {
    for (int i = 0; i < Wsize - n; i++) {
      this.window[i] = this.window[i + n];
    }
    for (int i = Wsize - n; i < Wsize; i++) {
      this.window[i] = 0;
    }

    base = (base + n) % seq;
    this.nextseqnum = this.nextseqnum-n;
  }
  
  /**
   * 判断是否可以滑动
   * @return
   */
  public boolean canSlip() {
    if(this.window[0] == 0) {
      return false;
    }else {
      return true;
    }
  }
  
  /**
   * 滑动可滑动最大步数
   * @return
   */
  public int slip() {
    int i = 0;
    for(int j =0;j < this.nextseqnum;j++) {
      if(this.window[j] == 1) {
        i++;
      }else {
        break;
      }
    }
    this.slipN(i);
    return i;
  }

  /**
   * 通过设置窗口中的索引来设置第一个数据是否接收到Ack的状态 索引为[0,Wsize)
   * 
   * @param n
   *          窗口中每个数据的索引
   */
  public void setAckByWin(int n) {
    this.window[n] = 1;
  }

  /**
   * 通过数据的序号设置窗口中某个数据的是否接收到Ack的状态 序号顺序 [base,(base+Wise)%seq)
   * 
   * @param seq
   *          每个数据的序号
   */
  public void setAckBySeq(int seq) {
    seq = (seq >= base) ? seq : seq + this.seq;
    this.window[seq - base] = 1;
  }

  public int getWsize() {
    return this.Wsize;
  }

  /**
   * 可以重置窗口大小，但是新的窗口大小不能小于原来窗口中的nextseqnum大小
   * 
   * @param newSize
   * @return
   */
  public boolean setWsize(int newSize) {
    if (newSize < nextseqnum) {
      return false;
    }
    byte[] newWin = new byte[newSize];
    for (int i = 0; i < this.window.length; i++) {
      newWin[i] = this.window[i];
    }
    this.Wsize = newSize;
    return true;
  }

  public int getBase() {
    return this.base;
  }

  public int getNextseqnum() {
    return this.nextseqnum;
  }
  
  public void setNextseqnum(int n) {
    this.nextseqnum = n;
  }
  
  public int getAckOfn(int n) {
    return this.window[n];
  }

}
