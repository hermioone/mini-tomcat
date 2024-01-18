package org.hermione.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Deque;

public class HttpConnector implements Runnable {
    int minProcessors = 3;
    int maxProcessors = 10;
    int curProcessors = 0;
    //存放Processor的池子
    Deque<HttpProcessor> processors = new ArrayDeque<>();
    public void run() {
        ServerSocket serverSocket = null;
        int port = 8080;
        try {
            serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        // 初始化池子initialize processors pool
        for (int i = 0; i < minProcessors; i++) {
            HttpProcessor initprocessor = new HttpProcessor(this);
            initprocessor.start();
            processors.push(initprocessor);
        }
        curProcessors = minProcessors;
        while (true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                //对每一个socket，从池子中拿到一个processor
                HttpProcessor processor = createProcessor();
                if (processor == null) {
                    socket.close();
                    continue;
                }
                //分配给这个processor
                processor.assign(socket);
                // Close the socket
//                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }
    //从池子中获取一个processor，池子为空且数量小于最大限制则会新建一个processor
    private HttpProcessor createProcessor() {
        synchronized (processors) {
            if (!processors.isEmpty()) {
                return ((HttpProcessor) processors.pop());
            }
            if (curProcessors < maxProcessors) {
                return (newProcessor());
            }
            else {
                return (null);
            }
        }
    }
    private HttpProcessor newProcessor() {
        HttpProcessor initprocessor = new HttpProcessor(this);
        initprocessor.start();
        processors.push(initprocessor);
        curProcessors++;
        return ((HttpProcessor) processors.pop());
    }
    void recycle(HttpProcessor processor) {
        processors.push(processor);
    }
}

