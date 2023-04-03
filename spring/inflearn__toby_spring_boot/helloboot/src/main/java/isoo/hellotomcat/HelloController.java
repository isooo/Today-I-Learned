package isoo.hellotomcat;

import java.util.Objects;

public class HelloController {
    public String hello(final String name) {
        final SimpleHelloService helloService = new SimpleHelloService();
        return helloService.sayHello(Objects.requireNonNull(name));
    }
}
