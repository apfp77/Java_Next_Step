# 실습을 위한 개발 환경 세팅
* https://github.com/slipp/web-application-server 프로젝트를 자신의 계정으로 Fork한다. Github 우측 상단의 Fork 버튼을 클릭하면 자신의 계정으로 Fork된다.
* Fork한 프로젝트를 eclipse 또는 터미널에서 clone 한다.
* Fork한 프로젝트를 eclipse로 import한 후에 Maven 빌드 도구를 활용해 eclipse 프로젝트로 변환한다.(mvn eclipse:clean eclipse:eclipse)
* 빌드가 성공하면 반드시 refresh(fn + f5)를 실행해야 한다.

# 웹 서버 시작 및 테스트
* webserver.WebServer 는 사용자의 요청을 받아 RequestHandler에 작업을 위임하는 클래스이다.
* 사용자 요청에 대한 모든 처리는 RequestHandler 클래스의 run() 메서드가 담당한다.
* WebServer를 실행한 후 브라우저에서 http://localhost:8080으로 접속해 "Hello World" 메시지가 출력되는지 확인한다.

# 각 요구사항별 학습 내용 정리
* 구현 단계에서는 각 요구사항을 구현하는데 집중한다. 
* 구현을 완료한 후 구현 과정에서 새롭게 알게된 내용, 궁금한 내용을 기록한다.
* 각 요구사항을 구현하는 것이 중요한 것이 아니라 구현 과정을 통해 학습한 내용을 인식하는 것이 배움에 중요하다. 

# 목차별 요구 사항
## 3장
<details>
<summary> <b>요구사항</b> </summary>

### 요구사항 1 - http://localhost:8080/index.html로 접속시 응답
<details>
<summary><b>Hint</b></summary>

* BufferedReader.readLine()을 사용하여 InputStream을 한 줄 단위로 읽을 수 있다
* HTTP 요청 정보의 첫 번째 라인에서 요청 URL을 추출한다
* 요청 URL에 해당하는 파일을 webapp 디렉토리에서 읽어 전달한다
* Files.readAllBytes와 File("경로/파일명").toPath()을 사용한다


</details>

### 요구사항 2 - get 방식으로 회원가입
<details> <summary><b>Hint</b></summary>
* HTTP 요청의 첫 번째 라인에서 요청 URL을 추출하여 User 클래스에 담아 DataBase 클래스에 저장한다
* 이름=값 파싱은 util.HttpRequestUtils 클래스의 parseQueryString()메소드를 활용한다
  * 요청 URL에서 "?"뒤의 문자열만 넣어야한다
</details>

### 요구사항 3 - post 방식으로 회원가입
<details> <summary><b>Hint</b></summary>
* POST로 데이터를 전달할 경우 전달하는 데이터는 HTTP 본문에 담긴다
* HTTP 본문은 HTTP 헤더 이후 빈 공백을 가지는 한 줄 다음부터 시작한다
* 본문의 데이터는 util.IOUtils 클래스의 readData() 메소드를 활용한다
* 본문의 길이는 HTTP 헤더의 Content-Length의 값이다
</details>

### 요구사항 4 - redirect 방식으로 이동
<details> <summary><b>Hint</b></summary>

* 302 status code를 찾아본다

</details>

### 요구사항 5 - 로그인 하기
#### /user/login.html으로 이동해 로그인을 할 수 있다
#### 로그인 성공 시 Set-Cookie를 추가해 로그인 성공 여부를 전달하고 /login.html로 이동한다
#### 실패 시 로그인 실패 여부를 전달하고 /user/login_failed.html로 이동한다
* ex) `Cookie: logined=true` or `Cookie: logined=false`

### 요구사항 6 - 사용자 목록 출력
로그인에 성공한 사람의 목록을 페이지로 만들어서 반환한다
<details> <summary><b>Hint</b></summary>

* 로그인 여부를 판단하기 위해 Cookie값을 확인한다
* util.HttpRequestUtils 클래스의 parseCookies 메소드를 활용하여 Cookie를 파싱한다
* StringBuilder을 활용하여 HTML을 동적으로 생성한 후 응답을 보낸다
</details>

### 요구사항 7 - stylesheet 적용
* 확장자가 css, js로 들어오는 요청을 처리한다
<details> <summary><b>Hint</b></summary>

* 응답 헤더의 Content-Type을 변경한다
* css: `text/css`, js: `application/javascript`
</details>
</details>

## 4장은 3장의 풀이과정으로 생략합니다

## 5장 HTTP 웹 서버 리팩토링 실습
<details> <summary><b>요구사항</b></summary>

### 요구사항 1 - 요청 데이터를 처리하는 로직을 별도의 클래스로 분리한다(HttpRequest)
<details> <summary><b>Hint</b></summary>

* 클라이언트 요청 데이터를 담는 InputStream을 생성자로 받아 HTTP 메소드, URL, 헤더, 본문을 분리하는 작업을 한다
* 헤더는 Map<String, String>에 저장하여 관리한다
* getHeader("필드 이름") 메소드를 통해 접근 가능하도록 구현한다
* GET과 POST 메소드에 따라 전달되는 인자를 Map<String, String>에 저장하여 관리한다
* getParameter("인자 이름") 메소드를 통해 접근 가능하도록 구현한다
</details>

### 요구사항 2 - 응답 데이터를 처리하는 로직을 별도의 클래스로 분리한다(HttpResponse)
<details> <summary><b>Hint</b></summary>

* 중복된 코드를 제거해본다
* 응답 헤더를 Map<String, String>으로 관리한다
* 파일을 직접 읽어 응답으로 보내는 메소드는 forward로 지정한다
* 다른 URL로 리다이렉트하는 메소드는 sendRedirect로 지정한다
</details>

### 요구사항 3 - 다형성을 활용해 클라이언트 요청 URL에 대한 분기 처리를 제거한다
<details> <summary><b>Hint</b></summary>

* 각 URL에 대해 분기를 if/else이 아닌 자바의 다형성을 활용한다
* 각 요청과 응답에 대한 처리를 담당하는 부분을 추상화해 인터페이스로 만든다
    ```java
    public interface Controller{
        void service(HttpRequest request, HttpResponse response);
    }
    ```
* 각 분기문을 Controller 인터페이스를 구현하는 클래스를 만들어 분리한다
* 위에서 구현한 Controller를 Map<String, Controller>에 저장하여 관리한다
  * key: URL, value: Controller 구현체
* 클라이언트 요청 URL에 해당하는 Controller를 찾아 service()메소드를 호출한다
* Controller 인터페이스를 구현하는 AbstractController 추상클래스를 추가해 중복을 제거한다
* service 메소드에서 GET과 POST HTTP 메소드에 따라 doGet, doPost 메소드를 호출한다
</details>
</details>

