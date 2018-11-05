package utils;

import java.io.UnsupportedEncodingException;

/**
 * 该类为进行模拟时发送的数据帧对象，其变量仅有一个byte[]数组；
 * 该数组的第一位为一个seq,其余为数据部分data
 * @author MaXU
 *
 */
public class UDPFrame {

  // 第一位字节为seq,其余位为数据data
  private byte[] allData = new byte[1471];

  /**
   * 得到整个报文数据
   * @return 一个 byte[]
   */
  public byte[] getAllData() {
    return this.allData;
  }

  /**
   * 得到报文部分的seq
   * @return  一个 byte
   */
  public byte getSeq() {
    return this.allData[0];
  }

  /**
   * 设置报文中的seq
   * @param seq    一个 byte
   */
  public void setSeq(byte seq) {
    this.allData[0] = seq;
  }

  /**
   * 得到报文部分的data部分
   * @return     一个 byte[]
   */
  public byte[] getData() {
    byte[] result = new byte[1470];
    for (int i = 1; i < this.allData.length; i++) {
      result[i - 1] = this.allData[i];
    }

    return result;
  }

  /**
   * 设置报文部分的data部分，传入的是一个byte[],如果该数组长度大于原来能够容纳的最大长度
   * 就将后面超出的部分丢弃，并且返回fasle,如果正常设置就反水true
   * @param data    一个 byte[]
   * @return      false或者true
   */
  public boolean setData(byte[] data) {
    for (int i = 0; i < data.length; i++) {
      if (i > 1469)                            //大于数据长度的部分直接舍弃掉并返回false
        return false;
      this.allData[i + 1] = data[i];
    }
    return true;
  }

  /**
   * 传入一个String,然后将该string在内部转换为一个byte[]后调用setData进行处理
   * @param data
   * @return
   */
  public boolean setData(String data) {
    try {
      data.getBytes(StaticData.CHAR_FORMAT_NAME);
    } catch (UnsupportedEncodingException e) {
      System.err.println("将string数据编译成byte[]出错");
    }
    byte[] byteData = data.getBytes();
    return this.setData(byteData);
  }
  
  /**
   * 返回byte[]中data部分，不过是以String形式
   * @return
   */
  public String getStrData() {
    try {
      return new String(this.getData(),StaticData.CHAR_FORMAT_NAME);
    } catch (UnsupportedEncodingException e) {
      System.err.println("将data转为string出错");
    }
    return "";
  }
  
  public void setAllData(byte[] data) {
    this.allData = data;
  }

}
