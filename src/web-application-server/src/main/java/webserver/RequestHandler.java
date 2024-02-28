package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

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

    private String getContextType(String path){
        if (path == null) return  null;
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".css")) return  "text/css";
        return "text/html";
    }

    private boolean isCreateUser(String path){
        return path.startsWith("/user/create");
    }

    private void createUser(String bodyData) {
//      NOTE: 정상적인 쿼리스트링이 아닌경우
        if (bodyData == null)   return;
        Map<String, String> user = HttpRequestUtils.parseQueryString(bodyData);
        User tmp = new User(user.get("userId"), user.get("password"), user.get("name"), user.get("email"));
        DataBase.addUser(tmp);
    }

    private boolean checkUser(String bodyData){
//        userId=a&password=a
        if (bodyData == null) return false;
        Map<String, String> user = HttpRequestUtils.parseQueryString(bodyData);
        User find = DataBase.findUserById(user.get("userId"));
        if (find == null) return  false;
        return find.getPassword().equals(user.get("password"));
    }


    private boolean isLogin(String path) { return "/user/login".equals(path); }
    private String getRequestBody(BufferedReader br) throws  IOException{
        String line = br.readLine();
        int contentLength = 0;
        while (!"".equals(line)) {
            if (line.contains("Content-Length")) contentLength = Integer.parseInt((line.split(": "))[1]);
            line = br.readLine();
        }
        return IOUtils.readData(br, contentLength);
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        /*
          InputStream: 클라이언트(브라우저)에서 서버로 요청을 보낼 때 전달되는 데이터 스트림
          OutputStream: 서버에서 클라이언트에 응답을 보낼 때 전달되는 데이터를 담당하는 스트림
         */
        String path;
        String contentType;
        try (InputStream in = connection.getInputStream();
             OutputStream out = connection.getOutputStream();
             DataOutputStream dos = new DataOutputStream(out);
             BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            path = getPath(br);
            contentType = getContextType(path);
//            회원가입일 경우
            if (isCreateUser(path)){
                createUser(getRequestBody(br));
                response302Header(dos);
                return;
            }
            if (isLogin(path)){
                boolean flag = checkUser(getRequestBody(br));
                try {
                    dos.writeBytes("HTTP/1.1 302 Found \r\n");
                    dos.writeBytes("Content-Type: text-html/\r\n"); // 변경된 부분
                    dos.writeBytes("Location: " + (flag ? "/index.html" : "/user/login_failed.html") + "\r\n");
                    dos.writeBytes("Set-Cookie: logined="+ (flag ? "true" : "false") + "; Path=/\r\n");
                    dos.writeBytes("\r\n");
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
                return;
            }


            try {
                byte[] body = getBody(path);
                response200Header(dos, body.length, contentType);
                responseBody(dos, body);
            }catch (IOException e){
                badRequest();
            }



        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }


    private  byte[] getBody(String path) throws IOException{
        return Files.readAllBytes(new File("src/web-application-server/webapp" + path).toPath());
    }

    private void badRequest(){
        try {
//            TODO: 할당 실패함
            DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
            dos.writeBytes("HTTP/1.1 404 Not Found \r\n");
            dos.writeBytes("Content-Type: text/html\r\n"); // 변경된 부분
            byte[] body = Files.readAllBytes(new File("src/web-application-server/webapp/notFound.html").toPath());
            dos.writeBytes("Content-Length: " + body.length + "\r\n");
            dos.writeBytes("\r\n");
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private String getPath(BufferedReader br) throws  IOException{
        String line = br.readLine();
        if (line == null) return "/notFound.html";
        String path = line.split(" ").length > 1 ? line.split(" ")[1] : null;
        if ("/".equals(path)) path = "/index.html";
        return path;
    }
    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + contentType + "\r\n"); // 변경된 부분
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    private void response302Header(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + "/index.html" + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
