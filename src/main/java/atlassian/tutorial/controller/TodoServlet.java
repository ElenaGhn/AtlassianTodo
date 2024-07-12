package atlassian.tutorial.controller;

import atlassian.tutorial.entity.Todo;
import atlassian.tutorial.service.TodoService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.user.UserManager;

import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TodoServlet extends HttpServlet {
    private final TodoService todoService;
    @ComponentImport
    private final UserManager userManager;

    @Inject
    public TodoServlet(TodoService todoService, UserManager userManager) {
        this.todoService = checkNotNull(todoService);
        this.userManager = checkNotNull(userManager);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!enforceLoggedIn(req, res)) {
            return;
        }

        final PrintWriter w = res.getWriter();
        w.printf("<h1>Todos (%s)</h1>", userManager.getRemoteUser().getUsername());

        String action = req.getParameter("action");
        if ("edit".equals(action)) {
            final int id = Integer.parseInt(req.getParameter("id"));
            Todo todo = todoService.get(id);
            if (todo != null) {
                w.write("<form method=\"post\">");
                w.write("<input type=\"hidden\" name=\"action\" value=\"save\" />");
                w.write("<input type=\"hidden\" name=\"id\" value='" + todo.getID() + "' />");
                w.write("<input type=\"text\" name=\"task\" size=\"25\" value='" + todo.getDescription() + "' />");
                w.write("&nbsp;&nbsp;");
                w.write("<input type=\"submit\" name=\"submit\" value=\"Save\"/>");
                w.write("</form>");
            }
        } else if ("delete".equals(action)) {
            final int id = Integer.parseInt(req.getParameter("id"));
            todoService.delete(id);
            res.sendRedirect(req.getContextPath() + "/plugins/servlet/todo/list");
            return;
        } else {
            w.write("<form method=\"post\">");
            w.write("<input type=\"hidden\" name=\"action\" value=\"add\" />");
            w.write("<input type=\"text\" name=\"task\" size=\"25\"/>");
            w.write("&nbsp;&nbsp;");
            w.write("<input type=\"submit\" name=\"submit\" value=\"Add\"/>");
            w.write("</form>");

            w.write("<ol>");
            for (Todo todo : todoService.all()) {
                w.write("<li>");
                w.printf("<%2$s> %s </%2$s>", todo.getDescription(), todo.isComplete() ? "strike" : "strong");
                w.write(" <a href='" + req.getContextPath() + "/plugins/servlet/todo?action=edit&id=" + todo.getID() + "'>Edit</a>");
                w.write(" <a href='" + req.getContextPath() + "/plugins/servlet/todo?action=delete&id=" + todo.getID() + "'>Delete</a>");
                w.write("</li>");
            }
            w.write("</ol>");
            w.write("<script language='javascript'>document.forms[0].elements[0].focus();</script>");
        }

        w.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!enforceLoggedIn(req, res)) {
            return;
        }

        final String action = req.getParameter("action");

        if ("add".equals(action)) {
            final String description = req.getParameter("task");
            todoService.add(description);
        } else if ("save".equals(action)) {
            final int id = Integer.parseInt(req.getParameter("id"));
            final String description = req.getParameter("task");
            todoService.update(id, description);
        }

        res.sendRedirect(req.getContextPath() + "/plugins/servlet/todo/list");
    }

    private boolean enforceLoggedIn(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (userManager.getRemoteUser() == null) {
            res.sendRedirect(req.getContextPath() + "/plugins/servlet/login");
            return false;
        }
        return true;
    }
}
