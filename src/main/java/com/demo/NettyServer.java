package com.demo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 基本过程如下：
 *
 * 初始化创建 2 个 NioEventLoopGroup，其中 boosGroup 用于 Accetpt 连接建立事件并分发请求，workerGroup 用于处理 I/O 读写事件和业务逻辑。
 * 基于 ServerBootstrap(服务端启动引导类)，配置 EventLoopGroup、Channel 类型，连接参数、配置入站、出站事件 handler。
 * 绑定端口，开始工作。
 *
 * Server 端包含 1 个 Boss NioEventLoopGroup 和 1 个 Worker NioEventLoopGroup。
 *
 * NioEventLoopGroup 相当于 1 个事件循环组，这个组里包含多个事件循环 NioEventLoop，每个 NioEventLoop 包含 1 个 Selector 和 1 个事件循环线程。
 *
 * 每个 Boss NioEventLoop 循环执行的任务包含 3 步：
 * 轮询 Accept 事件。
 * 处理 Accept I/O 事件，与 Client 建立连接，生成 NioSocketChannel，并将 NioSocketChannel 注册到某个 Worker NioEventLoop 的 Selector 上。
 * 处理任务队列中的任务，runAllTasks。任务队列中的任务包括用户调用 eventloop.execute 或 schedule 执行的任务，或者其他线程提交到该 eventloop 的任务。
 *
 * 每个 Worker NioEventLoop 循环执行的任务包含 3 步：
 * 轮询 Read、Write 事件。
 * 处理 I/O 事件，即 Read、Write 事件，在 NioSocketChannel 可读、可写事件发生时进行处理。
 * 处理任务队列中的任务，runAllTasks。
 *
 *
 *
 * @author zouyongsheng
 */

@Component
@Slf4j
public class NettyServer {

    ChannelFuture channelFuture = null;

    NioEventLoopGroup workGroup = null;

    NioEventLoopGroup boosGroup = null;
    @PostConstruct
    public void init(){

        try {
            // 创建mainReactor 创建boss线程组 用于服务端接受客户端的连接
            boosGroup = new NioEventLoopGroup();
            //创建 worker 线程组 用于进行 SocketChannel 的数据读写
            workGroup = new NioEventLoopGroup();
            //用于应用程序网络层配置的容器
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //设置使用的EventLoopGroup
            serverBootstrap.group(boosGroup,workGroup)
                            // 设置channel类型为NIO类型
                           .channel(NioServerSocketChannel.class)
                           //设置客户端请求队列的大小
                           .option(ChannelOption.SO_BACKLOG,1024)
                          // 设置 NioServerSocketChannel 的处理器
                           .handler(new LoggingHandler(LogLevel.INFO))
                           //设置处理连入服务端的 Client 的 SocketChannel 的子处理器,配置入站、出站事件handler
                           .childHandler(new NettyServerInitializer());
            //绑定端口，并同步等待成功，即启动服务端
             channelFuture = serverBootstrap.bind(8888).sync();
             log.info("服务器启动");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @PreDestroy
    public void destory(){

        try {
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            // 优雅关闭两个 EventLoopGroup 对象，释放资源，包括所有创建的线程
            workGroup.shutdownGracefully();
            boosGroup.shutdownGracefully();
        }
    }
}
