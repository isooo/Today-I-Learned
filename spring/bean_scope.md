# Bean scopes
- **singleton scope** <sub>**싱글턴**</sub>
- **prototype scope** <sub>**프로토타입**</sub>
  
  
Spring은 기본적으로 모든 bean을 singleton으로 생성하여 관리한다.
- 명시적으로 별도의 스코프가 지정되지 않으면, 기본적으로 모든 bean은 singleton
- Spring을 통해 제공받은 bean은 동일한 객체라는 뜻

<br/>

> ### :memo: 참고   
> *`Spring Web Application`에서만 유효한 request, session, application, websocket 등의 스코프도 있다* .   

<br/>

## :small_blue_diamond: Singleton scope
- singleton bean은 Spring IoC container에서 **한 번** 생성된다.
    - singleton scope의 bean들은 기본적으로 `ApplicationContext`가 초기 구동될 때 전부 인스턴스화된다.
- 생성된 하나의 인스턴스는 singleton beans 캐시에 저장되었다가, 해당 bean에 대한 요청과 참조가 있다면 캐시 된 객체를 반환한다.
    - 하나만 생성되기 때문에, 동일한 것을 참조하게 됨.
- `per-container and per-bean`
    - 하나의 Spring Container에서 하나의 bean을 정의하면, 해당 Spring Container는 정의된 bean에 대한 하나의 인스턴스만을 생성한다.

### 선언 방법
- xml 설정
    ```xml
    <!-- singleton이 default라 굳이 명시할 필요는 없음 -->
    <bean id="..." class="..." scope="singleton"/>
    ```
    
- annotation 설정
    ```java
    // 대상 클래스에 붙여주기
    @Scope("singletone")
    ```

<br/><br/>

## :small_blue_diamond: Prototype scope
- prototype bean은 모든 요청에게 새로운 인스턴스를 생성하여 반환한다.
- 의존 관계에서 주입될 때나 또는 `getBean()`을 통해 호출될 때, prototype scope에서는 새로운 인스턴스가 생성되어 주입된다.
    - Spring Container의 역할은 `new` 연산자를 대체한 것이라 볼 수도 있다.
- Spring은 prototype bean의 전체 라이프 사이클을 관리하지 않는다.
    - destroy에 대한 콜백은 호출되지 않음

### 선언 방법
- xml 설정
    ```xml
    <bean id="..." class="..." scope="prototype"/>
    ```
- annotation 설정
    ```java
    @Scope("prototype")
    ```
  
<br/><br/><br/><br/>  
  
# 각 스코프에 적합한 객체

<br/>

## :small_blue_diamond: singleton으로 적합한 객체
- 상태가 없는 공유 객체
    - 상태를 가지고 있지 않은 객체는 동기화 비용이 없다. 따라서 매번 이 객체를 참조하는 곳에서 새로운 객체를 생성할 이유가 없다.  
- 읽기용으로만 상태를 가진 공유 객체
    - 상태가 없는 공유 객체와 유사. 상태를 가지고 있으나 읽기 전용이므로 여전히 동기화 비용이 들지 않는다. 매 요청마다 새로운 객체 생성할 필요가 없다. 
- 공유가 필요한 상태를 지닌 공유 객체
    - 객체 간의 반드시 공유해야 할 상태를 지닌 객체가 하나 있다면, 이 경우에는 해당 상태의 쓰기를 가능한 동기화할 경우 singleton도 적합하다.
- 쓰기가 가능한 상태를 지니면서도 사용빈도가 매우 높은 객체
    - 애플리케이션 안에서 정말로 사용빈도가 높다면, 쓰기 접근에 대한 동기화 비용을 감안하고서라도 singleton을 고려할만하다.  

아래의 경우, singleton은 유용한 선택이 될 수 있다.
- 장시간에 걸쳐 매우 많은 객체가 생성될 때
- 해당 객체가 매우 작은 양의 쓰기 상태를 가지고 있을 때
- 객체 생성 비용이 매우 클 때

<br/><br/>

## :small_blue_diamond: non-singleton으로 적합한 객체
- 쓰기가 가능한 상태를 지닌 객체
    - 쓰기가 가능한 상태가 많아서 동기화 비용이 객체 생성 비용보다 크다면 singleton으로 적합하지 않다.
- 상태가 노출되지 않은 객체
    - 일부 제한적인 경우, 내부 상태를 외부에 노출하지 않는 bean을 참조하여 다른 의존 객체와는 독립적으로 작업을 수행하는 의존 객체가 있다면 singleton보다 non-singleton 객체를 사용하는 것이 더 나을 수 있다.

<br/><br/><br/><br/>  

# Singleton bean과 Prototype bean의 의존 관계

<br/>

## :small_blue_diamond: prototype이 singleton에 의존하는 경우
- 문제되지 않음
    - 항상 동일한 인스턴스만을 참조하기 때문

```java
@Component
public class SingletonScope { ... }
```    

```java
@Component
@Scope("prototype")
public class PrototypeScope {
    private final SingletonScope singletonScope;

    public PrototypeScope(final SingletonScope singletonScope) {
        this.singletonScope = singletonScope;
    }

    public SingletonScope getSingletonScope() {
        return singletonScope;
    }
}
```

