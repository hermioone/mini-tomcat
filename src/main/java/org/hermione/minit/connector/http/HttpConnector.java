package org.hermione.minit.connector.http;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hermione.minit.core.StandardContext;
import org.hermione.minit.core.StandardHost;
import org.hermione.minit.session.StandardSession;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class HttpConnector implements Runnable {

    // 线程池中最小的线程数
    int minProcessors = 3;

    // 线程池中最大运行的线程数
    int maxProcessors = 10;
    int curProcessors = 0;

    private final int port;

    public HttpConnector(int port) {
        this.port = port;
    }

    /**
     * 这里为什么不使用 JDK 中的线程池呢？
     * 因为 JDK 中的线程池当线程数 > coreSize 时，会将多余的线程塞入队列中，如果队列塞满后才会继续创建新线程，直到到达线程池的 maxSize
     * 这种情况很明显不适用于 tomcat，因为此时队列中的线程相当于被阻塞住了。
     * 所以需要使用自定义线程池，当 tomcat 启动时，会默认在池中创建 minProcessors 个线程，当线程数 > minProcessors 时，会继续创建新线程，直到线程数到达 maxProcessors
     */
    //存放Processor的池子
    final Deque<HttpProcessor> processors = new ArrayDeque<>();

    //这是与connector相关联的container
    @Getter
    @Setter
    StandardHost container = null;

    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
        } catch (IOException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            System.exit(1);
        }
        // initialize processors pool
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
                HttpProcessor processor = createProcessor();
                if (processor == null) {
                    socket.close();
                    continue;
                }
                processor.assign(socket);
                // Close the socket
//                socket.close();
            } catch (Exception e) {
                log.error(ExceptionUtils.getStackTrace(e));
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
            } else {
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


    //sessions map存放session
    public static Map<String, HttpSession> sessions = new ConcurrentHashMap<>();

    //创建新的session
    public static StandardSession createSession() {
        StandardSession session = new StandardSession();
        session.setValid(true);
        session.setCreationTime(System.currentTimeMillis());
        String sessionId = generateSessionId();
        session.setId(sessionId);
        sessions.put(sessionId, session);
        return (session);
    }

    //以随机方式生成byte数组,形成sessionid
    protected static synchronized String generateSessionId() {
        Random random = new Random();
        long seed = System.currentTimeMillis();
        random.setSeed(seed);
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        StringBuilder result = new StringBuilder("tomcat-");
        for (byte aByte : bytes) {
            byte b1 = (byte) ((aByte & 0xf0) >> 4);
            byte b2 = (byte) (aByte & 0x0f);
            if (b1 < 10) result.append((char) ('0' + b1));
            else result.append((char) ('A' + (b1 - 10)));
            if (b2 < 10) result.append((char) ('0' + b2));
            else result.append((char) ('A' + (b2 - 10)));
        }
        return (result.toString());
    }
}

