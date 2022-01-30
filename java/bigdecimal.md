# BigDecimal  

<br/>

## 배경 설명  
당연히 `0.9`가 나올 것이라 예상했던 코드가 있었다.     
```java
System.out.println(2.0 - 1.1);
```
위 코드를 실행시킨 결과는 <sub>~~*놀랍게도*~~</sub> `0.8999999999999999`였다. :flushed:     

위와 같은 결과로 나오는 이유는 정확히 `0.9`라는 값은 이진법으로 나타낼 수 없고 근사치로 표현할 수 있기 때문이다.    
  
그렇다면 예상과 동일한 결과를 도출하기 위해서는 어떻게 해야 할까?  

<br/>

## `BigDecimal`을 쓰자!  
자바에서는 `BigDecimal`이라는 클래스를 제공하고 있다.  
```java
package java.math;
public class BigDecimal extends Number implements Comparable<BigDecimal> { ... }
```

이 클래스를 이용해 앞서 살펴봤던 예시를 다시 계산해 보자.  
```java
final BigDecimal b1 = new BigDecimal("2.0");
final BigDecimal b2 = new BigDecimal("1.1");

System.out.println(b1.subtract(b2)); // 0.9
```  

이처럼 부동소수점에 대한 정확한 계산을 할 수 있다.    
  
물론 위의 예시 외에도 다양한 api가 제공된다.    
```java
System.out.println(b1.min(b2));     // 1.1
System.out.println(b1.max(b2));     // 2.0
System.out.println(b1.divide(b2, RoundingMode.FLOOR));      // 1.8
System.out.println(b1.divide(b2, 4, RoundingMode.FLOOR));   // 1.8181
System.out.println(b1.pow(3));      // 8.000
```  
