package webserver;

import java.net.ServerSocket;
import java.net.Socket;

import http.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 역할
 * - 웹 서버를 시작한다
 * - ServerSocket 을 사용하여 사용자의 요청이 있을 때까지 대기상태이다
 * - 사용자 요청이 있을 경우 요청을 RequestHandler 클래스에 위임한다
 */
public class WebServer {
    private static final Logger log = LoggerFactory.getLogger(WebServer.class);
    private static final int DEFAULT_PORT = 8080;

    private static int getPort(String[] args){
        if (args == null || args.length == 0) return DEFAULT_PORT;
        return Integer.parseInt(args[0]);
    }

    public static void main(String[] args) throws Exception {
        // NOTE: 서버소켓을 생성한다. 웹서버는 기본적으로 8080번 포트를 사용한다.
        int port = getPort(args);

        try (ServerSocket listenSocket = new ServerSocket(port)) {
            log.info("Web Application Server started {} port.", port);

            // NOTE: 클라이언트가 연결될때까지 대기한다.
            Socket connection;
            while ((connection = listenSocket.accept()) != null) {
                RequestHandler requestHandler = new RequestHandler(connection);
                requestHandler.start();
            }
        }
    }
}
