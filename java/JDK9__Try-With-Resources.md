# Java9에 추가된 기능: More Concise try-with-resources Statements  
`try-with-resources`는 JDK7에서 최초 추가된 기능이며, JDK9에선 좀 더 향상된 기능을 제공한다.  
   
`try-with-resources`는 `try-with-resources`문에서 사용할 자원을 선언하면 `try` 블럭 실행 후, 선언된 자원을 close 해준다. 이때 전달된 자원은 `AutoCloseable` 인터페이스의 구현체여야 한다.   
> `AutoCloseable` 인터페이스는 JDK7에서 추가된 인터페이스로, `close()`를 포함한다.  
> `try-with-resource`블럭이 종료될 때 `close()`가 자동으로 호출된다.   

JDK7 이전과 이후, JDK9 이후로 나누어 예시를 살펴보자.      
  
<br/>

## **JDK7 이전 코드**
```java
public static String readFirstLineFromFileWithFinallyBlock(final String path) throws IOException {
    final BufferedReader br = new BufferedReader(new FileReader(path));
    try {
        return br.readLine();
    } finally {
        if (br != null) {
            br.close();
        }
    }
}
```

`try` 블럭 안에서 특정 로직을 수행하고, `finally` 블럭에서 앞서 선언되었던 `BufferedReader br`을 `close` 해준다.    
이 코드에서 만약 `readLine()`과 `close()` 둘 다 exception이 발생한다면, `readFirstLineFromFileWithFinallyBlock()`는 `close`에서 발생한 exception을 throw한다. 즉 `try`에서 발생한 예외는 suppressed(억제?)되고 `finally`에서 발생한 예외가 throw 된다.  

<br/>
  
## **JDK7 에서 추가된 `try-with-resource`를 사용한 코드**  
```java
public static String readFirstLineFromFileForJDK7V1(final String path) throws IOException {
    try (final BufferedReader br = new BufferedReader(new FileReader(path))) {
        return br.readLine();
    }
}
```
이전 코드와 달라진 점은 `BufferedReader br` 리소스를 `try-with-resource`문안에 선언해 준 부분이다.   
`try`문안에서 로직 수행 중 예외가 발생하더라도 **리소스는 정상적으로 close**된다.  
이 코드에서 만약 `try-with-resource`문과 `try`블럭 두 군데에서 exception이 발생한다면, `readFirstLineFromFile()`는 `try`블럭에서 발생한 exception을 throw한다. 즉 `try-with-resource`문에서 발생한 예외는 suppressed되고 `readLine()`수행 중 발생한 예외를 throw한다.  
  
<br/>

> JDK7 이전 코드에서는 `readLine()`에서 예외가 발생했더라도 `close()`에서 발생한 예외로 throw 되는 반면,  
> `try-with-resource`를 사용한 코드에서는 `readLine()`에서 발생한 예외가 throw된다는 차이가 있다.  

<br/>