```java
@Component
public class AppRunner implements ApplicationRunner {
    private final ApplicationContext ctx;
    private final PrototypeScope prototypeScope;

    public AppRunner(final ApplicationContext ctx, final PrototypeScope prototypeScope) {
        this.ctx = ctx;
        this.prototypeScope = prototypeScope;
    }

    @Override
    public void run(final ApplicationArguments args) throws Exception {
        System.out.println(prototypeScope.getSingletonScope());
        System.out.println(ctx.getBean(SingletonScope.class));
        System.out.println(prototypeScope.getSingletonScope());
        System.out.println(ctx.getBean(SingletonScope.class));
    }
}
```
  
결과:   
```
me.isooo.demo.test.SingletonScope@1db0ec27
me.isooo.demo.test.SingletonScope@1db0ec27
me.isooo.demo.test.SingletonScope@1db0ec27
me.isooo.demo.test.SingletonScope@1db0ec27
```

<br/><br/>  

## :small_blue_diamond: singleton이 prototype에 의존하는 경우
- prototype은 계속 새로운 bean이 생성되고, singleton은 한 번 생성되고 이후엔 재활용되기 때문에, singleton에서 prototype을 참조할 경우 **처음 주입된** prototype을 **계속 참조**하게 된다.   
==> *Prototype인 인스턴스가 계속 새로 생성되어도, Singleton에서 참조하고 있는 인스턴스는 초기 세팅된 인스턴스로 고정되어 버림*  

```java
@Component
public class SingletonScope {
    private final PrototypeScope prototypeScope;

    public SingletonScope(final PrototypeScope prototypeScope) {
        this.prototypeScope = prototypeScope;
    }

    public PrototypeScope getPrototypeScope() {
        return prototypeScope;
    }
}
```

```java
@Component
public class AppRunner implements ApplicationRunner {
    private final ApplicationContext ctx;
    private final SingletonScope singletonScope;
    private final PrototypeScope prototypeScope;

    public AppRunner(
            final ApplicationContext ctx,
            final SingletonScope singletonScope,
            final PrototypeScope prototypeScope
    ) {
        this.ctx = ctx;
        this.singletonScope = singletonScope;
        this.prototypeScope = prototypeScope;
    }

    @Override
    public void run(final ApplicationArguments args) throws Exception {
        System.out.println("get bean (PrototypeScope.class) : " + ctx.getBean(PrototypeScope.class));
        System.out.println(singletonScope.getPrototypeScope());
        System.out.println(ctx.getBean(SingletonScope.class).getPrototypeScope());
        System.out.println("get bean (PrototypeScope.class) : " + ctx.getBean(PrototypeScope.class));
        System.out.println(singletonScope.getPrototypeScope());
        System.out.println(ctx.getBean(SingletonScope.class).getPrototypeScope());
    }
}
```

결과:  
```
// singleton에 있는 prototype는 업데이트되지 않음

get bean (PrototypeScope.class) : me.isooo.demo.test2.PrototypeScope@7726e185
me.isooo.demo.test2.PrototypeScope@aa5455e
me.isooo.demo.test2.PrototypeScope@aa5455e
get bean (PrototypeScope.class) : me.isooo.demo.test2.PrototypeScope@282308c3
me.isooo.demo.test2.PrototypeScope@aa5455e
me.isooo.demo.test2.PrototypeScope@aa5455e
```

<br/><br/>

## :small_blue_diamond: 매번 새로운 Prototype 참조할 수 있도록 설정할 수 있는 방법: proxyMode로 설정해주기
- Prototype bean을 Proxy 객체로 감싸서, Prototype을 참조할 때 Proxy bean을 참조하게 함
- 아래와 같이 `@Scope`에 속성으로 `proxyMode = ScopedProxyMode.TARGET_CLASS` 를 설정해주면 된다.
    > `proxyMode = ScopedProxyMode.DEFAULT`는 프록시를 사용하지 않는 것.    
    - Proxy를 쓰게 되면, 해당 인스턴스를 감싸는 프록시 인스턴스가 만들어지고 그 프록시 인스턴스가 bean으로 등록된다.    
    - 즉 다른 데서 이 bean을 사용할 때 프록시 bean을 제공함

```java
@Component
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class PrototypeScope { ... }
```

```java
@Component
public class SingletonScope {
    private final PrototypeScope prototypeScope;

    public SingletonScope(final PrototypeScope prototypeScope) {
        this.prototypeScope = prototypeScope;
    }

    public PrototypeScope getPrototypeScope() {
        return prototypeScope;
    }
}
```

<br/><br/>

## :small_blue_diamond: 매번 새로운 Prototype 참조할 수 있도록 설정할 수 있는 방법: `ObjectProvider`
- Spring에서 제공하는 기능을 이용
  
> 코드 자체에 스프링 코드가 들어가게 되므로, POJO스럽지 못해 그다지 추천되진 않는 방법  
  
```java
@Component
public class SingletonScope {
    private final ObjectProvider<PrototypeScope> prototypeScope;

    public SingletonScope(final ObjectProvider<PrototypeScope> prototypeScope) {
        this.prototypeScope = prototypeScope;
    }

    public PrototypeScope getPrototypeScope() {
        return prototypeScope.getIfAvailable();
    }
}
```

<br/><br/>

## 참고
- [docs.spring.io: 1. The IoC Container : 1.5. Bean Scopes](https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#beans-factory-scopes)
- [백기선: 스프링 프레임워크 핵심 기술(인프런)](https://www.inflearn.com/course/spring-framework_core)
- [Heee's Development Blog: Spring Bean의 개념과 Bean Scope 종류](https://gmlwjd9405.github.io/2018/11/10/spring-beans.html)
  