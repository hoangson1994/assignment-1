package com.cloudtask.cotroller;

import com.cloudtask.config.SourceConstant;
import com.cloudtask.entity.Post;
import com.cloudtask.entity.Resource;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.ObjectifyService;
import javafx.geometry.Pos;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class PushTaskController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(PushTaskController.class.getName());
    private static Queue q = QueueFactory.getQueue(SourceConstant.GAE_QUEUE_NAME);

    static {
        ObjectifyService.register(Post.class);
        ObjectifyService.register(Resource.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        LOGGER.severe("Crawler job. Started at: " + Calendar.getInstance().getTime());
        try {
            List<Resource> listSouce = ofy().load().type(Resource.class).list();
            for (Resource source :
                    listSouce) {
                Document document = Jsoup.connect(source.getUrl()).get();
                Elements els = document.select(source.getBlockSource());
                for (Element el :
                        els) {
                    String link = el.select(source.getLinkSelector()).attr("href");
                    String img = el.select(source.getImageSelector()).attr("src");
                    if ("".equalsIgnoreCase(img)) {
                        String style = el.select(source.getImageSelector()).attr("style");
                        img = style.split("\\(")[1].split("\\)")[0].replace("\"", "");
                    }
                    LOGGER.info(img);
                    String des = el.select(source.getDescriptionSelector()).html();

                    Post post = new Post(link, img, des, Post.Status.PENDING);
                    Post existPost = ofy().load().type(Post.class).id(post.getLink()).now();
                    if (existPost == null) {
                        post.setSource(source.getUrl());
                        ofy().save().entity(post).now();
                        addToQueue(post.getLink());
                    }
                }
            }

        } catch (Exception ex) {
            LOGGER.severe(ex.toString());
            ex.printStackTrace();
        }
    }

    private void addToQueue(String content) {
        q.add(TaskOptions.Builder.withMethod(TaskOptions.Method.PULL).payload(content));
    }
}
