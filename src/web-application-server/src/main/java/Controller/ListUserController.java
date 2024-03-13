package Controller;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;

import java.io.IOException;
import java.util.Collection;

public class ListUserController extends  AbstractController{
    private static final Logger log = LoggerFactory.getLogger(ListUserController.class);

    @Override
    public void service(HttpRequest request, HttpResponse response) {
        if (HttpMethod.GET != request.getMethod()) {
            try {
                response.methodNotAllowed("GET");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        doGet(request, response);
    }

    @Override
    public void doGet(HttpRequest request, HttpResponse response) {
        if (!isLogin(request.getParameter("logined"))) {
            try {
                response.sendRedirect("/user/login.html");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        try {
            response.forwardBody(userListPage());
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

    }

    private boolean isLogin(String loginedValue){
        return "true".equals(loginedValue);
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
}
