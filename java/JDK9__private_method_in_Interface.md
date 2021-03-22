# Java9에 추가된 기능: Support for Private Interface Methods
JDK8에서는 인터페이스가 default method를 가질 수 있었다.  
**JDK9**부터는 인터페이스가 **private** method도 가질 수 있다!  

> Private interface methods are supported. This support allows nonabstract methods of an interface to share code between them.
> - [*ORACLE Java Documentation: Java Language Updates: Java SE 9: Support for Private Interface Methods*](https://docs.oracle.com/en/java/javase/16/language/java-language-changes.html#GUID-905DDDF7-E63A-40BD-84E6-8A08C926E1C7)

한 인터페이스 내 여러 defualt method가 공통으로 사용할 수 있는 로직을 이제 private method에 정의해두고 쓸 수 있겠다.  

```java
interface MyInterface {
    void saySomething();
    default void sayHello() {
        System.out.println("hello");
    }
    default void sayBye() {
        myPrivate();
        System.out.println("bye");
    }

    private void myPrivate() {
        myPrivate();
        System.out.println("private");
    }
}

class MyClass implements MyInterface {
    @Override
    public void saySomething() {
        System.out.println("something");
    }
}
```

<br/>

# 참고
- [ORACLE Java Documentation: Java Language Updates: Java SE 9](https://docs.oracle.com/en/java/javase/16/language/java-language-changes.html#GUID-B06D7006-D9F4-42F8-AD21-BF861747EDCF)
- [ORACLE Java Documentation: The Java Tutorials: Default Methods](https://docs.oracle.com/javase/tutorial/java/IandI/defaultmethods.html)