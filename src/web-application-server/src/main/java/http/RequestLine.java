package http;

public class RequestLine {
    private String path;
    final private HttpMethod method;
    final private String version;

    private String queryString;

    public RequestLine(String line) {
        String[] node = line.split(" ");
        // FIXME: ArrayIndexOutOfBoundsException
        method = HttpMethod.valueOf(node[0]);
        // FIXME: IllegalArgumentException
        splitQueryString(node[1]);
        version = node[2];
    }

    private void splitQueryString(String url){
        int idx = url.indexOf("?");
        if (idx > 0){
            queryString = url.substring(idx + 1);
            path = url.substring(0, idx);
            return;
        }
        path = url;
    }

    public String getPath(){
        return path;
    }
    public HttpMethod getMethod(){
        return method;
    }

    public String getQueryString(){
        return queryString;
    }

    public String getVersion(){
        return version;
    }
}
