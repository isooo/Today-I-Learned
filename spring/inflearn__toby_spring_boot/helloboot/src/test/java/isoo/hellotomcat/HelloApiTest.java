package isoo.hellotomcat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class HelloApiTest {
    @Test
    void 웹_응답의_3가지_요소를_검증한다() {
        final TestRestTemplate testRestTemplate = new TestRestTemplate();
        final ResponseEntity<String> responseEntity =
                testRestTemplate.getForEntity("http://localhost:8080/hello?name={name}", String.class, "Spring");

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)).startsWith(MediaType.TEXT_PLAIN_VALUE);
        assertThat(responseEntity.getBody()).isEqualTo("*Hello Spring*");
    }

    @Test
    void 예외가_발생했을_때_웹_응답의_3가지_요소를_검증한다() {
        final TestRestTemplate testRestTemplate = new TestRestTemplate();
        final ResponseEntity<String> responseEntity =
                testRestTemplate.getForEntity("http://localhost:8080/hello?name=", String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
