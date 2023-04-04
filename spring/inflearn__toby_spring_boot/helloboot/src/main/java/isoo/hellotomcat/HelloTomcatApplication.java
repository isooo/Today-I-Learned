package isoo.hellotomcat;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class HelloTomcatApplication {
    public static void main(String[] args) {
        // 스프링 컨테이너 만들기
        final GenericWebApplicationContext applicationContext = new GenericWebApplicationContext() {
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

        applicationContext.registerBean(HelloController.class); // 컨트롤러를 직접 생성해줘도 되지만, 클래스로 등록해줄 수 있다
        applicationContext.registerBean(SimpleHelloService.class); // 인터페이스 타입으로 클래스를 생성할 수 없으니, 구현체를 등록
        applicationContext.refresh(); // 등록된 정보들을 바탕으로 컨테이너를 초기화 함(bean 오브젝트들을 만듦)
    }
}
