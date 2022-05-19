# IfsMoverRest

## 개요

### 용도
* [IfsMover](https://github.com/infinistor/ifsmover)를 구동하기 위한 REST Server.

### 전제 조건
* [IfsMover-0.2.6+](https://github.com/infinistor/ifsmover/releases) 설치되어 있어야 합니다.

### 구동 환경
* CentOS Linux release 7.5+
* JDK 11+

### Quick Start
1. 최신 [Release](https://github.com/infinistor/ifsmoverrest/releases)를 다운 받은 후에 설치를 합니다.
``` bash
tar -xvzf ifsmoverRest-0.1.2.tar.gz
```

2. ./etc/ifsmoverRest.conf 파일을 환경에 맞도록 입력합니다.
``` bash
port=7123   // ifsmoverRest 가 사용할 port number
ifsmover_path=/user/local/pspace/bin/ifsmover-0.2.6   // ifsmover가 설치된 디렉토리
```

3. ifsmoverRest.sh 를 실행 시킵니다.
``` bash
./ifsmoverRest.sh 
```

* log 파일 (설정:etc/ifsmoverRestLog.xml)
``` bash
tailf ./log/ifsmoverRest.log
```

### API
* UserId는 임의의 문자열입니다. 
* ifsmoverRest의 모든 동작은 UserId로 구분됩니다.
* UserId는 사용자가 관리합니다.

#### Start
ifsmover를 실행합니다. -check 옵션으로 먼저 수행한 후에 에러가 없으면 job을 수행하고 그렇지 않으면 failed를 리턴합니다.

* URL : /api/Start
* Request

``` bash
{
    "UserId":"string",
    "Type":"string",    // file, s3, swift
    "Source":{
        "Mountpoint":"string",
        "Endpoint":"string",
        "Access":"string",
        "Secret":"string",
        "Bucket":"string",
        "Prefix":"string",
        "Move_size":"string"
    },
    "Target":{
        "Endpoint":"string",
        "Access":"string",
        "Secret":"string",
        "Bucket":"string",
        "Prefix":"string"
    }
}
```

* Return

``` bash
{
    "Result":"string",// success, failed
    "Message":"string"
}
```

* Test
``` bash
curl -H "Content-Type:application/json" http://localhost:7123/api/Start -d "{\"UserId\":\"1234\", \"Check\":true, \"Type\":\"s3\", \"Source\":{\"Mountpoint\":null, \"Endpoint\":\"http://localhost:8080\", \"Access\":\"your_access_key\", \"Secret\":\"your_secret_key\", \"Bucket\":\"mover-test-source\", \"Prefix\":null, \"Move_size\":null}, \"Target\":{\"Endpoint\":\"http://localhost:8080\", \"Access\":\"your_access_key\", \"Secret\":\"your_secret_key\", \"Bucket\":\"mover-test-target-01\", \"Prefix\":\"05-18-001\"}}"
```


#### Stop
UserId와 JobId에 해당하는 작업을 중지 시킵니다.

* URL : /api/Stop/{UserId}/{JobId}
* Return

``` bash
{
    "Result":"string",// success, failed
    "Message":"string"
}
```

* test
``` bash
curl http://localhost:7123/api/Stop/1234/1
```


#### Rerun
UserId와 JobId에 해당하는 작업을 다시 수행합니다.

* URL : /api/Rerun/{UserId}/{JobId}
* Request

``` bash
{
    "Source":{
        "Mountpoint":"string",
        "Endpoint":"string",
        "Access":"string",
        "Secret":"string",
        "Bucket":"string",
        "Prefix":"string",
        "Move_size":"string"
    },
    "Target":{
        "Endpoint":"string",
        "Access":"string",
        "Secret":"string",
        "Bucket":"string",
        "Prefix":"string"
    }
}
```

* Return

``` bash
{
    "Result":"string",// success, failed
    "Message":"string"
}
```

* Test
``` bash
curl -H "Content-Type:application/json" http://localhost:7123/api/Rerun/1234/1 -d "{\"Source\":{\"Mountpoint\":null, \"Endpoint\":\"http://localhost:8080\", \"Access\":\"your_access_key\", \"Secret\":\"your_secret_key\", \"Bucket\":\"mover-test-source\", \"Prefix\":null, \"Move_size\":null}, \"Target\":{\"Endpoint\":\"http://192.168.13.13:9090\", \"Access\":\"your_access_key\", \"Secret\":\"your_secret_key\", \"Bucket\":\"mover-test-target-01\", \"Prefix\":\"05-18-001\"}}"
```


#### Remove
UserId와 JobId에 해당하는 작업을 삭제합니다. 진행 중인 작업은 삭제할 수 없습니다. 먼저 Stop을 한 후에 Remove를 해야 합니다.

* URL : /api/Remove/{UserId}/{JobId}
* Return

``` bash
{
    "Result":"string",// success, failed
    "Message":"string"
}
```

* Test
``` bash
curl http://localhost:7123/api/Remove/1234/1
```


#### Status
UserId에 해당하는 모든 Job에 대한 진행 정보를 가져옵니다.

* URL : /api/Status/{UserId}
* Return

``` bash
{
    "Result":"string", // success, failed
    "Message":null,
    "Items":[
        {
            "JobId":0,
            "Status":"string",
            "Source":"string",
            "Target":"string",
            "StartTime":"string",
            "EndTime":"string",
            "ErrorDesc":"string",
            "TotalCount":0,
            "TotalSize":"string",
            "MovedCount":0,
            "MovedSize":"string",
            "SkippedCount":0,
            "SkippedSize":"string",
            "FailedCount":0,
            "FailedSize":"string",
            "Progress":"string"
        },
    ]
}
// Status
    INIT
    MOVE
    COMPLETE
    STOP
    RERUN INIT
    RERUN MOVE
    ERROR
```

* Test
``` bash
curl http://localhost:7123/api/Status/1234
```


## How to Get Started
<kbd>git clone https://github.com/infinistor/ifsmoverrest.git</kbd>

## How to Build

### Maven 설치
* Maven이 설치되어 있는지 확인해야 합니다.

* <kbd>mvn -v</kbd> 로 설치되어 있는지 확인하세요.

* 설치가 되어 있지 않으면 다음 명령어로 설치를 해야 합니다. <br> 
<kbd>sudo apt install maven</kbd>

### Build

* pom.xml 파일이 있는 위치에서 <kbd>mvn package</kbd> 명령어를 입력하시면 빌드가 되고, 빌드가 완료되면 target이라는 폴더에 ifsmoverRest.jar가 생성됩니다.


## How to Use (빌드한 경우)

* ifsmoverRest를 실행시키기 위하여 필요한 파일은 4개입니다.
 * target/ifsmoverRest.jar - 소스 빌드 후, 생성된 실행 파일	
 * script/ifsmoverRest.sh - ifsmoverRest.jar를 실행시켜 주는 스크립트
 * script/ifsmoverRest.conf - ifsmoverRest.jar의 config 파일
 * script/ifsmoverRestLog.xml - log 관련 설정

* 4개의 파일을 실행시킬 위치에 복사합니다.
 * target/ifsmoverRest.jar -> 실행시킬 위치/lib/ifsmoverRest.jar
 * script/ifsmoverRest.sh -> 실행시킬 위치/lib/ifsmoverRest.sh
 * script/ifsmoverRest.conf -> 실행시킬 위치/etc/ifsmoverRest.conf
 * script/ifsmoverRestLog.xml -> 실행시킬 위치/etc/ifsmoverRestLog.xml

* ifsmoverRest.sh의 실행 권한을 확인합니다.
 * ifsmoverRest.sh의 실행 권한이 없는 경우 실행권한을 부여합니다. <br>
 <kbd>chmod +x ifsmoverRest.sh</kbd>
 
* 실행시킬 위치/etc/ifsmoverRest.conf에 ifsmoverRest 설정 정보를 입력합니다.

```bash
port=7123   // ifsmoverRest Server가 사용할 port number
ifsmover_path=/usr/local/pspace/bin/ifsmover-0.2.6  // ifsmover가 설지된 디렉토리
```

* ifsmoverRest.sh를 실행합니다. <br>
<kbd>./ifsmoverRest.sh</kbd>

* 더 자세한 실행 방법은 본 문서의 "실행 예시", "설정 파일 예시"를 참조하세요.

## How to Use (배포판의 경우)

* 아래 배포판 페이지의 "Asset" 항목을 펼쳐서 ifsmoverRest-x.x.x.tar.gz 파일 링크를 미리 복사하세요.
  * 배포판 페이지 : https://github.com/infinistor/ifsmoverrest/releases

* 배포판을 다운로드하고 압축을 풀어 설치합니다.

```bash
# mkdir /usr/local/pspace
# mkdir /usr/local/pspace/bin
# cd /usr/local/pspace/bin
# wget "https://github.com/infinistor/ifsmover/releases/download/v0.x.x/ifsmoverRest-0.x.x.tar.gz"
# tar -xvf ifsmoverRest-0.x.x.tar.gz
# mv ifsmoverRest-0.x.x ifsmoverRest
```

* 설치 경로로 이동합니다. <br>
<kbd> cd /usr/local/pspace/bin/ifsmoverRest </kbd>

* /usr/local/pspace/bin/ifsmoverRest/etc/ifsmoverRest.conf에 ifsmoverRest 설정 정보를 입력합니다.

```bash
port=7123   // ifsmoverRest Server가 사용할 port number
ifsmover_path=/usr/local/pspace/bin/ifsmover-0.2.6  // ifsmover가 설지된 디렉토리
```

* ifsmoverRest.sh를 실행합니다. <br>
<kbd>./ifsmoverRest.sh</kbd>
