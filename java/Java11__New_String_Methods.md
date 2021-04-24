# Java11에 추가된 기능: New String Methods
- [`isBlank()`](#isBlank())
- [`lines()`](#lines())
- [`repeat​(int count)`](#repeat​(int-count))
- [`strip()`](#strip())
- [`stripLeading()`](#stripLeading())
- [`stripTrailing()`](#stripTrailing())

<br/>

## `isBlank()`
Returns `true` if the string is **empty** or contains **only white space codepoints**, otherwise `false`.

```java
// All cases are true

"".isBlank(); 
"  ".isBlank();
" \t ".isBlank();
```

<br/>

## `lines()`
Returns a **stream** of lines extracted from this `string`, separated by **line terminators**.     
A **line terminator** is one of the following:   
- `\n` (U+000A) 
- `\r` (U+000D)
- `\r\n` (U+000D U+000A)

```java
final int size = str.lines()
        .peek(System.out::println)
        .collect(Collectors.toList())
        .size();
System.out.println(size == 5);
```
  
```
ab c
d	e
f    g

h
true
```
  
<br/>

## `repeat​(int count)`
Returns a `string` whose value is the concatenation of this string repeated count times.   
If this string is empty or count is zero then the empty string is returned.

```java
final String str = "a  b";
System.out.println("--" + str.repeat(3) + "--");
System.out.println("--" + str.repeat(0) + "--");
```

```
--a  ba  ba  b--
----
```

<br/>

## `strip()`
Returns a `string` whose value is this string, with **all leading and trailing white space removed**.   
If this String object represents an empty string, or if all code points in this string are white space, then an empty string is returned.   
Otherwise, returns a substring of this string beginning with the first code point that is not a white space up to and including the last code point that is not a white space.   
This method may be used to **strip white space from the beginning and end** of a `string`.    
  
> ### `strip()`과 `trim()`의 차이
> - `trim()`은 unicode로 표현된 공백을 인식하지 못하였으나, `strip()`은 인식 가능!
> 

```java
System.out.println("\n\t a b  \u2000".trim().length());
System.out.println("\n\t a b  \u2000".strip().length());
```

```
3
6
```

> - 참고: [`\u2000`](https://en.wikipedia.org/wiki/Whitespace_character#Unicode)은 white space를 나타내는 유니코드다. 

<br/>

## `stripLeading()`
Returns a `string` whose value is this string, with **all leading white space removed**.
If this String object represents an empty string, or if all code points in this string are white space, then an empty string is returned.   
Otherwise, returns a substring of this string beginning with the first code point that is not a white space up to and including the last code point of this string.    
This method may be used to **trim white space from the beginning** of a `string`.  

```java
System.out.println("\n\t a b  \u2000".stripLeading() + "-");
```

```
a b   -
```

<br/>

## `stripTrailing()`
Returns a `string` whose value is this string, with **all trailing white space removed**.
If this String object represents an empty string, or if all characters in this string are white space, then an empty string is returned.
Otherwise, returns a substring of this string beginning with the first code point of this string up to and including the last code point that is not a white space.
This method may be used to **trim white space from the end** of a `string`.

```java
System.out.println("\n\t a b  \u2000".stripTrailing() + "-");
```

```
	 a b-
```
