package com.ryan;

import io.javalin.Javalin;
import io.javalin.http.JavalinServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class MainServlet extends HttpServlet {

    private JavalinServlet javalinServlet;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        javalinServlet.service(req, resp);
    }

    @Override
    public void destroy() {
        javalinServlet.destroy();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        javalinServlet = Javalin.createStandalone()
                .get("/", context -> {
                    String token = context.sessionAttribute("token");
                    if (token == null) {
                        context.redirect("login");
                        return;
                    }


                })
                .get("/login", context -> {
                    String path = getServletContext().getRealPath("login.html");
                    String login = String.join("\n", Files.readAllLines(Paths.get(path)));
                    context.html(login);
                })
                .post("/login", context -> {

                })
                .get("/public/{item}", context -> {
                    String path = getServletContext().getRealPath(context.pathParam("item"));
                    String file = String.join("\n", Files.readAllLines(Paths.get(path)));
                    context.result(file);
                    String[] split = path.split("\\.");
                    context.contentType("text/" + split[split.length - 1]);
                })
                .javalinServlet();
        super.init(config);
    }

}
