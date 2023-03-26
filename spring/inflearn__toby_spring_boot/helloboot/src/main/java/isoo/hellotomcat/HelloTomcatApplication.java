package isoo.hellotomcat;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;

public class HelloTomcatApplication {
    public static void main(String[] args) {
        // 부트에 내장된 톰캣 서블릿 컨테이너를 만들 수 있는 팩토리 클래스
        final ServletWebServerFactory serverFactory = new TomcatServletWebServerFactory();
                                                        // 필요에 따라 jetty, undertow 등 다른 서버로 교체할 수 있도록,
                                                        // 각 서버팩토리는 WebServerFactory를 구현하고 있다
        final WebServer webServer = serverFactory.getWebServer();
        webServer.start();
    }
}
