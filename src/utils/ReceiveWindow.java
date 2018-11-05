package utils;

public class ReceiveWindow {
  int seq = StaticData.MAX_SEQ;
  
  private byte[] window;
  private int Wsize = 0;
  private int base = 0;
  
  public ReceiveWindow(int size) {
    this.Wsize = size;
    this.window = new byte[size];
  }
  
  public void setAck(int seq) {
    seq = (seq >= base) ? seq : seq + this.seq;
    this.window[seq - base] = 1;
  }
  
  public void setBase(int b) {
    this.base = b;
  }
  
  public int getBase() {
    return this.base;
  }
  
  /**
   * 判断是否可以向上交付
   * @return
   */
  public boolean canRcv() {
    if(this.window[0] == 1) {
      return true;
    }else {
      return false;
    }
  }
  
  /**
   * 向上交付可交付数据
   * @return
   */
  public int rcv() {
    int i = 0;
    for(int j = 0;j < this.Wsize;j++) {
      if(this.window[j] == 1) {
        i++;
        this.window[j] = 0;
      }else {
        break;
      }
    }
    for(int j = i;j < Wsize;j++) {
      window[j-i] = window[j];
    }
    for(int j = Wsize-i;j<Wsize;j++) {
      this.window[j] = 0;
    }
    this.base = (this.base+i)%this.seq;
    return i;
  }
  
  public String getCo() {
    String string = "";
    for(int i =0;i < Wsize;i++) {
      string = " "+string+window[i];
    }
    return string;
    
  }
      
}
