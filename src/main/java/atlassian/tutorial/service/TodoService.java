package atlassian.tutorial.service;

import atlassian.tutorial.entity.Todo;
import java.util.List;

public interface TodoService {
    Todo add(String description);
    List<Todo> all();
    Todo get(int id);
    void update(int id, String description);
    void delete(int id);
}
