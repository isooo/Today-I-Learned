# 일급 객체 (First-class citizen)
해당 언어에서 어떤 개체가 다음 조건을 만족하면 일급 객체로 간주한다.      
> *아래는 자바스크립트의 function을 예로 들었다* .
- 파라미터로 전달할 수 있다.
    ```js
    var ele = function() {return 10;}
    getNumber(ele);     // ele에는 10이 아닌, 함수 자체가 인자로 전달됨
    ```
- 반환값(return value)로 사용할 수 있다.
- 변수나 데이터 구조 안에 담을 수 있다.
    ```js
    var ele = function() {
        return function() {return 10;}
    }
    var f1 = ele;
    console.log(f1()())     // 10
    ```
- 할당에 사용된 이름과 관계없이 고유한 구별이 가능하다.
    ```js 
    var ele = function f2() {retur 10;}
    ele();      // f2로 선언했지만, ele라는 이름으로 호출할 수 있다. 
    ```

<br/>

위 조건들을 만족한다면,  
그 개체를 해당 언어의 `First-class citizen` <sub>일급 객체</sub> 이라 부를 수 있다.  

<br/>

## java의 경우
java에서는 **Object**<sub>객체</sub>가 위 기능들을 수행할 수 있다.  
파라미터로 전달할 수 있고, 반환값을 담을 수 있고, 변수에 담을 수도 있다.   
즉, 객체지향 프로그래밍인 **java에서는 객체**를 **일급 시민**이라 부를 수 있다.  
  
그렇다면 java의 method는 일급 객체라 부를 수 있을까?    
예전에는 java에서 Function에 대응되는 개념이 method라고 생각했었다. 하지만 위에서 나열한 조건을 java의 method로는 풀 수 없다는 걸 알게 되었다.    
`String getName()`이라는 메서드를 특정 변수에 담으려 할 때, `getName()`의 return 값인 `String 타입의 데이터`가 아닌 `getName()`자체를 변수에 담을 수 없다.       
==> 즉 **java의 method는 일급 객체가 아니다**.   

<br/>  

## modern java
java 8은 다양한 라이브러리 제공과 함수형 프로그래밍 컨셉을 도입하였기에 패러다임이 전환되어 일명 `modern java`라고 불리고 있다.  
이 modern java부터는 java에서 함수형 프로그래밍을 할 수 있도록 설계되었다.  
즉 java 8부터는 Function을 일급 객체로 사용할 수 있게 된 것이다.   
> *`Function이 일급 객체로 사용되는 언어`를 **First-class function를 지원하는 언어**라 한다* .  

<br/>

## 장점
- 메소드의 파라미터와 바디에만 집중할 수 있다.  
- 테스트에 용이하다.  
- 코드를 읽기 쉽다.
- 사이드 이펙트가 없다.
    - 상태 값을 가지지 않으므로, 다른 변경을 일으키지 않는다.  
- OOP로 구현하는 것보다 간편하게 메소드 레벨에서 작업할 수 있다.

<br/>

## 맛보기
계산 기능이 있는 프로그램을 만들었다. 

```java
interface Calculation {
    int calculate(int x, int y);
}

class Addition implements Calculation {
    @Override
    public int calculate(final int x, final int y) {
        return x + y;
    }
}

class Subtraction implements Calculation {
    @Override
    public int calculate(final int x, final int y) {
        return x - y;
    }
}
```

```java
class CalculatorService {
    private final Calculation calculation;

    public CalculatorService(final Calculation calculation) {
        this.calculation = calculation;
    }

    public int calculator(final int x, final int y) {
        return this.calculation.calculate(x, y);
    }
}
```

위 `Calculation`에서 만약 다른 기능이 추가된다면(e.g. 곱셈, 나눗셈..), 그에 대한 구현체를 만들어줘야 한다.   
그리고 `CalculatorService`를 생성할 때 하나의 구현체만 주입할 수 있어, 덧셈과 뺄셈에 대한 기능이 필요할 땐 `CalculatorService` 인스턴스를 2개 만들어줘야 한다.  

```java
final CalculatorService calculatorService1 = new CalculatorService(new Addition());
calculatorService1.calculator(10, 20);

final CalculatorService calculatorService2 = new CalculatorService(new Subtraction());
calculatorService2.calculator(10, 20);
```
  
<br/>

이를 FP<sub>Functional Programming</sub>로 나타내면 훨씬 간편하게 구현할 수 있다.  
java 8에서는 Function은 일급 객체로 사용할 수 있도록 함수형 인터페이스를 제공하고 있다. 이를 이용하여 구현해 보자.  

```java
class FpCalculatorService {
    public int calculator(final Calculation calculation, final int x, final int y) {
        return calculation.calculate(x, y);
    }
}
```
  
이제 `FpCalculatorService`를 호출하는 쪽에서 `Calculation` 인스턴스를 넘겨주면 된다.  
```java
final FpCalculatorService fpCalculatorService = new FpCalculatorService();
fpCalculatorService.calculator((x, y) -> x - y, 10, 20);
fpCalculatorService.calculator((x, y) -> x * y, 10, 20);    
```
  
입맛에 맞게 기능을 구현하여, 해당 functional interface를 파라미터로 넘겨줄 수 있다.  
    
<br/>

==> 이렇게 함수형 프로그래밍을 사용하여 간결하고 편리하게, 그리고 테스트도 용이하게 기능을 구현할 수 있다.     

<br/>

## 참고
- [케빈 TV : 모던 자바 (자바8) 못다한 이야기 - 02 Function, The Transformer](https://www.youtube.com/watch?v=Ql9car-IjR0&list=PLRIMoAKN8c6O8_VHOyBOhzBCeN7ShyJ27&index=4)
