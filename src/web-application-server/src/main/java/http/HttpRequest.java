package http;

import util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HttpRequest {
    private final HttpHeaders header = new HttpHeaders();
    private RequestLine requestLine;

    private final RequestParams parameters = new RequestParams();

    private final BufferedReader br;

    public HttpRequest(InputStream in) throws IOException{
        br = new BufferedReader(new InputStreamReader(in));
        splitHeader();
        splitBody();
    }

    private void splitBody() throws IOException{
        httpMethodGetParameters();
        if (HttpMethod.POST == requestLine.getMethod()) httpMethodPostParameters();
    }

    private void httpMethodGetParameters() {
        String queryString = requestLine.getQueryString();
        if (queryString != null) parameters.addBody(queryString);
    }

    private void httpMethodPostParameters() throws IOException {
        if (header.getHeader("Content-Length") != null){
            parameters.addBody(IOUtils.readData(br, Integer.parseInt(header.getHeader("Content-Length"))));
        }
    }

    private void splitHeader() throws IOException{
        requestLine();
        requestHeader();
    }

    private void requestHeader() throws IOException{
        String line = br.readLine();
        while (!"".equals(line)){
            header.add(line);
            line = br.readLine();
        }
    }

    private void requestLine() throws IOException {
        String line = br.readLine();
        if (line == null) throw new IOException();
        requestLine = new RequestLine(line);
    }

    public HttpMethod getMethod(){ return requestLine.getMethod(); }

    public String getPath(){ return requestLine.getPath(); }

    public String getHeader(String key){
        return header.getHeader(key);
    }

    public String getParameter(String key){
        return parameters.getParameter(key);
    }
}
