package Router;

import Controller.*;

import java.util.HashMap;
import java.util.Map;

public class Router {
    private final Map<String, Controller> router= new HashMap<>();

    private void setRouter() {
        router.put("/user/create", new CreateUserController());
        router.put("/user/login", new LoginController());
        router.put("/user/list", new ListUserController());
    }

    public Router(){
        setRouter();
    }

    public Controller getController(String path) {
        for (String key: router.keySet()){
            if (key.startsWith(path)) return router.get(key);
        }
        return new OtherController();
    }
}
