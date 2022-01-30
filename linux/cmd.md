# linux cli 명령어

<br/>

## :small_blue_diamond: vi 명령어
- 현재 커서가 위치한 글자 삭제: `x` (소문자)
    - == Del
- 현재 커서 위치의 앞 글자 삭제: `X` (대문자)
    - == Backspace
- 현재 커서의 행 삭제: `dd`
    - `dd` 앞에 숫자입력했을 때, 숫자만큼의 행 삭제
- 현재 커서가 있는 행 복사: `yy`
    - `yy` 앞에 숫자 입력했을 때, 숫자만큼 행 복사
- 복사한 내용을 현재 행 이후에 붙여넣기: `p` (소문자)
- 복사한 내용을 현재 행 이전에 붙여넣기: `P` (대문자)
- 다음 페이지: `ctrl`+ `f`
- 이전 페이지: `ctrl`+ `b`
- 키워드 찾기: `/` + `키워드`
    - e.g. `/CATALINA_OUT`
    - 계속 다음 찾기를 하려면 **`/` + enter**
- 페이지 마지막으로 가기: `shift` + `g`

<br/>

## :small_blue_diamond: 리눅스 종료
- `shutdown –P now`
- `halt –p`
- `init 0`

<br/>

## :small_blue_diamond: 리눅스 재시작
- `shutdown -r now`
- `reboot`
- `init 6`

<br/>

## :small_blue_diamond: 파일 또는 파일 내 검색: `find`
- 특정 폴더 이하 파일 개수 세기
    - `find /폴더/경로 -type f | wc -l`
        - e.g. `find . -type f | wc -l`

- 파일명으로 검색: `find 찾을위치 -name "파일명"`
    - e.g. 파일명이 `txt`로 끝나는 파일을, 현재 폴더부터 하위 폴더를 포함하여 찾아줘
        ```bash
        $ find ./ -name "*txt"
        ./test111.txt
        ./test1120.txt
        ```

- 만약 모든 폴더에서 찾고 싶다면 `find / -name "파일명"`   

<br/>

## :small_blue_diamond: 텍스트 검색: `grep`
```bash
$ grep [options] [pattern] file
```

- `[options]`
    - `-c` : 패턴이 일치하는 행의 수를 출력
    - `i` : 비교시 대소문자 구별을 안함
    - `-v` : 지정한 패턴과 일치하지 않는 행만 출력
    - `-n` : 행의 번호와 함께 출력
    - `-l` : 패턴이 포함된 파일의 이름을 출력
    - `-w` : 패턴이 전체 단어와 일치하는 행만 출력

### 파일 내 문자열 검색: `grep -r "찾을문자열" 찾을위치`
- e.g. 현재 폴더부터 하위 모든 폴더 내 파일에서 `test`라는 문자열을 찾아줘
	```bash
    $ grep -r "test" ./*
    ./test111.txt:for test
    ./test171120.txt:for test 20171120 Mon
	```

- `-n` 옵션을 주면, 해당 문자열이 몇 번째 라인에 있는지도 출력할 수 있다
    - e.g. 현재 폴더에 있는 `test111.txt` 에서 `test`라는 문자열이 몇 번째 라인에 있는지 찾아줘
        ```bash
        $ grep -r -n "test" ./test111.txt
        1:for test
        ```	

<br/>

## :small_blue_diamond: 텍스트 파일 내용을 앞/뒤 일부분 출력: `head`, `tail`
```bash
$ head/tail [-라인 수] filename
```

- 라인 수를 옵션으로 주지 않으면 기본적으로 10줄씩 출력한다.
- e.g. `a.log` 파일의 맨 뒷부분을 실시간으로 출력해줘
    - `tail -f a.log`
        - `-f` 옵션을 주면 실시간으로 계속해서 출력하여 보여준다. 주로 서버 로그 볼 때 사용했음

<br/>

## :small_blue_diamond: 파일 소유권 변경: `chown`
- e.g. `a.dir` 하위 모든 파일 및 폴더의 소유자를 `isoo`로 지정
    - `chown -R isoo aaa.dir`

<br/>

## :small_blue_diamond: 파일이나 디렉토리의 파일시스템 모드 변경: `chmod`
- e.g. 현재 폴더 내 `sh`확장자를 가진 모든 파일을 755로 바꿈
    - `chmod 755 *.sh`
    > #### `755` 권한이란     
    > 소유자만 모든 것(쓰기, 읽기, 실행)이 가능하고 그 외 사용자의 경우는 읽기, 실행은 가능하나 쓰기는 불가능  

<br/>

## :small_blue_diamond: 네트워크 인터페이스 설정 확인: `ifconfig`
- IP 주소, 맥주소,  넷마스크, MTU 설정 등 확인 가능
- `[options]`
    - `-a` : 모든 네트워크 정보를 볼 수 있음
    - `up` : 지정한 인터페이스를 활성화
    - `down` : 지정한 인터페이스 비활성화
    - `netmask addr` : 넷마스크 설정
    - `address` : 인터페이스에 IP주소를 설정

<br/>

## :small_blue_diamond: 네트워크 인터페이스 통계 정보: `netstat`
> network status

```bash
$ netstat [options]
```

