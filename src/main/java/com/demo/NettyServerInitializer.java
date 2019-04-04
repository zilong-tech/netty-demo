package com.demo;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

import java.util.concurrent.TimeUnit;

/**
 * Description:服务初始化类
 * <p>
 * Create on 2019/04/04
 *
 * @author zouyongsheng
 */
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    // 配置入站、出站事件channel
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {

        ChannelPipeline pipeline = socketChannel.pipeline();

        //服务端心跳配置
        //入参说明: 读超时时间、写超时时间、所有类型的超时时间、时间单位
        pipeline.addLast(new IdleStateHandler(12 * 60, 0, 0, TimeUnit.SECONDS));
        //设置写出超时时间，5秒
        pipeline.addLast(new WriteTimeoutHandler(5*1000,TimeUnit.MILLISECONDS));
        //发送数据进行编码时，自动计算消息体长度，并在消息体前加上4个byte记录消息体长度作为消息头
        pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
        //命令服务
        pipeline.addLast("serverHandler", new ServerHandler());
    }
}
