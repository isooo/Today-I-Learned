# window cli 명령어

<br/>

## :small_blue_diamond: 동작 중인 프로세스 확인: `tasklist`
```bash
C:\Users\helloworld>tasklist

이미지 이름        ID    세션 이름    세션#    메모리 사용
========================= ======== ================ =========== ============
System Idle Process   0     Services     0      8 K
```

좀 더 상세히 알고 싶다면 `tasklit /V`   
- e.g. 실행 중인 `mysql`을 확인하기 위해, `sql`문자를 포함하고 있는 프로세스를 찾아본다
    ```bash
    C:\Users\helloworld>tasklist /V | findstr sql 
    ```
