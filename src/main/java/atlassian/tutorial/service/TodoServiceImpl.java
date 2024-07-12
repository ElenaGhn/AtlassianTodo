package atlassian.tutorial.service;

import atlassian.tutorial.entity.Todo;
import atlassian.tutorial.entity.User;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableMap;
import net.java.ao.DBParam;
import net.java.ao.Query;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

@Named
public class TodoServiceImpl implements TodoService {
    @ComponentImport
    private final ActiveObjects ao;
    @ComponentImport
    private final UserManager userManager;

    @Inject
    public TodoServiceImpl(ActiveObjects ao, UserManager userManager) {
        this.ao = checkNotNull(ao);
        this.userManager = checkNotNull(userManager);
    }

    @Override
    public Todo add(String description) {
        User user = getOrCreateUser(ao, currentUserName());
        final Todo todo = ao.create(Todo.class, new DBParam("USER_ID", user.getID()));
        todo.setDescription(description);
        todo.setComplete(false);
        todo.save();
        return todo;
    }

    @Override
    public void delete(int id) {
        Todo todo = ao.get(Todo.class, id);
        if (todo != null) {
            ao.delete(todo);
        }
    }

    @Override
    public List<Todo> all() {
        User user = getOrCreateUser(ao, currentUserName());
        return newArrayList(ao.find(Todo.class, Query.select().where("USER_ID = ?", user.getID())));
    }

    @Override
    public Todo get(int id) {
        return ao.get(Todo.class, id);
    }

    @Override
    public void update(int id, String description) {
        Todo todo = ao.get(Todo.class, id);
        if (todo != null) {
            todo.setDescription(description);
            todo.save();
        }
    }

    private String currentUserName() {
        return userManager.getRemoteUser().getUsername();
    }

    private User getOrCreateUser(ActiveObjects ao, String userName) {
        User[] users = ao.find(User.class, Query.select().where("NAME = ?", userName));
        if (users.length == 0) {
            return createUser(ao, userName);
        } else if (users.length == 1) {
            return users[0];
        } else {
            throw new IllegalStateException("There shouldn't be 2 users with the same username! " + userName);
        }
    }

    private User createUser(ActiveObjects ao, String userName) {
        return ao.create(User.class, ImmutableMap.<String, Object>of("NAME", userName));
    }
}
