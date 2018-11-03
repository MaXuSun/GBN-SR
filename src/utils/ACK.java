package utils;

/**
 * 该类为接收方进行确认时使用的Ack确认报文
 * @author MaXU
 *
 */
public class ACK {
  private byte ack;
  public ACK(byte ack) {
    this.ack = ack;
  }
  
  public void setAck(byte ack) {
    this.ack = ack;
  }
  
  public byte getAck() {
    return this.ack;
  }
}