- 네트워크 접속, 라우팅 테이블, 네트워크 인터페이스의 통계 정보를 보여준다.
- `[options]`
    - `-a` (all) : 모든 소켓 보기, 이 옵션이 없으면 Established 상태만 출력된다.
    - `-c` (continuous) : 실행명령을 매 초마다 실행
    - `-e` (extend) : 확장 정보 추가, User부분과 Inode열이 추가된다.
    - `-l` (listening) : Listening상태인 소켓 리스트만 보여준다.
    - `-n` (numeric) : 도메인 주소를 읽지않고 숫자로 출력
    - `-p` (program) : PID와 사용중인 프로그램명이 출력
    - `-r` (route) : 라우팅 테이블 보기
    - `-t` (tcp)
    - `-u` (udp)
    
- e.g. `netstat -tnlp`    

<br/>

## :small_blue_diamond: 네트워크 경로추적
> ### tracert 
> - Windows기반 운영체제의 네트워크 경로추적 명령어
> ### traceroute
> - Linux기반 운영체제의 네트워크 경로추적 명령어

```bash
$ traceroute 서버주소
```

- 네트워크를 통해 목적지에 도달하는 경로를 수집
- centOS7은 최초 설치시 `traceroute`이 없으므로 설치해줘야 함
    - 설치방법
        ```bash
        $ yum install traceroute
        ```

<br/>

## :small_blue_diamond: 시스템 정보 출력: `uname`
```bash
$ uname [options]
```

- 시스템 정보 출력
- [options]
    - `-a`: 아래의 모든 옵션에 대한 정보를 출력
    - `-m` : 시스템의 하드웨어타입정보 출력
    - `-n` : 네트웍노드 호스트이름에 대한 정보 출력
    - `-p` : 프로세스 정보를 출력
    - `-r` : 운영체제의 배포버전을 출력
    - `-s` : 커널이름을 출력
    - `-v` : 커널 버전정보를 출력

<br/>

## :small_blue_diamond: 현재 디렉토리에서 서브 디렉토리까지 디스크 사용량 확인: `du`
> disk usage  

``` bash
$ du [options] file
```

- `[options]`
    - `-a` : 모든 파일들의 기본정보를 보여준다.
    - `-b` : 표시단위를 Byte로 한다.
    - `-k` : 표시단위를 Kb로 한다.
    - `-h` : 알아보기 쉽게 출력 (human readable)
    - `-c` : 모든 파일의 정보를 보여주고나서 최종 합계를 표시한다.
    - `-s` : 총사용량만 표시한다.
    - `-sh *` : 한단계 서브디렉토리 기준으로 보여준다.

<br/>

## :small_blue_diamond: 리눅스 시스템의 디스크 사용량 확인: `df`
> disk free

```bash
$ dk [options] file
```

- 리눅스 전체 파일 시스템의 사용량을 확인할 수 있다
- `[options]`
    - `-h` : 알아보기 쉽게 출력 (human readable)
    - `-k` : 킬로바이트 단위로 현재 남은 용량 확인
    - `-m` : 메가바이트 단위로 남은 용량 확인

<br/>

## :small_blue_diamond: 파일을 복사: `cp`
```bash
$ cp [options] [원본] [사본]
```

- 파일을 **복사**하는 명령어
- `[options]`
    - `-a` : 원본 파일의 속성, 링크 정보들을 그대로 유지하면서 복사한다.
    - `-b` : 복사할 대상이 이미 있을 경우 기존 파일을 백업하고 복사한다.
    - `-d` : 만약 복사할 원본이 심볼릭 링크일 때 심볼릭 자체를 복사한다.
    - `-f` : 만약 복사할 대상이 이미 있으면 강제로 지우고 복사한다.
    - `-i` : 만약 복사할 대상이 이미 있으면 사용자에게 물어본다.
    - `-l` : 하드링크 형식으로 복사한다.
    - `-P` (대문자 P) : 원본 파일 지정을 경로와 같이했을 경우 그 경로 그대로 복사된다.
    - `-p` (소문자 p) : 파일의 소유자, 그룹, 권한, 시간 정보들이 그대로 보존되어 복사된다.
    - `-r` : 원본이 파일이면 그냥 복사되고, 디렉터리라면 디렉터리 전체가 복사된다.
    - `-s` : 파일을 심볼릭 링크 형식으로 복사한다. 원본 파일이름을 절대 경로로 지정해야 한다.
    - `-u` : 복사할 대상이 있을 때 이 파일의 변경 날짜가 같거나 더 최근의 것이면 복사하지 않는다.
    - `-v` : 복사 상태를 보여준다.
    - `-x` : 원본과 대상 파일의 파일시스템이 다를 경우에는 복사하지 않는다.
    - `-R` : 디렉터리를 복사할 경우 그 안에 포함된 모든 하위경로와 파일들을 모두 복사한다.

<br/>

## :small_blue_diamond: 파일을 이동: `mv`
```bash
$ mv [options] [원본] [이동경로]
```
- `cp`는 파일을 **복사**, `mv`는 파일 **이동**인데, `mv`는 이동 시 **원본 파일이 삭제된다**는게 차이점이다.
    > 파일이름을 변경할 때도 `mv` 를 쓰면 편리함
- `[options]`
    - `-b` : 이동 경로에 같은 이름의 파일이나 디렉터리가 존재하면 기존 파일을 백업한 뒤에 이동한다.
    - `-f` : 이동 경로에 같은 이름의 파일이나 디렉터리가 존재하면 덮어쓸 때 묻지 않고 바로 덮어쓴다.
    - `-i` : 이동 경로에 같은 이름의 파일이나 디렉터리가 존재하면 덮어쓸 때 물어본다.
    - `-v` : 이동 상태를 표시한다.

<br/><br/>

---

<br/><br/>

## 참고
- [ThroughKim : Linux 공부 14 - 개념 정리](http://throughkim.kr/2017/01/09/linux-14)
