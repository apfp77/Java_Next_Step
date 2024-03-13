package http;

import model.FileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

    private final DataOutputStream dos;

    private final Map<String, String> response;

    public HttpResponse(OutputStream out){
        dos = new DataOutputStream(out);
        response = new HashMap<>();
    }

    private void writeHeader() throws IOException {
        response.forEach((key, value) -> {
            try {
                dos.writeBytes(key + ": " + value + "\r\n");
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        });
        dos.writeBytes("\r\n");
    }

    private void writeBody(byte[] body) throws IOException {
        dos.write(body, 0, body.length);
        dos.flush();
    }

    private void responseLine(int code) throws IOException {
        switch (code){
            case 200: {
                dos.writeBytes("HTTP/1.1 200 OK \r\n");
                break;
            }
            case 302: {
                dos.writeBytes("HTTP/1.1 302 Found \r\n");
                break;
            }
            case 404: {
                dos.writeBytes("HTTP/1.1 404 Not Found \r\n");
                break;
            }
            case 405: {
                dos.writeBytes("HTTP/1.1 405 Method Not Allowed \r\n");
                break;
            }
        }
    }

    public void forward(String fileName) throws IOException {
        try {
            FileReader file = new FileReader(fileName);
            responseLine(200);
            addHeader("Content-Type", getContextType(fileName) + "; charset=utf-8");
            addHeader("Content-Length", file.getContentLength() + "");
            writeHeader();
            writeBody(file.getContent());
        }catch (IOException e){
            notFound();
        }
    }

    public void forwardBody(String body) throws IOException {
        try {
            byte[] bodys = body.getBytes();
            responseLine(200);
            addHeader("Content-Type", "text/html; charset=utf-8");
            addHeader("Content-Length", bodys.length + "");
            writeHeader();
            writeBody(bodys);
        }catch (IOException e){
            notFound();
        }
    }

    public void sendRedirect(String url) throws IOException {
        responseLine(302);
        addHeader("Content-Type", "text/html; charset=utf-8");
        addHeader("Location", url);
        writeHeader();
    }

    public void addHeader(String key, String value){
        if (key != null && value != null && !key.isEmpty()){
            response.put(key, value);
        }
    }

    public void notFound() throws IOException{
        FileReader file = new FileReader("/notFound.html");
        responseLine(404);
        addHeader("Content-Type", "text/html; charset=utf-8");
        addHeader("Content-Length", file.getContentLength() + "");
        writeHeader();
        writeBody(file.getContent());
    }


    private String getContextType(String path){
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".css")) return  "text/css";
        return "text/html";
    }

    public void methodNotAllowed(String code) throws IOException {
        responseLine(405);
        addHeader("Allow", code);
        addHeader("Content-Length", "0");
        writeHeader();
    }
}
