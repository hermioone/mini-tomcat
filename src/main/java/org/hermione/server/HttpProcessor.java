package org.hermione.server;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

@Slf4j
public class HttpProcessor implements Runnable{
    Socket socket;
    boolean available = false;
    HttpConnector connector;
    public HttpProcessor(HttpConnector connector) {
        this.connector = connector;
    }
    @Override
    public void run() {
        while (true) {
            // 等待分配下一个 socket
            Socket socket = await();
            if (socket == null) continue;
            // 处理来自这个socket的请求
            process(socket);
            // 完成此请求
            connector.recycle(this);
        }
    }
    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }
    public void process(Socket socket) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e1) {
            log.error(ExceptionUtils.getStackTrace(e1));
        }
        InputStream input = null;
        OutputStream output = null;
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();
            // 创建请求对象并解析
            HttpRequest request = new HttpRequest(input);
            request.parse(socket);
            // 创建响应对象
            Response response = new Response(output);
            if (request.getUri().startsWith("/servlet/")) {
                ServletProcessor processor = new ServletProcessor();
                processor.process(request, response);
            }
            else {
                StaticResourceProcessor processor = new StaticResourceProcessor();
                processor.process(request, response);
            }
            // 关闭 socket
            socket.close();
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }
    synchronized void assign(Socket socket) {
        // 等待 connector 提供新的 Socket
        while (available) {
            try {
                wait();
            } catch (InterruptedException e) {
                log.error(ExceptionUtils.getStackTrace(e));
            }
        }
        // 存储新可用的 Socket 并通知我们的线程
        this.socket = socket;
        available = true;
        notifyAll();
    }
    private synchronized Socket await() {
        // 等待 connector 提供一个新的 Socket
        while (!available) {
            try {
                wait();
            }catch (InterruptedException e) {
                log.error(ExceptionUtils.getStackTrace(e));
            }
        }
        // 通知Connector我们已经收到这个Socket了
        Socket socket = this.socket;
        available = false;
        notifyAll();
        return (socket);
    }
}
