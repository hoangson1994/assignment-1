package com.cloudtask.cotroller;

import com.cloudtask.config.SourceConstant;
import com.cloudtask.entity.Post;
import com.cloudtask.entity.Resource;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.googlecode.objectify.ObjectifyService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;
public class PullTaskController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(PushTaskController.class.getName());
    static {
        ObjectifyService.register(Post.class);
        ObjectifyService.register(Resource.class);
    }

    private static Queue q = QueueFactory.getQueue(SourceConstant.GAE_QUEUE_NAME);
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<TaskHandle> tasks = q.leaseTasks(10, TimeUnit.SECONDS, 1);
        for (TaskHandle task: tasks) {
            String nameTask = new String(task.getPayload());
            taskHandle(nameTask);
            q.deleteTask(task);
        }
    }

    private void taskHandle(String task) throws IOException {

        Post post = ofy().load().type(Post.class).id(task).now();

        Resource resource = ofy().load().type(Resource.class).id(post.getSource()).now();
        Document document = null;
        if (task.contains("http")) {
             document = Jsoup.connect(task).get();
        } else {
            String[] host = post.getSource().split("/");
            document = Jsoup.connect(host[0] + "/" + "/" + host[2] + task).get();
        }

        Element titleEl = document.selectFirst(resource.getTitleSelector());
        Element contentEl = document.selectFirst(resource.getContentSelector());
        Element authorEl = document.selectFirst(resource.getAuthorSelector());

        post.setTitle(titleEl.html());
        post.setContent(contentEl.html());
        post.setAuthor(authorEl.html());
        post.setStatus(Post.Status.INDEXED.getValue());
        ofy().save().entity(post).now();
    }
}
