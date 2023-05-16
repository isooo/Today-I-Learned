package isoo.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;

import java.util.Map;

public class MyOnCondition implements Condition {
    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        final Map<String, Object> attrs = metadata.getAnnotationAttributes(ConditionalMyOnClass.class.getName());// 해당 애너테이션이 선언됐을 때, 해당 애너테이션의 value 값을 가져올 수 있도록 하자
        final String value = (String) attrs.get("value");
        return ClassUtils.isPresent(value, context.getClassLoader()); // 이 애플리케이션에서 'value'라는 이름의 클래스가 존재하면 true, 없으면 false
    }
}
