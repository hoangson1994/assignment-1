package com.cloudtask.cotroller;

import com.cloudtask.entity.Post;
import com.cloudtask.resdtos.PostResDto;
import com.google.gson.Gson;
import com.googlecode.objectify.ObjectifyService;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class PostController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(PushTaskController.class.getName());

    static {
        ObjectifyService.register(Post.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
//        setAccessControlHeaders(resp);
        PrintWriter res = resp.getWriter();
        LOGGER.info(req.getRequestURI());
        if ("/posts/".contains(req.getRequestURI())) {
            List<Post> posts = ofy().load().type(Post.class).filter("status =", Post.Status.INDEXED.getValue()).list();
            List<PostResDto> postResDtos = new ArrayList<>();
            for (Post post : posts
            ) {
                postResDtos.add(new PostResDto(post));
            }
            res.print(new Gson().toJson(postResDtos));
            res.flush();
        } else if (req.getRequestURI().contains("/posts/detail")) {
            String param = req.getParameter("p");
            LOGGER.info(param);
            Post post = ofy().load().type(Post.class).id(param).now();
            res.print(new Gson().toJson(post));
            res.flush();
        } else if (req.getRequestURI().contains("posts/news")) {
            List<Post> posts = ofy().load().type(Post.class).limit(4).list();
            List<PostResDto> postResDtos = new ArrayList<>();
            for (Post post : posts
            ) {
                postResDtos.add(new PostResDto(post));
            }
            res.print(new Gson().toJson(postResDtos));
            res.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        setAccessControlHeaders(resp);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {

            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            String payload = buffer.toString();
            JSONObject obj = new JSONObject(payload);
            String url = (String) obj.get("url");
            String title = (String) obj.get("title");
            String description = (String) obj.get("description");
            Document document = Jsoup.connect(url).get();
            Elements elsTitle = document.select(title);
            Elements elsDescription = document.select(description);
            String saveTitle = elsTitle.text();
            String saveDesciprion = elsDescription.toString();
            JSONObject json = new JSONObject();
            String message;
            json.put("link", url);
            json.put("title", saveTitle);
            json.put("content", saveDesciprion);
            message = json.toString();

            resp.getWriter().println(message);
        }
    }

//    private void setAccessControlHeaders (HttpServletResponse resp){
//        resp.setHeader("Access-Control-Allow-Origin", "*");
//        resp.setHeader("Access-Control-Allow-Methods", "*");
//        resp.setHeader("Access-Control-Allow-Headers", "X-PINGOTHER,Content-Type,X-Requested-With,accept,Origin,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization");
//    }

}
