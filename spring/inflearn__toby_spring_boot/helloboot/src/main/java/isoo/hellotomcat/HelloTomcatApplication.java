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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;

public class HelloTomcatApplication {
    public static void main(String[] args) {
        // 부트에 내장된 톰캣 서블릿 컨테이너를 만들 수 있는 팩토리 클래스
        final ServletWebServerFactory serverFactory = new TomcatServletWebServerFactory();
        // 필요에 따라 jetty, undertow 등 다른 서버로 교체할 수 있도록,
        // 각 서버팩토리는 WebServerFactory를 구현하고 있다

        final HelloController helloController = new HelloController();
        final WebServer webServer = serverFactory.getWebServer(servletContext -> addFrontController(servletContext, helloController));
        webServer.start();
    }

    // 인증, 보안, 다국어, 공통 기능을 처리할 프론트 컨트롤러
    private static void addFrontController(ServletContext servletContext, HelloController helloController) {
                                                            // 서블릿 컨테이너에 서블릿을 등록하기 위해 필요한 작업을 해줄 오브젝트를
                                                            // 파라미터로 전달한다
        servletContext.addServlet("FrontController", new HttpServlet() {
            @Override
            protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {

                if ("/hello".equals(req.getRequestURI()) && HttpMethod.GET.name().equals(req.getMethod())) {
                    final String name = req.getParameter("name"); // 서블릿 요청을 이용해보자

                    final String body = helloController.hello(name);

                    // 응답 만들기
                    resp.setStatus(HttpStatus.OK.value()); // 200
                    resp.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE); // Content-Type: text/plain
                    resp.getWriter().println(body);
                } else if ("/user".equals(req.getRequestURI())) {
                    //
                } else {
                    resp.setStatus(HttpStatus.NOT_FOUND.value());
                }
            }
        }).addMapping("/*"); // 모든 요청을 이 서블릿이 받을 수 있게!
    }
}
