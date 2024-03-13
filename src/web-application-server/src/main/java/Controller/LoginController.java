package Controller;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;

import java.io.IOException;

public class LoginController extends AbstractController{

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @Override
    public void service(HttpRequest request, HttpResponse response) {
        if (request.getMethod() != HttpMethod.POST) {
            try {
                response.methodNotAllowed("POST");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        doPost(request, response);
    }

    @Override
    public void doPost(HttpRequest request, HttpResponse response) {
        User find = DataBase.findUserById(request.getParameter("userId"));
        if (find == null || !find.getPassword().equals(request.getParameter("password"))) {
            try {
                response.sendRedirect("/user/login_failed.html");
            } catch (IOException e) {
                log.error(e.getMessage());
                throw new RuntimeException(e);
            }
            return;
        }
        response.addHeader("Ser-Cookie", "true");
        try {
            response.sendRedirect("/index.html");
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

    }
}
