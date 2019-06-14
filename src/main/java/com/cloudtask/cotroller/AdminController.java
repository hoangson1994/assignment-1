package com.cloudtask.cotroller;

import com.cloudtask.entity.Post;
import com.cloudtask.entity.Resource;
import com.google.gson.Gson;
import com.googlecode.objectify.ObjectifyService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class AdminController extends HttpServlet {

    static {
        ObjectifyService.register(Resource.class);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        req.getParameterMap();
        String url = req.getParameter("url");
        String imageSelector = req.getParameter("image");
        String blockSelector = req.getParameter("block");
        String descriptionSelector = req.getParameter("description");
        String titleSelector = req.getParameter("title");
        String contentSelector = req.getParameter("content");
        String authorSelector = req.getParameter("author");
        String linkSelector = req.getParameter("link");

        // validate
        Resource resource = new Resource(url, Resource.Status.ACTIVE.getValue());
        resource.setImageSelector(imageSelector);
        resource.setBlockSource(blockSelector);
        resource.setDescriptionSelector(descriptionSelector);
        resource.setTitleSelector(titleSelector);
        resource.setContentSelector(contentSelector);
        resource.setAuthorSelector(authorSelector);
        resource.setLinkSelector(linkSelector);
        ofy().save().entity(resource).now();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        List<Resource> resources = ofy().load().type(Resource.class).list();
        resp.getWriter().print(new Gson().toJson(resources));
        resp.getWriter().flush();
    }
}
