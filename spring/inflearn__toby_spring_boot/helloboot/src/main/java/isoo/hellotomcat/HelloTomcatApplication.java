package isoo.hellotomcat;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@Configuration
public class HelloTomcatApplication {

    @Bean
    public HelloController helloController(final HelloService helloService) {
        return new HelloController(helloService);
    }

    @Bean
    public HelloService helloService() {
        return new SimpleHelloService();
    }

    public static void main(String[] args) {
        // 스프링 컨테이너 만들기
        final AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext() {
            @Override
            protected void onRefresh() {
                super.onRefresh();

                // 부트에 내장된 톰캣 서블릿 컨테이너를 만들 수 있는 팩토리 클래스
                final ServletWebServerFactory serverFactory = new TomcatServletWebServerFactory();
                // 필요에 따라 jetty, undertow 등 다른 서버로 교체할 수 있도록,
                // 각 서버팩토리는 WebServerFactory를 구현하고 있다

                final WebServer webServer = serverFactory.getWebServer(
                        servletContext -> {
                            servletContext.addServlet(
                                    "dispatcherServlet", new DispatcherServlet(this)
                            ).addMapping("/*"); // 모든 요청을 이 서블릿이 받을 수 있게!
                        }
                );
                webServer.start();
            }
        };

        applicationContext.register(HelloTomcatApplication.class); // 자바 코드로된 구성 정보를 등록해줌
        applicationContext.refresh(); // 등록된 정보들을 바탕으로 컨테이너를 초기화 함(bean 오브젝트들을 만듦)
    }
}
