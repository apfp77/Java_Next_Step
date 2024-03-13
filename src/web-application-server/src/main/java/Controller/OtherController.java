package Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import http.HttpRequest;
import http.HttpResponse;

import java.io.IOException;

public class OtherController extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(OtherController.class);
    @Override
    public void service(HttpRequest request, HttpResponse response) {
        try {
            response.forward(request.getPath());
        }catch (IOException e){
            try {
                log.error(e.getMessage());
                response.notFound();
            }catch (IOException error){
                log.error(error.getMessage());
            }
        }
    }
}
