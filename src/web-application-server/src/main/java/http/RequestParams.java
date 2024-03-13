package http;

import java.util.HashMap;
import java.util.Map;

public class RequestParams {
    Map<String, String> parameters = new HashMap<>();

    public void addQueryString(String parameter) {
        String[] paramSplit = parameter.split("=");
        if (paramSplit.length != 2) return ;
        parameters.put(paramSplit[0], paramSplit[1]);
    }

    public void addBody(String body) {
        String[] getParameters = body.split("&");
        for (String param : getParameters){
            addQueryString(param);
        }
    }

    public String getParameter(String key) {
        return parameters.get(key);
    }
}
