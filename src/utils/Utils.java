package utils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * 该类为一个工具类，其中的方法被其他类处理功能时进行辅助使用
 * @author MaXU
 *
 */
public class Utils {
  private Charset charset = Charset.forName(StaticData.CHAR_FORMAT_NAME);
  
  /**
   * 解码
   * @param buffer
   * @return
   */
  public String decode(ByteBuffer buffer) {
    CharBuffer charBuffer = charset.decode(buffer);
    return charBuffer.toString();
  }
  
  /**
   * 编码
   * @param str
   * @return
   */
  public ByteBuffer encode(String str) {
    return charset.encode(str);
  }
  
  /**
   * 根据输入的n值构建n个数据帧,如果 n<=0 或者 n>Byte.MAX_VALUE 就返回 null
   * @param n 传入构建数据帧的个数
   * @return
   */
  public UDPFrame[] geneFrame(byte n) {
    if(n<=0||n>Byte.MAX_VALUE) {
      return null;
    }
    UDPFrame[] result = new UDPFrame[n];
    
    for(byte i = 0;i < n;i++) {
      result[i] = new UDPFrame();
      result[i].setSeq(i);
      result[i].setData("the "+i+" data");
    }
    return result;
  }
  public static void main(String[] args) throws UnsupportedEncodingException {
    System.out.println("0".getBytes("utf-8")[0]);
  }
}
