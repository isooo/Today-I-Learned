# Elasticsearch의 검색 API

> ### *[엘라스틱서치 실무 가이드 (2019)](http://www.yes24.com/Product/Goods/71893929)을 읽고 정리한 내용입니다* .     

<br/>

Elasticsearch에서 제공하는 검색 API는 기본적으로 질의(Query)를 기반으로 동작한다.  
검색 질의에는 검색하고자 하는 각종 조건들을 명시할 수 있으며, 동일한 조건을 아래 2가지 방식으로 표현할 수 있다.
- **URI 방식**
    - 루씬에서 사용하던 전통적인 방식의 URI 표기법
    - e.g. `GET movie_search/_search?q=prdtYear:2018`
- **Request Body 검색**
    - RESTful API를 이용해 body에 조건을 표기

두 가지 방법 중 `Request Body` 방식을 이용한 검색을 알아보자.    
  
### Request Body 검색
Request Body 방식은 HTTP 요청 시 body에다 검색할 컬럼과 검색어를 **json 형태**로 전달하는 방식이다. 이 방식은 복잡한 옵션을 가진 검색 쿼리를 URI 방식에 비해 깔끔한 json 형태로 넘길 수 있다는 장점이 있다.  
덧붙여 [Query DSL](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl.html)을 이용한다면 여러 질의를 조합하거나, 질의 결과에 대해 재검색을 수행하는 등 다양하게 조합하여 표현할 수 있다.   
     
Query DSL을 이용하여 Elasticsearch에 질의하는 방법을 알아보자.    

<br/>
<br/>

## Query DSL 주요 쿼리
Elasticsearch에서 제공하는 검색 관련 기능은 Query DSL을 이용해 모두 활용할 수 있다.   

<br/>

### :small_blue_diamond: **Match Query**(Full Text Query)
검색어가 분석돼야 할 경우 사용하는 쿼리.    
`Match Query`는 검색어를 형태소 분석을 통해 term으로 분리한 후, 이 term들을 이용해 검색 질의를 수행.  

```json
POST 인덱스명/_search

{
  "query": {
    "match": {
      "필드명": "검색어"
    }
  }
}
```

<br/>

### :small_blue_diamond: **Term Query**
분석 없이 검색어 그대로를 해당 필드의 값으로 가진 document검색.   

```json
POST 인덱스명/_search

{
  "query": {
    "term": {
      "필드": "검색어"
    }
  }
}
```
  
`Match Query`는 텍스트에 대해 **형태소 분석을 한 뒤 검색**을 수행하지만, `Term Query`는 **검색어를 하나의 term으로 처리**하기 때문에 **필드에 term이 정확히 존재해야** 검색된다. 따라서 영문의 경우 **대소문자가 다를 경우 검색되지 않으므로** 주의해야 한다.  

<br/>

### :small_blue_diamond: **Bool Query**
RDB에서는 `AND`나 `OR`로 묶은 여러 조건을 `WHERE`절에서 사용할 수 있다.  
이처럼 es에서도 여러 쿼리를 조합해 검색을 수행할 수 있다. 이러한 유형의 쿼리는 `Compound Query`라 하는데, `Compound Query`를 구현하기 위해 es에서는 `Bool Query`를 제공한다.  
`Bool Query`를 상위에 두고, 그 하위에 다른 Query들을 두는 구조로, 복잡한 조건의 쿼리문을 작성할 수 있다.  
`Bool Query`는 주어진 쿼리와 논리적으로 일치하는 문서를 복합적으로 검색한다. 해당 Query를 사용해 여러 형태(AND, OR, NAND, FILTER)를 표현할 수 있다.   
문법적으로 제공되는 속성은 `must`, `must_not`, `should`, `filter` 총 4가지다.  

```json
POST 필드명/_search

{
  "query": {
    "bool": {
      "must": [],       // AND 칼럼 = 조건
      "must_not": [],   // AND 칼럼 != 조건
      "should": [],     // OR 칼럼 = 조건
      "filter": []      // 칼럼 IN (조건)
    }
  }
}
```

<br/>  

> ### 책 예시 인용
> - `repGenreNm`에 `코미디`라는 키워드가 들어있으며 **&&** `repNationNm`에 `한국`이 포함되어 있으며 **&&** `typeNm`에는 `단편`을 포함하지 않는 문서를 검색    
> 
> ```json
> POST movie_search/_search
> 
> {
>   "query": {
>     "bool": {
>       "must": [
>         {
>           "term": {
>             "repGenreNm": "코미디"
>           }
>         },
>         {
>           "match": {
>             "repNationNm": "한국"
>           }
>         }
>       ],
>       "must_not": [
>         {
>           "match": {
>             "typeNm": "단편"
>           }
>         }
>       ]
>     }
>   }
> }
> ```

<br/>

### :small_blue_diamond: **Exists Query**
필드의 값이 null이거나, 필드 자체가 없는 문서 검색하고자 할 때.    
> *만약 `create_date`라는 필드의 값이 null이거나 `create_date`이라는 필드 자체가 없는 문서를 검색하고자 한다면*   

```json
POST 인덱스명/_search

{
  "query": {
    "bool": {
      "must_not": {
        "exists": {
          "field": "필드명"
        }
      }
    }
  }
}
```

<br/>

### :small_blue_diamond: **Count API**
검색된 문서의 개수만 가져오고자 할 때.

```json
POST 인덱스명/_count

{
  "query" : {
    "match" : {
      "필드명" : "검색어"
    }
  }
}
```

<br/>

### :small_blue_diamond: **정렬**
전체 문서를 대상으로, 특정 필드 기준으로 정렬하고자 할 때.  

```json
{
  "sort": {
    "필드명": {
      "order": "desc"
    }
  }
}
```

Object 구조에서도 사용 가능.    
```json
{
  "sort": {
    "필드명.필드명": { // e.g. goods.id 
      "order": "desc"
    }
  }
}
```
