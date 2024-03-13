package http;

import java.util.HashMap;
import java.util.Map;

public class HttpHeaders {
    Map<String, String> header = new HashMap<>();

    public void add(String str) {
        String[] node = str.split(": ");
        header.put(node[0], node.length > 1 ? node[1] : "");
    }

    public String getHeader(String key) {
        return header.get(key);
    }
}
