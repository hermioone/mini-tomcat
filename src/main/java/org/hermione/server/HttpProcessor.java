package org.hermione.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

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
            e1.printStackTrace();
        }
        InputStream input = null;
        OutputStream output = null;
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();
            // 创建请求对象并解析
            Request request = new Request(input);
            request.parse();
            // 创建响应对象
            Response response = new Response(output);
            response.setRequest(request);
//               response.sendStaticResource();
            // 检查这是对servlet还是静态资源的请求
            // a request for a servlet begins with "/servlet/"
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
            e.printStackTrace();
        }
    }
    synchronized void assign(Socket socket) {
        // 等待 connector 提供新的 Socket
        while (available) {
            try {
                wait();
            } catch (InterruptedException e) {
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
            }
        }
        // 通知Connector我们已经收到这个Socket了
        Socket socket = this.socket;
        available = false;
        notifyAll();
        return (socket);
    }
}
