package ut.com;

import org.junit.Test;
import atlassian.tutorial.api.MyPluginComponent;
import atlassian.tutorial.service.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest {
    @Test
    public void testMyName() {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent", component.getName());
    }
}