## **좀 더 기능이 추가된 JDK9의 `try-with-resource`를 사용한 코드**  
JDK7에서는 `try-with-resource`블럭에서 사용할 리소스는 반드시 `try-with-resource`문에서 선언을 해줬어야 했다.  
[앞선 예시](#jdk7-%EC%97%90%EC%84%9C-%EC%B6%94%EA%B0%80%EB%90%9C-try-with-resource%EB%A5%BC-%EC%82%AC%EC%9A%A9%ED%95%9C-%EC%BD%94%EB%93%9C)에서처럼 `try-with-resource`문에 곧바로 선언해주거나, 아래 예시처럼 외부에서 선언된 리소스를 `try-with-resource`문에서 새로운 변수에 담아주어야 했다.  
```java
public static String readFirstLineFromFileForJDK7V2(final String path) throws IOException {
    final BufferedReader br = new BufferedReader(new FileReader(path));
    try (BufferedReader br2 = br) { // 외부에서 선언한 리소스 br을 `try-with-resource`블럭에서 사용하기 위해, 새 변수를 선언해 할당해주어야 함
        return br2.readLine(); // br.readLine() 해도 됨... 왜 되는지는 더 살펴봐야 함!
    }
}
```

<br/>

하지만 JDK9부터는 선언을 또 할 필요 없이 `try-with-resource`문에 넘겨주기만 하면 된다. 
```java
public static String readFirstLineFromFileForJDK9(final String path) throws IOException {
    final BufferedReader br = new BufferedReader(new FileReader(path));
    try (br) {
        return br.readLine();
    }
}
```

<br/>

아래처럼 `try-with-resource`문안에서 세미콜론(`;`)을 이용해 변수를 여러 개 할당할 수도 있다.   
```java
public static void writeToFileFromZipFileContentsForJDK9(final String zipFileName, final String outputFileName) throws IOException {
    final ZipFile zf = new ZipFile(zipFileName);

    final Charset charset = java.nio.charset.StandardCharsets.US_ASCII;
    final Path outputFilePath = Paths.get(outputFileName);
    final BufferedWriter writer = Files.newBufferedWriter(outputFilePath, charset);

    try (zf; writer) {
        for (final Enumeration entries = zf.entries(); entries.hasMoreElements(); ) {
            final String newLine = System.getProperty("line.separator");
            final String zipEntryName = ((java.util.zip.ZipEntry) entries.nextElement()).getName() + newLine;
            writer.write(zipEntryName, 0, zipEntryName.length());
        }
    }
}
```

<br/>

> JDK9 이전에도 변수는 여러 개 사용할 수 있었으나, `try-with-resource`문에서 선언해주어야 했다. 
> <details>
> <summary>예시 코드1 (JDK9 이전)</summary>
> 
> ```java
> public static void writeToFileFromZipFileContents(final String zipFileName, final String outputFileName) throws IOException {
>     final Charset charset = java.nio.charset.StandardCharsets.US_ASCII;
>     final Path outputFilePath = Paths.get(outputFileName);
> 
>     try (final ZipFile zf = new ZipFile(zipFileName);
>             final BufferedWriter writer = Files.newBufferedWriter(outputFilePath, charset)
>     ) {
>         for (final Enumeration entries = zf.entries(); entries.hasMoreElements(); ) {
>             final String newLine = System.getProperty("line.separator");
>             final String zipEntryName = ((java.util.zip.ZipEntry) entries.nextElement()).getName() + newLine;
>             writer.write(zipEntryName, 0, zipEntryName.length());
>         }
>     }
> }
> ```
> 
> </details>  
>   
>     
> 또는 외부에서 선언 후, `try-with-resource`문에서 새로운 변수에 할당해 주어야 했음.  
> <details>
> <summary>예시 코드2 (JDK9 이전)</summary>
>   
> ```java
> public static void writeToFileFromZipFileContents(final String zipFileName, final String outputFileName) throws IOException {
>     final ZipFile zf = ...
> 
>     final Charset charset = ...
>     final Path outputFilePath = ...
>     final BufferedWriter writer = ...
>     
>     try (ZipFile zf2 = zf; BufferedWriter writer2 = writer) {...}
> }
> ```
>
> </details>  

<br/>

이 코드 블럭이 정상적 혹은 비정상적으로 종료되면, `try-with-resource`문에서 선언된 역순으로 리소스가 close된다. 위 예시에서는 `writer`가 먼저 close되고 그 다음에 `zf`가 close된다.    
> 이 케이스에서는 `try-with-resource`문에 선언된 두 변수를 close하다가 예외가 발생할 수 있고 또 `try`블럭에서도 예외가 발생할 수도 있는데, throw되는 예외는 앞서 설명한 바와 마찬가지로 **`try`블럭에서 발생한 예외**가 `writeToFileFromZipFileContents`를 호출한 쪽으로 throw된다. (`try-with-resource`문에서 발생할 수 있는 두 변수에 대한 예외는 suppressed 된다)   
     
<br/>
  
### + 물론 아래와 같이 `try-with-resource`를 `catch`나 `finally`와 함께 사용할 수도 있다. 
```java
public static void viewTable(final Connection con) throws SQLException {
    final String query = "select COF_NAME, SUP_ID, PRICE, TOTAL from COFFEES";

    try (final Statement stmt = con.createStatement()) {
        final ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            final String coffeeName = rs.getString("COF_NAME");
            final int supplierID = rs.getInt("SUP_ID");
            final float price = rs.getFloat("PRICE");
            final int total = rs.getInt("TOTAL");

            System.out.println(coffeeName + ", " + supplierID + ", " + price + ", " + total);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
```
이 경우 `Statement stmt`는 `try-with-resource`에 의해 close된다. 그리고 `catch`나 `finally` 블럭은 앞서 선언된 리소스가 close된 이후 실행된다.  

<br/>

# 참고
- [ORACLE Java Documentation: The Java Tutorials: The try-with-resoures Statement](https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html)
- [ORACLE Java Documentation: Java Language Updates: Java SE 9](https://docs.oracle.com/en/java/javase/16/language/java-language-changes.html#GUID-A920DB06-0FD1-4F9C-8A9A-15FC979D5DA3)
- [Baeldung: New Features in Java 9](https://www.baeldung.com/new-java-9)

### 예시 코드 전체
<details>
<summary>예시 코드</summary>

```java
public class TryResourceTest {
    public static String readFirstLineFromFileWithFinallyBlock(final String path) throws IOException {
        final BufferedReader br = new BufferedReader(new FileReader(path));
        try {
            return br.readLine();
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }

    public static String readFirstLineFromFileForJDK7V1(final String path) throws IOException {
        try (final BufferedReader br = new BufferedReader(new FileReader(path))) {
            return br.readLine();
        }
    }

    public static String readFirstLineFromFileForJDK7V2(final String path) throws IOException {
        final BufferedReader br = new BufferedReader(new FileReader(path));
        try (BufferedReader br2 = br) { // 외부에서 선언한 리소스 br을 `try-with-resource`블럭에서 사용하기 위해, 새 변수를 선언해 할당해주어야 함
            return br2.readLine(); // br.readLine() 해도 됨... 왜 되는지는 더 살펴봐야 함!
        }
    }

    public static String readFirstLineFromFileForJDK9(final String path) throws IOException {
        final BufferedReader br = new BufferedReader(new FileReader(path));
        try (br) {
            return br.readLine();
        }
    }

    public static void writeToFileFromZipFileContents(final String zipFileName, final String outputFileName) throws IOException {
        final ZipFile zf = new ZipFile(zipFileName);

        final Charset charset = java.nio.charset.StandardCharsets.US_ASCII;
        final Path outputFilePath = Paths.get(outputFileName);

        final BufferedWriter writer = Files.newBufferedWriter(outputFilePath, charset);

        try (ZipFile zf2 = zf; BufferedWriter writer2 = writer) {
            for (final Enumeration entries = zf.entries(); entries.hasMoreElements(); ) {
                final String newLine = System.getProperty("line.separator");
                final String zipEntryName = ((java.util.zip.ZipEntry) entries.nextElement()).getName() + newLine;
                writer.write(zipEntryName, 0, zipEntryName.length());
            }
        }
    }

    public static void writeToFileFromZipFileContentsForJDK9(final String zipFileName, final String outputFileName) throws IOException {
        final ZipFile zf = new ZipFile(zipFileName);

        final Charset charset = java.nio.charset.StandardCharsets.US_ASCII;
        final Path outputFilePath = Paths.get(outputFileName);
        final BufferedWriter writer = Files.newBufferedWriter(outputFilePath, charset);

        try (zf; writer) {
            for (final Enumeration entries = zf.entries(); entries.hasMoreElements(); ) {
                final String newLine = System.getProperty("line.separator");
                final String zipEntryName = ((java.util.zip.ZipEntry) entries.nextElement()).getName() + newLine;
                writer.write(zipEntryName, 0, zipEntryName.length());
            }
        }
    }

    public static void viewTable(final Connection con) throws SQLException {
        final String query = "select COF_NAME, SUP_ID, PRICE, TOTAL from COFFEES";

        try (final Statement stmt = con.createStatement()) {
            final ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                final String coffeeName = rs.getString("COF_NAME");
                final int supplierID = rs.getInt("SUP_ID");
                final float price = rs.getFloat("PRICE");
                final int total = rs.getInt("TOTAL");

                System.out.println(coffeeName + ", " + supplierID + ", " + price + ", " + total);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
```

</details>

<br/>

<details>
<summary>예시 코드 TC</summary>

```java
class TryResourceTestTest {
    private String path;
    private String zipFileName;
    private String outputFilePath;


    @BeforeEach
    void setUp() {
        path = "/Users/isoo/Desktop/test.txt";
        zipFileName = "/Users/isoo/Desktop/test_zip.zip";
        outputFilePath = "/Users/isoo/Desktop/";
    }

    @Test
    void JDK7_이전_코드() throws IOException {
        // given
        // when
        final String result = TryResourceTest.readFirstLineFromFileWithFinallyBlock(path);

        // then
        assertThat(result).isEqualTo("hello");
    }

    @Test
    void JDK7_코드_V1() throws IOException {
        // given
        // when
        final String result = TryResourceTest.readFirstLineFromFileForJDK7V1(path);

        // then
        assertThat(result).isEqualTo("hello");
    }

    @Test
    void JDK7_코드_V2() throws IOException {
        // given
        // when
        final String result = TryResourceTest.readFirstLineFromFileForJDK7V2(path);

        // then
        assertThat(result).isEqualTo("hello");
    }

    @Test
    void JDK9_코드() throws IOException {
        // given
        // when
        final String result = TryResourceTest.readFirstLineFromFileForJDK9(path);

        // then
        assertThat(result).isEqualTo("hello");
    }

    @Test
    void JDK7_코드_zip() throws IOException {
        // given
        final String outputFileName = "test_7.txt";
        final String outputFile = outputFilePath + "/" + outputFileName;

        // when
        TryResourceTest.writeToFileFromZipFileContents(zipFileName, outputFile);

        // then
        assertTrue(new File(outputFile).exists());
    }

    @Test
    void JDK9_코드_zip() throws IOException {
        // given
        final String outputFileName = "test_9.txt";
        final String outputFile = outputFilePath + "/" + outputFileName;

        // when
        TryResourceTest.writeToFileFromZipFileContentsForJDK9(zipFileName, outputFile);

        // then
        assertTrue(new File(outputFile).exists());
    }
}
```

</details>