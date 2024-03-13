package Controller;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;

import java.io.IOException;

public class CreateUserController extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(CreateUserController.class);

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
        if (request.getMethod() != HttpMethod.POST) return;
        DataBase.addUser(new User(request.getParameter("userId"), request.getParameter("password"), request.getParameter("name"), request.getParameter("email")));
        try {
            response.sendRedirect("/index.html");
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException();
        }
    }
}
