# IfsMoverRest

## 개요

### 용도
* [ifsmover](https://github.com/infinistor/ifsmover)를 구동하기 위한 REST Server.

### 전제 조건
* [IfsMover-0.2.6+](https://github.com/infinistor/ifsmover/releases) 설치되어 있어야 합니다.

### 구동 환경
* CentOS Linux release 7.5+
* JDK 11+
* Python 2.0+

### 사용법
1. [Release](https://github.com/infinistor/ifsmoverrest/releases)를 다운 받은 후에 설치를 합니다.
``` bash
tar -xvzf ifsmoverRest-0.1.2.tar.gz
```

2. etc/ifsmoverRest.conf 파일을 환경에 맞도록 입력합니다.
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
tailf log/ifsmoverRest.log
```

### API
UserId는 임의의 문자열입니다. ifsmoverRest의 모든 동작은 UserId로 구분됩니다.
UserId는 사용자가 관리합니다.

#### Start
ifsmover를 실행합니다. Request의 Check에 true를 입력하면 주어진 type, source 정보, target 정보에 대한 검사 결과를 리턴합니다.

* URL : /api/Start
* Request

``` bash
{
    "UserId":"string",
    "Check": false,
    "Type":"string",// file, s3, swift
    "Source":{
        "mountpoint":"string",
        "endpoint":"string",
        "access":"string",
        "secret":"string",
        "bucket":"string",
        "prefix":"string",
        "move_size":"string"
    },
    "target":{
        "endpoint":"string",
        "access":"string",
        "secret":"string",
        "bucket":"string",
        "prefix":"string"
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


#### Stop
UserId와 jobId에 해당하는 작업을 중지 시킵니다.

* URL : /api/Stop/{UserId}/{JobId}
* Return

``` bash
{
    "Result":"string",// success, failed
    "Message":"string"
}
```


#### Rerun
UserId와 jobId에 해당하는 작업을 다시 수행합니다.

* URL : /api/Rerun/{UserId}/{JobId}
* Request

``` bash
{
    "Source":{
        "mountpoint":"string",
        "endpoint":"string",
        "access":"string",
        "secret":"string",
        "bucket":"string",
        "prefix":"string",
        "move_size":"string"
    },
    "target":{
        "endpoint":"string",
        "access":"string",
        "secret":"string",
        "bucket":"string",
        "prefix":"string"
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


#### Remove
UserId와 jobId에 해당하는 작업을 삭제합니다.

* URL : /api/Remove/{UserId}/{JobId}
* Return

``` bash
{
    "Result":"string",// success, failed
    "Message":"string"
}
```


#### Status
UserId에 해당하는 모든 진행 정보를 가져옵니다.

* URL : /api/Status/{UserId}
* Return

``` bash
{
    \"Result\":\"string\", // success, failed
    \"Message\":null,
    \"Items\":[
        {
            \"JobId\":0,
            \"Status\":\"string\",
            \"Source\":\"string\",
            \"Target\":\"string\",
            \"StartTime\":\"string\",
            \"EndTime\":\"string\",
            \"ErrorDesc\":\"string\",
            \"TotalCount\":0,
            \"TotalSize\":\"string\",
            \"MovedCount\":0,
            \"MovedSize\":\"string\",
            \"SkippedCount\":0,
            \"SkippedSize\":\"string\",
            \"FailedCount\":0,
            \"FailedSize\":\"string\",
            \"Progress\":\"string\"
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



