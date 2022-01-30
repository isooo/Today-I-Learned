# ArrayList vs ArrayList

`Arrays.asList()`를 사용하여 리스트를 생성할 경우   
이는 <sub>`java.util.ArrayList`가 아닌</sub> `java.util.Arrays.ArrayList`가 return된다.  <small>*~~왜냐면... Java is just like that...?~~*</small>  

<br/>

## :small_blue_diamond: `java.util.Arrays.ArrayList`
`java.util.Arrays.ArrayList`에 `add()`를 할 경우, `UnsupportedOperationException` 예외가 발생한다.   
`java.util.Arrays.ArrayList`에는 `add()`가 없기 때문에, 이의 부모클래스인 `AbstractList`에 구현된 `add()`가 실행된다.

```java
package java.util;

public abstract class AbstractList<E> extends AbstractCollection<E> implements List<E> {
    public boolean add(E e) {
        add(size(), e);
        return true;
    }

    public void add(int index, E element) {
        throw new UnsupportedOperationException();
    }
    ...
}
```  

> `remove()`도 마찬가지로 `AbstractList`의 `remove()`를 사용한다.   
> 이 메서드 역시 `AbstractList#remove`에서 `throw new UnsupportedOperationException`를 하기 때문에, 원래 기대했던(*요소 삭제*) 것처럼 동작하지 않음

<br/>

## :small_blue_diamond: `java.util.ArrayList`
`java.util.ArrayList`는 해당 클래스 내에서 `add()`를 오버라이드하여, 일반적으로 사용되는 **`ArrayList`에 요소추가하기** 가 가능하다.  

```java
package java.util;

public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable {
    public boolean add(E e) {
        ensureCapacityInternal(size + 1);  // Increments modCount!!
        elementData[size++] = e;
        return true;
    }

    public void add(int index, E element) {
        rangeCheckForAdd(index);

        ensureCapacityInternal(size + 1);  // Increments modCount!!
        System.arraycopy(elementData, index, elementData, index + 1,
                         size - index);
        elementData[index] = element;
        size++;
    }
    ...
}
```  


<br/>


## `asList()`를 이용하여 `ArrayList`를 생성하였을 때, `add()`를 사용할 수 있도록 하기 위한 약간의 해결책 ..?      
해당 객체를 `java.util.ArrayList`로 캐스팅해주면 된다.   
```java
String[] strArr = new String[]{"apple", "banana", "cherry"};
List<String> arr = new ArrayList<>(Arrays.asList(strArr));
arr.add("aaa");
```  

<br/><br/>  


> ## + *개인적인 궁금증*  
> `AbstractList`가 `implements List`를 하고 있음에도 불구하고, 왜 `java.util.ArrayList`에서도 `implements List`하고 있을까?..   
> <small>*~~이것도 왠지 Java is just like that...?~~*</small>  
> 
> ```java
> public abstract class AbstractList<E> extends AbstractCollection<E> implements List<E>
> ```
>   
> ```java
> public class ArrayList<E> extends AbstractList<E>
>   implements List<E>, RandomAccess, Cloneable, java.io.Serializable
> ```    
>  
