package isoo.hellotomcat;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HelloServiceTest {
    @Test
    void simpleHelloService() {
        final SimpleHelloService helloService = new SimpleHelloService();
        final String result = helloService.sayHello("Test");
        assertThat(result).isEqualTo("Hello Test");
    }
}
