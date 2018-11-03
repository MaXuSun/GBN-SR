package client;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.*;
import java.nio.*;


public class SendChannel{
    public static void main(String args[]){
        DatagramChannel channel = DatagramChannel.open();
        DatagramSocket socket = channel.socket();
        SocketAddress localAddress = new InetSocketAddress(7000);
        SocketAddress remoteAddress = new InetSocketAddress(InetAdress.getByName("localhost"), 8000);
        socket.bind(localAddress);
        while(true){
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            buffer.clear();
            System.out.println("缓冲区的剩余字节数为"+buffer.remaining());
            int n = channel.send(buffer,remoteAddress);
            System.out.println("发送的字节数为"+n);
            Thread.sleep(500);
        }

    }
}