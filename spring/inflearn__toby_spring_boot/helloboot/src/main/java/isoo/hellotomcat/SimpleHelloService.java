package isoo.hellotomcat;

import org.springframework.stereotype.Component;

@Component
public class SimpleHelloService implements HelloService {
    @Override
    public String sayHello(final String name) {
        return "Hello " + name;
    }
}
