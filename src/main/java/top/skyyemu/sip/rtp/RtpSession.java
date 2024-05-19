package top.skyyemu.sip.rtp;

import lombok.Data;
import top.skyyemu.sip.utils.ThreadPoolUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Vector;

/**
 * @Description rtp会话，每一个SIP账号与服务端建立通话邀请侧生成一个会话
 * @Author LPkun
 * @Date 2024/5/19 18:44
 */
@Data
public class RtpSession implements Runnable {
    /**
     * 接收、发送RTO套接字
     */
    private DatagramSocket datagramSocket;
    /**
     * 监听者
     */
    private volatile Vector<RtpListener> rtpListeners = new Vector<>();
    /**
     * 是否销毁，true = 销毁
     */
    private volatile Boolean running;
    /**
     * 服务器接收RTP流的IP地址
     */
    private InetAddress serverAddress;
    /**
     * 服务器接收RTP流的端口号
     */
    private int serverPort;
    /**
     * 远端端口号（设备的媒体RTP接收端口）
     */
    private Integer remotePort;
    /**
     * 远端地址（设备的媒体RTP接收IP地址）
     */
    private InetAddress remoteAddress;

    private Thread receiveThread;
    int i = 0;

    /**
     * 构造函数
     */
    public RtpSession(InetAddress serverAddress, Integer serverPort, InetAddress remoteAddress, Integer remotePort) {
        this.serverAddress = serverAddress;
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
        this.serverPort = serverPort;

        try {
            for (int i = 0; i < 10; i++) {
                //上面默认值是0，这里给个0进去会自动绑定一个没有被占用的端口号
                this.datagramSocket = new DatagramSocket(this.serverPort, serverAddress);
                //将占用到的端口号设置到属性
                this.serverPort = this.datagramSocket.getLocalPort();
                if (this.serverPort % 2 == 0) {
                    break;
                }
                datagramSocket.close();
                this.serverPort++;
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        receiveThread = new Thread(this);
    }

    /**
     * 构造函数
     */
    public RtpSession(InetAddress serverAddress, InetAddress remoteAddress, Integer remotePort) {
        this(serverAddress, 0, remoteAddress, remotePort);
    }


    @Override
    public void run() {
        while (running) {
            byte[] trimmedData;
            // 接收RTP数据包
            try {
                int receiveBufferSize = datagramSocket.getReceiveBufferSize();
                byte[] buf = new byte[receiveBufferSize];
                final DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
                datagramSocket.receive(datagramPacket);

                int remotePort = datagramPacket.getPort();
                if (remotePort != RtpSession.this.remotePort) {
                    System.out.println(RtpSession.this.remotePort + " 》》》改变成：" + remotePort);
                    RtpSession.this.remotePort = remotePort;
                }

                byte[] data = datagramPacket.getData();
                int offset = datagramPacket.getOffset();
                int length = datagramPacket.getLength();
                trimmedData = new byte[length];
                System.arraycopy(data, offset, trimmedData, 0, length);
            } catch (Exception e) {
                // 保留中断状态
                receiveThread.interrupt();
                break;
            }
            ThreadPoolUtil.executorByIgnore(() -> rtpListeners.forEach(listener -> listener.receivedRtpPacket(trimmedData)));
        }
    }

    /**
     * 添加监听者
     *
     * @param rtpListener 监听者s
     */
    public void addListenters(RtpListener rtpListener) {
        rtpListeners.add(rtpListener);
    }

    /**
     * 销毁
     */
    public void destruction() {
        running = false;
        receiveThread.interrupt();
        datagramSocket.close();
    }

    public void start() {
        receiveThread.start();
    }
}
