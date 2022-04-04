# Elasticsearch의 aggregation
- 참고
    - [elastic > Docs > Elasticsearch Guide](https://www.elastic.co/guide/en/elasticsearch/reference/index.html)
    - [엘라스틱서치 실무 가이드 (2019)](http://www.yes24.com/Product/Goods/71893929)

<br/>

## :large_orange_diamond: aggregation이란?
SQL에서 `GROUP BY` 연산과 비슷한 기능을 Elasticsearch에서도 제공한다. 그것이 바로 집계<sub>aggregation</sub>기능이다.  
둘을 비교해보자면 아래와 같다.    
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

### :small_blue_diamond: 집계 구문의 구조
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

### :small_blue_diamond: 집계 영역(Aggregation Scope)
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

### :small_blue_diamond: 전체 데이터 대상으로 집계해보기
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

<br/>

## :large_orange_diamond: 집계 연산
Elasticsearch는 3가지 종류의 집계 연산을 제공한다
- Metric <sub>메트릭</sub>
    - 쿼리 결과로 도출된 도큐먼트 집합에 합계, 평균 등의 연산을 수행하는 집계
- Bucket <sub>버킷</sub>
    - 도큐먼트를 특정 기준(필드 값, 범위 등)에 따라 그룹화하는 집계
        - 이때 산출된 도큐먼트 그룹이 버킷
- Pipeline <sub>파이프라인</sub>
    - 집계 결과를 또 다른 집계에 사용하는 집계

<br/>

### :small_blue_diamond: Metric aggregation > Cardinality aggregation
카디널리티 집계<sub>Cardinality aggregation</sub>는 개별 값의 대략적인 개수를 계산하는 단일 값 메트릭 집계다.. :neutral_face: *~무슨 뜻인지 모르겠다~*      
쉽게 예를 들자면, 사용자 검색 로그에서 검색어 기준으로 집계하되, 특정 검색어로 검색한 사용자가 몇 명인지 ip 기반으로 알아내고자 할 때 사용할 수 있는 집계!     

아래 예시는 `create_date`필드에 from - to 조건에 해당하는 로그 데이터 중, `keyword` 필드로 집계를 하고 (100개까지 집계, 집계된 버킷의 이름은 `group_by_keyword`), `group_by_keyword` 버킷별로 `session_id` 필드가 몇 개 존재하는지 알아내는 쿼리다.  
```json
{
    "size": 0,
    "query": {
        "bool": {
            "must": [
                {
                    "range": {
                        "create_date": {
                            "from": "2022-04-03 10:00:00",
                            "to": "2022-04-04 10:45:00"
                        }
                    }
                }
            ]
        }
    },
    "aggregations": {
        "group_by_keyword": {
            "terms": {
                "field": "keyword",
                "size": 100
            },
            "aggregations": {
                "count": {
                    "cardinality": {
                        "field": "session_id"
                    }
                }
            }
        }
    }
}
```

카디널리티 집계는 근사치를 통해 집계를 수행한다. 정확한 계산을 위해 모든 다큐먼트를 대상으로 집계하는 것은 클러스터의 리소스가 너무 많이 사용되어 성능에 영향을 줄 수 있기 때문.    
정확도는 `precision_threshold` 속성으로 설정할 수 있는데, 이 속성의 기본 값은 `3_000`, 최대 `40_000`까지 설정할 수 있다. 일반적으로 `cardinality`가 `precision`보다 작다면 거의 100% 정확한 값을 집계해 준다.  
   
> *카디널리티 집계는 `HyperLogLog++` 알고리즘을 기반으로 동작한다. 자세한 것은 
[공식 문서](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-metrics-cardinality-aggregation.html#_counts_are_approximate)를 읽어보자* :sunglasses:
