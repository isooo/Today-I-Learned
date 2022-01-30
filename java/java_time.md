# `java.time`

## 날짜, 시간 표현하기
java8 이전까지는 날짜와 시간을 표현할 때 `Calendar`나 `Date`를 사용했었다.
```java
final Date date1 = new Date();
System.out.println("현재 시각1 : " + date1);

final SimpleDateFormat sdf =
        new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA);
final String date2 = sdf.format(date1);
System.out.println("현재 시각2 : " + date2);
```  

```  
현재 시각1 : Sun Jan 20 21:05:37 KST 2019
현재 시각2 : 2019.01.20 21:05:37
```  

하지만 위 API를 사용하기엔 불편하고 귀찮으며 위험한 부분들이 존재하였다.    
<sub> *`Calendar.JANUARY`는 사실 0이라던가.. 시간을 구하고자 하여 `getTime()`했더니 Date 타입이 반환된다던가... `set()`으로 수정이 가능해진다거나....* </sub>    
이리하여 **java8**부터 새로운 API가 제공되었다 ==> `java.time` 패키지 :tada:    
  
```java
// java.time.LocalDateTime 을 사용하면, 앞서 보았던 예시를 아래와 같이 변경할 수 있다

final LocalDateTime now = LocalDateTime.now();
System.out.println(now);
System.out.println(now.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")));
```

<br/>

## :small_blue_diamond: `LocalDate`
- `년, 월, 일`과 같이 **날짜만 표현** 하는 클래스  

```java
final LocalDate nowDate = LocalDate.now();
System.out.println(nowDate);           

final LocalDate ofDate = LocalDate.of(1919, 1, 19);
System.out.println(ofDate);         

final LocalDate plusMonthDate = LocalDate.now().plusMonths(5);
System.out.println(plusMonthDate);          

final LocalDate ld1 = LocalDate.of(2018, 05, 07);
final LocalDate ld2 = LocalDate.now();
final long periodDateCount = ld2.toEpochDay() - ld1.toEpochDay();
System.out.println(periodDateCount);
```  

```  
2019-01-20
1919-01-19
2019-06-20
258
```  

<br/>

## :small_blue_diamond: `LocalTime`
- `시, 분, 초`와 같이 **시간만 표현** 하는 클래스

```java
final LocalTime lt1 = LocalTime.now();
System.out.println(lt1);     

final LocalTime lt2 = LocalTime.of(22, 10, 59);
System.out.println(lt2);      

final LocalTime lt3 = LocalTime.now().minusMinutes(100);
System.out.println(lt3);
```

```
21:54:26.098
22:10:59
20:14:26.099
```

<br/>

## :small_blue_diamond: `LocalDateTime`
- `년, 월, 일, 시, 분, 초`를 표현하는 클래스  

```java
final LocalDateTime ldt = LocalDateTime.now();
System.out.println(ldt);    

final LocalDateTime ldt2 = LocalDateTime.of(
        LocalDate.of(1919, 1, 19),
        LocalTime.of(22, 10, 20)
);
System.out.println(ldt2);   

final LocalDateTime ldt3 = LocalDateTime.now()
        .withMonth(11)
        .withDayOfMonth(4);
System.out.println(ldt3);   
```

```
2019-01-20T21:54:26.099
1919-01-19T22:10:20
2019-11-04T21:54:26.100
```

<br/>

## :small_blue_diamond: `Period`
- 두 날짜 사이의 차이를 `년, 월, 일`을 이용하여 표현  

```java
System.out.println(Period.ofDays(10));      
System.out.println(Period.ofYears(5));      

final LocalDate ld1 = LocalDate.now();
final LocalDate ld2 = ld1.plusDays(5);
final Period p1 = ld1.until(ld2);
System.out.println(p1);                    

final Period p2 = Period.between(ld1, ld2);    // until()과 달리 period()는 static 메서드
System.out.println(p2);                  
```

```
P10D  // 10일 차이
P5Y   // 5년 차이
P5D   // 5일차이
P5D
```

<br/>


## :small_blue_diamond: `Duration`  
- 두 시간 사이의 차이를 `일, 시, 분, 초`로 표현

```java
System.out.println(Duration.ofDays(1));      

final LocalDateTime ldt1 = LocalDateTime.now();
final LocalDateTime ldt2 = ldt1.plusDays(2);
final Duration d1 = Duration.between(ldt1, ldt2);
System.out.println(d1);                    

final LocalDateTime ldt3 = LocalDate.of(2018, 5, 7)
        .atTime(10, 30)
        .plus(Duration.ofHours(7));
System.out.println(ldt3);    
```  

```  
PT24H
PT48H
2018-05-07T17:30
```  

<br/>

## :small_blue_diamond: `format`
```java
final LocalDateTime now = LocalDateTime.now();
System.out.println("now : " + now);     

final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
final String format = now.format(formatter);
System.out.println("포맷 변경: " + format);
```  

```  
now : 2019-01-20T22:28:20.729
포맷 변경: 2019년 01월 20일
```  

<br/><br/>  

## 참고
- [oracle: `java.time` package summary](https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html)  
