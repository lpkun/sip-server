package top.skyyemu.sip;

import cn.hutool.core.net.Ipv4Util;

import java.net.InetAddress;

/**
 * @Description
 * @Author LPkun
 * @Date 2024/5/19 18:32
 */
public class TestMain {

    public static void main(String[] args) throws Exception {
        InetAddress addr = InetAddress.getLocalHost();
        System.out.println("Local HostAddress: " + addr.getHostAddress());
        String hostname = addr.getHostName();
        System.out.println("Local host name: " + hostname);
    }
}
