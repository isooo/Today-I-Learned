# Elasticsearch의 aggregation

> ### *[엘라스틱서치 실무 가이드 (2019)](http://www.yes24.com/Product/Goods/71893929)을 읽고 정리한 내용입니다* .     

<br/>

SQL에서 `GROUP BY` 연산과 비슷한 기능을 Elasticsearch에서도 제공한다.  
바로 집계(aggregation)기능이다. 둘을 비교해보자면 아래와 같다.  
- **SQL의 `GROUP BY` 구문을 이용한 집계**  
    ```sql
    SELECT SUM(ratings) FROM movie_review GROUP BY movie_no;
    ```
- **Elasticsearch의 Query DSL로 집계하는 쿼리**  
    ```json
    {
        "aggs" : {
            "movie_no_agg" : {
                "terms" : {
                    "field" : "movie_no"
                },
                "aggs" : {
                    "ratings_agg" : {
                        "sum" : {
                            "field" : "ratings"
                        }
                    }
                }
            }
        }
    }
    ```

<br/>

## 집계 구문의 구조
기본적인 집계 구문의 구조를 알아보자.  
```json
"aggregation" : {
    "집계그룹이름1" : {
        "집계타입" : {
            "body"
        }
        [,"meta" : { ["meta_data_body"] }]?
        [,"aggregations" : { ["sub_aggregation"]+ }]?
    }
    [,"집계그룹이름2" : { ... }]*
}
```

<br/>

## 집계 영역(Aggregation Scope)
포스팅 처음에 등장한 예시에선 집계와 함께 질의(query)를 사용했다. 집계와 질의를 함께 수행하면 질의의 결과 영역 안에서 집계가 수행된다. 즉, 질의를 통해 반환된 문서들에 한해서 집계를 수행한다.    

아래는 예시 쿼리다.  
```json
{
    "query" : {                     // 1
        "constant_score" : {
            "filter" : {
                "match" : "필드 조건"
            }
        }
    },
    "aggs" : {                      // 2
        "집계 이름" : {
            "집계 타입" : {
                "field" : "필드명"
            }
        }
    }
}
```

- **1: query**
    - 질의를 수행한다. 하위에 필터 조건에 의해 명시한 필드와 값이 일치하는 문서만 반환한다.  
- **2: aggs**
    - 질의를 통해 반환받은 문서들의 집한 내에서 집계를 수행한다.  

<br/>

## 전체 데이터 대상으로 집계해보기
아래는 `movie_nm` 필드를 기준으로 집계한 예다. 
```json
POST 인덱스명/_search

{
    "aggs" : {
        "movie_names" : {
            "terms" : {
                "field" : "movie_nm"
            }
        }
    }
}
```
