package isoo.hellotomcat;

import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class MySpringApplication {
    public static void run(final Class<?> applicationClass, final String... args) {
        // 스프링 컨테이너 만들기
        final AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext() {
            @Override
            protected void onRefresh() {
                super.onRefresh();

                // 부트에 내장된 톰캣 서블릿 컨테이너를 만들 수 있는 팩토리 클래스
                final ServletWebServerFactory serverFactory = this.getBean(ServletWebServerFactory.class); // 앞서 등록한 TomcatServletWebServerFactory을 가져옴
                final DispatcherServlet dispatcherServlet = this.getBean(DispatcherServlet.class);

                final WebServer webServer = serverFactory.getWebServer(
                        servletContext -> {
                            servletContext.addServlet(
                                    "dispatcherServlet", dispatcherServlet
                            ).addMapping("/*"); // 모든 요청을 이 서블릿이 받을 수 있게!
                        }
                );
                webServer.start();
            }
        };

        applicationContext.register(applicationClass);
        applicationContext.refresh(); // 등록된 정보들을 바탕으로 컨테이너를 초기화 함(bean 오브젝트들을 만듦)
    }
}
