package isoo.study;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;

import static org.assertj.core.api.Assertions.assertThat;

public class ConditionalTest {
    @Test
    void conditional1() {
        new ApplicationContextRunner()
                .withUserConfiguration(Config1.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(MyBean.class);
                    assertThat(context).hasSingleBean(Config1.class);
                });
    }

    @Test
    void conditional2() {
        new ApplicationContextRunner()
                .withUserConfiguration(Config2.class)
                .run(context -> {
                    assertThat(context).doesNotHaveBean(MyBean.class);
                    assertThat(context).doesNotHaveBean(Config2.class);
                });
    }

    @Configuration
    @Conditional(TrueCondition.class)
    static class Config1 {
        @Bean
        MyBean myBean() {
            return new MyBean();
        }
    }

    @Configuration
    @Conditional(FalseCondition.class)
    static class Config2 {
        @Bean
        MyBean myBean() {
            return new MyBean();
        }
    }

    static class MyBean {
    }

    static class TrueCondition implements Condition {
        @Override
        public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
            return true;
        }
    }

    static class FalseCondition implements Condition {
        @Override
        public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
            return false;
        }
    }
}
