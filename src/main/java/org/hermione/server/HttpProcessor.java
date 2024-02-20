package org.hermione.server;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

@Slf4j
public class HttpProcessor implements Runnable {
    Socket socket;
    boolean available = false;
    HttpConnector connector;

    private int serverPort = 0;
    /**
     * 决定是否关闭 Socket
     */
    private boolean keepAlive = false;
    private boolean http11 = true;

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
        InputStream input = null;
        OutputStream output = null;
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();
            keepAlive = true;
            while (keepAlive) {
                // create Request object and parse
                HttpRequest request = new HttpRequest(input);
                request.parse(socket);
                // handle session
                request.getSession(true);
                // create Response object
                HttpResponse response = new HttpResponse(output);
                response.setRequest(request);
//               response.sendStaticResource();
                request.setResponse(response);
                try {
                    response.sendHeaders();
                } catch (IOException e1) {
                    log.info(ExceptionUtils.getStackTrace(e1));
                }
                // check if this is a request for a servlet or a static resource
                // a request for a servlet begins with "/servlet/"
                if (request.getUri().startsWith("/servlet/")) {
                    ServletProcessor processor = new ServletProcessor(this.connector);
                    processor.process(request, response);
                } else {
                    StaticResourceProcessor processor = new StaticResourceProcessor();
                    processor.process(request, response);
                }
                finishResponse(response);
                log.info("response header connection------" + response.getHeader("Connection"));
                /*if ("close".equals(response.getHeader("Connection"))) {
                    // 关闭连接
                    log.warn("Socket closed");
                    keepAlive = false;
                }*/
                // 应该像上面注释掉的代码一样，检测到 response header 中带有 Connection: close 再设置 keepAlive = false
                // 这里为了测试方便，就每个请求结束都设置 keepAlive 为 false
                keepAlive = false;
            }
            // Close the socket
            socket.close();
            socket = null;
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }

    private void finishResponse(HttpResponse response) {
        response.finishResponse();
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
            } catch (InterruptedException e) {
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
