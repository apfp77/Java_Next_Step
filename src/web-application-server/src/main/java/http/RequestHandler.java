package http;

import java.io.*;
import java.net.Socket;

import Controller.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import Router.Router;

/**
 * 사용자 요청이 발생하는 순간 클라이언트와 연결을 담당하는 Socket 을 전달받는다
 * 새로운 스레드를 실행하는 방식으로 멀티스레드 프로그래밍을 지원한다
 * 사용자의 요청과 응답에 대한 처리를 담당한다
 */
public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        /*
          InputStream: 클라이언트(브라우저)에서 서버로 요청을 보낼 때 전달되는 데이터 스트림
          OutputStream: 서버에서 클라이언트에 응답을 보낼 때 전달되는 데이터를 담당하는 스트림
         */
        try (InputStream in = connection.getInputStream();
             OutputStream out = connection.getOutputStream()) {
            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);
            Controller controller = (new Router()).getController(request.getPath());
            controller.service(request, response);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}