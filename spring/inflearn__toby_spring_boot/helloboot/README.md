# 버전
- spring boot 3.0.4
- gradle 7.6.1
- java 17

# ch2
- API 테스트하기
  - [HTTPPie](https://httpie.io/)를 이용한 테스트
    - 맥에서 터미널로 설치 시
      - `brew install httpie`
    - 호출 시
      - `http -v ":8080/hello?name=isoo"`
        - `-v` 옵션는 호출 정보를 자세히 알고자 할 때 붙임
      ```
      ➜  helloboot git:(main) ✗ http -v ":8080/hello?name=isoo"
      GET /hello?name=isoo HTTP/1.1
      Accept: */*
      Accept-Encoding: gzip, deflate
      Connection: keep-alive
      Host: localhost:8080
      User-Agent: HTTPie/3.2.1
      
      
      
      HTTP/1.1 200 
      Connection: keep-alive
      Content-Length: 10
      Content-Type: text/plain;charset=UTF-8
      Date: Sun, 19 Mar 2023 06:58:12 GMT
      Keep-Alive: timeout=60
      
      hello isoo
      ```
