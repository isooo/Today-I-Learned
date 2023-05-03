package isoo.test;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigurationTest {
    @Test
    void configuration1() {
        final MyConfig myconfig = new MyConfig();
        final Bean1 bean1 = myconfig.bean1();
        final Bean2 bean2 = myconfig.bean2();
        assertThat(bean1.common).isNotSameAs(bean2.common);
    }

    @Test
    void configuration2() {
        final AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext();
        ac.register(MyConfig.class);
        ac.refresh();

        final Bean1 bean1 = ac.getBean(Bean1.class);
        final Bean2 bean2 = ac.getBean(Bean2.class);

        assertThat(bean1.common).isSameAs(bean2.common);
    }

    @Test
    void configuration3() {
        final AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext();
        ac.register(MyConfig2.class);
        ac.refresh();

        final Bean1 bean1 = ac.getBean(Bean1.class);
        final Bean2 bean2 = ac.getBean(Bean2.class);

        assertThat(bean1.common).isNotSameAs(bean2.common);
    }

    @Configuration
    static class MyConfig {
        @Bean
        public Common common() {
            return new Common();
        }

        @Bean
        public Bean1 bean1() {
            return new Bean1(common());
        }

        @Bean
        public Bean2 bean2() {
            return new Bean2(common());
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class MyConfig2 {
        @Bean
        public Common common() {
            return new Common();
        }

        @Bean
        public Bean1 bean1() {
            return new Bean1(common());
        }

        @Bean
        public Bean2 bean2() {
            return new Bean2(common());
        }
    }

    static class Bean1 {
        private final Common common;

        Bean1(final Common common) {
            this.common = common;
        }
    }

    static class Bean2 {
        private final Common common;

        Bean2(final Common common) {
            this.common = common;
        }
    }

    static class Common {
    }
}
