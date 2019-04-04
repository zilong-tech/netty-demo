package com.demo;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetSocketAddress;

/**
 * Description:监听服务控制层
 * <p>
 * Create on 2019/04/04
 *
 * @author zouyongsheng
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 建立一个新链接时触发的方法
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        InetSocketAddress inSocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIP = inSocket.getAddress().getHostAddress();
        System.out.println("客户端ip:" + clientIP);
    }

    /**
     * 接收客户端推送命令时触发
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object dataBody)
            throws Exception {

      ByteBuf byteBuf = (ByteBuf) dataBody;
      byte[] bytes = new byte[1024];
      byteBuf.readBytes(bytes,0,4);

      System.out.println(new String(bytes));
    }
}
