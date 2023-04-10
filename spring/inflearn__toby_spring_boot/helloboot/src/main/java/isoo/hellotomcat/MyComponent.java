package isoo.hellotomcat;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // 이 애너테이션의 라이프 사이클
@Target(ElementType.TYPE) // 애너테이션이 적용될 위치
@Component
public @interface MyComponent {
}
