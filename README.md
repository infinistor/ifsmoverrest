# IfsMoverRest

## 개요

### 용도
* [ifsmover](https://github.com/infinistor/ifsmover)를 구동하기 위한 REST Server.

### 전제 조건
* [IfsMover-0.2.6+](https://github.com/infinistor/ifsmover/releases) 설치되어 있어야 합니다.

### 구동 환경
* CentOS Linux release 7.5+
* JDK 11+

### 사용법
1. 최신 [Release](https://github.com/infinistor/ifsmoverrest/releases)를 다운 받은 후에 설치를 합니다.
``` bash
tar -xvzf ifsmoverRest-0.1.2.tar.gz
```

2. ./etc/ifsmoverRest.conf 파일을 환경에 맞도록 입력합니다.
``` bash
port=7123   // ifsmoverRest 가 사용할 port number
ifsmover_path=/opt/ifsmover-0.2.6   // ifsmover가 설치된 디렉토리
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
ifsmover를 실행합니다. Request의 Check에 true를 입력하면 주어진 type, source 정보, target 정보에 대한 검사 결과를 리턴합니다.

* URL : /api/Start
* Request

``` bash
{
    "UserId":"string",
    "Check": false,     // true, false
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

* test
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

* test
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

* test
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


