package isoo.hellotomcat;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;

public class HelloTomcatApplication {
    public static void main(String[] args) {
        // 부트에 내장된 톰캣 서블릿 컨테이너를 만들 수 있는 팩토리 클래스
        final ServletWebServerFactory serverFactory = new TomcatServletWebServerFactory();
        // 필요에 따라 jetty, undertow 등 다른 서버로 교체할 수 있도록,
        // 각 서버팩토리는 WebServerFactory를 구현하고 있다
        final WebServer webServer = serverFactory.getWebServer(HelloTomcatApplication::addServlet);
        webServer.start();
    }

    private static void addServlet(ServletContext servletContext) {
        // 서블릿 컨테이너에 서블릿을 등록하기 위해 필요한 작업을 해줄 오브젝트를
        // 파라미터로 전달한다
        servletContext.addServlet("helloServlet", new HttpServlet() {
            @Override
            protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
                // 응답 만들기
                resp.setStatus(HttpStatus.OK.value()); // 200
                resp.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE); // Content-Type: text/plain
                resp.getWriter().println("Hello Servlet"); // body
            }
        }).addMapping("/hello"); // hello 라는 요청이 들어오면, 이 'helloServlet'이 처리한다
    }
}
