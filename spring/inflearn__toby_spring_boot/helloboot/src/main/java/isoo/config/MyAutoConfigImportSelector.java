package isoo.config;

import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.type.AnnotationMetadata;

public class MyAutoConfigImportSelector implements DeferredImportSelector {
    @Override
    public String[] selectImports(final AnnotationMetadata importingClassMetadata) {
        return new String[]{
                "isoo.config.autoconfig.DispatcherServletConfig",
                "isoo.config.autoconfig.TomcatWebServerConfig"
        };
    }
}
