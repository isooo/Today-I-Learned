package isoo.hellotomcat;

public class HelloController {
    public String hello(final String name) {
        return "Hello Servlet: " + name;
    }
}
