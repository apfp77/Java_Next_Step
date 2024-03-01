package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
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

    private String httpMethod = "";

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
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String path = getPath(br);
            responseByRequestToPath(path, br);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
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

    private void postCreateUser(String bodyData) {
        // NOTE: 정상적인 쿼리스트링이 아닌경우
        if (bodyData == null)   return;
        Map<String, String> user = HttpRequestUtils.parseQueryString(bodyData);
        User tmp = new User(user.get("userId"), user.get("password"), user.get("name"), user.get("email"));
        DataBase.addUser(tmp);
    }

    private boolean checkUser(String bodyData){
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


    private void responseByRequestToPath(String path, BufferedReader br) throws IOException{
        //NOTE: 회원 가입
        if (isCreateUser(path)){
            responseCreateUser(br, path);
            return;
        }
        //NOTE: 로그인
        if (isLogin(path)){
            responseLogin(checkUser(getRequestBody(br)));
            return;
        }
        //NOTE: 사용자 목록
        if(isUserList(path)){
            responseUserList(br);
            return;
        }
        otherRequest(path);
    }

    private void otherRequest(String path) throws IOException {
        try {
            byte[] body = getBody(path);
            response200Header(body.length, getContextType(path));
            responseBody(body);
        }catch (IOException e){
            responseNotFound();
        }
    }

    private void responseUserList(BufferedReader br) throws IOException {
        DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
        if (!isConnectUser(br)){
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Content-Type: text/html \r\n");
            dos.writeBytes("Location: " + "/user/login.html" + "\r\n");
            dos.writeBytes("\r\n");
            return;
        }
        connectUserListPage();
    }

    //NOTE: webapp/user/login.html 에서 form method 확인
    private void responseCreateUser(BufferedReader br, String path) throws  IOException{
        if ("GET".equals(httpMethod)) getCreateUser(path);
        if ("POST".equals(httpMethod)) postCreateUser(getRequestBody(br));
        response302Header();
    }

    private void getCreateUser(String path) {
        Map<String, String> user = HttpRequestUtils.parseQueryString(path.substring(path.indexOf("?") + 1));
        User tmp = new User(user.get("userId"), user.get("password"), user.get("name"), user.get("email"));
        DataBase.addUser(tmp);
    }

    private void responseLogin(boolean flag) throws IOException {
        DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
        dos.writeBytes("HTTP/1.1 302 Found \r\n");
        dos.writeBytes("Content-Type: text/html \r\n");
        dos.writeBytes("Location: " + (flag ? "/index.html" : "/user/login_failed.html") + "\r\n");
        dos.writeBytes("Set-Cookie: logined="+ (flag ? "true" : "false") + "; Path=/\r\n");
        dos.writeBytes("\r\n");
    }

    private String userListPage(){
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<html>\n");
        htmlBuilder.append("<title>Hello World</title>\n");
        htmlBuilder.append("<body>\n");
        htmlBuilder.append("<h1>접속중인 유저 목록</h1>\n");
        Collection<User> users = DataBase.findAll();
        for (User user: users){
            htmlBuilder.append("<span>");
            htmlBuilder.append(user.getUserId());
            htmlBuilder.append("</span>\n");
        }
        htmlBuilder.append("</body>\n");
        htmlBuilder.append("</html>\n");
        return  htmlBuilder.toString();
    }
    private void connectUserListPage() throws IOException {
        DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
        String htmlBuilder = userListPage();

        dos.writeBytes("HTTP/1.1 200 OK \r\n");
        dos.writeBytes("Content-Type: text/html; charset=utf-8\r\n");
        dos.writeBytes("Content-Length: " + htmlBuilder.length() + "\r\n");
        dos.writeBytes("\r\n");
        dos.write(htmlBuilder.getBytes(StandardCharsets.UTF_8), 0, htmlBuilder.length());
        dos.flush();
    }

    private boolean isConnectUser(BufferedReader br) throws IOException {
        String line = br.readLine();
        String separator = "Cookie: ";
        String getCookieKey = "logined";
        while (!"".equals(line)) {
            if (line.startsWith(separator)){
                try {
                    return Boolean.parseBoolean((HttpRequestUtils.parseCookies(line.substring(separator.length()))).get(getCookieKey));
                }catch (NullPointerException e){
                    return  false;
                }
            }
            line = br.readLine();
        }
        return  false;
    }

    private boolean isUserList(String path) {
        return path.startsWith("/user/list");
    }


    private  byte[] getBody(String path) throws IOException{
        return Files.readAllBytes(new File("src/web-application-server/webapp" + path).toPath());
    }

    private void responseNotFound() throws IOException{
        DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
        dos.writeBytes("HTTP/1.1 404 Not Found \r\n");
        dos.writeBytes("Content-Type: text/html\r\n"); // 변경된 부분
        byte[] body = Files.readAllBytes(new File("src/web-application-server/webapp/notFound.html").toPath());
        dos.writeBytes("Content-Length: " + body.length + "\r\n");
        dos.writeBytes("\r\n");
        dos.write(body, 0, body.length);
        dos.flush();
    }

    private String getPath(BufferedReader br) throws  IOException{
        String line = br.readLine();
        if (line == null) return "/notFound.html";
        String[] split = line.split(" ");
        if (split.length < 3) return "/notFound.html";
        httpMethod = split[0];
        if ("/".equals(split[1])) return "/index.html";
        return split[1];
    }
    private void response200Header(int lengthOfBodyContent, String contentType) throws IOException {
        DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
        dos.writeBytes("HTTP/1.1 200 OK \r\n");
        dos.writeBytes("Content-Type: " + contentType + "\r\n"); // 변경된 부분
        dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
        dos.writeBytes("\r\n");
    }
    private void response302Header() throws IOException {
        DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
        dos.writeBytes("HTTP/1.1 302 Found \r\n");
        dos.writeBytes("Location: " + "/index.html" + "\r\n");
        dos.writeBytes("\r\n");
    }

    private void responseBody(byte[] body) throws IOException{
        DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
        dos.write(body, 0, body.length);
        dos.flush();
    }
}
