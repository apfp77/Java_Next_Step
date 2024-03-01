package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    /*
     * 클라이언트 요청 데이터를 담는 InputStream을 생성자로 받아 HTTP 메소드, URL, 헤더, 본문을 분리하는 작업을 한다
     * 헤더는 Map<String, String>에 저장하여 관리한다
     * getHeader("필드 이름") 메소드를 통해 접근 가능하도록 구현한다
     * GET과 POST 메소드에 따라 전달되는 인자를 Map<String, String>에 저장하여 관리한다
     * getParameter("인자 이름") 메소드를 통해 접근 가능하도록 구현한다
     */
    Map<String, String> header = new HashMap<String, String>();
    Map<String, String> parameter = new HashMap<String, String>();

    private InputStream request = null;
    private BufferedReader br = null;

    public HttpRequest(InputStream in) throws IOException{
        request = in;
        br = new BufferedReader(new InputStreamReader(in));
        splitHeader();
        splitBody();
    }

    private void splitBody() throws IOException{
    }

    private void splitHeader() throws IOException{
        requestLine();
        requestHeader();
    }

    private void requestHeader() throws IOException{
        String line = br.readLine();
        while (!"".equals(line)){
            String[] splitLine = line.split(": ");
            if (splitLine.length != 2) continue;
            header.put(splitLine[0], splitLine[1]);
            line = br.readLine();
        }
    }

    private void requestLine() throws IOException {
        String line = br.readLine();
        if (line == null) throw new IOException();
        String[] splitLine = line.split(" ");
        if (splitLine.length != 3) throw new IOException();
        header.put("httpMethod", splitLine[0]);
        header.put("url", splitLine[1]);
        header.put("httpVersion", splitLine[2]);
    }

}
