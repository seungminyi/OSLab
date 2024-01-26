## 개요

개인적인 학습과 실험을 위한 프로젝트 입니다.
운영 체제의 싱글 스레드, 멀티 스레드, 멀티 프로세스의 성능을 비교 분석하는 것을 목표로하며
적당한 연산량과 병렬처리가 가능한 K-means 클러스터링 알고리즘을 선택했습니다. 

## 설치 및 사용법
0. JDK17 버전을 필요로 합니다.


1. 프로젝트 복제
```bash
git clone 
```
2. 빌드
```bash
./gradlew build
```
3. 실행
```agsl
java -jar build/libs/os-lab.jar [option]
```


## 사용 방법
프로그램은 커맨드 라인 인수를 통해 실행 모드와 필요한 설정을 지정할 수 있습니다. 다음은 사용 가능한 명령어 옵션들입니다:

`-mode`: 실행 모드를 지정합니다 (dataGenerate, single, multiThread, multiProcess).

`-dataPath`: 데이터 파일의 경로를 지정합니다.

`-clusters`: 클러스터의 수를 지정합니다.

`-threads`: 멀티 스레드 모드에서 사용할 스레드의 수를 지정합니다.

`-processes`: 멀티 프로세스 모드에서 사용할 프로세스의 수를 지정합니다.

`-dataPoints`: 생성할 데이터 포인트의 수를 지정합니다 (데이터 생성 모드에서만 사용).

`-iterations`: 알고리즘의 반복 횟수를 지정합니다.

예를 들어, 다음 명령어는 싱글 스레드 모드로 프로그램을 실행하며, 5개의 클러스터와 10번의 반복으로 설정합니다:

```bash
java -jar os-lab.jar -mode single -dataPath "./data.csv" -clusters 5 -iterations 10
```

데이터 생성
데이터 생성 모드 (dataGenerate)를 사용하여 테스트에 필요한 데이터를 생성할 수 있습니다. 데이터 포인트의 수와 파일 경로를 지정하여 실행하면, 지정된 수의 무작위 데이터 포인트가 생성됩니다.

예:
```bash
java -jar os-lab.jar -mode dataGenerate -dataPoints 1000 -dataPath "./data.csv"
```

성능 비교
싱글 스레드, 멀티 스레드, 멀티 프로세스 모드를 차례로 실행하여 각 모드에서의 수행 시간을 기록하고 비교합니다. 이를 통해 다양한 실행 환경에서의 K-means 알고리즘의 성능을 분석할 수 있습니다.

## 결과 및 리포트
결과 및 리포트는 [프로세스와 스레드의 비교 분석: k-means 알고리즘을 활용한 성능 평가](https://seungminyi.tistory.com/1)에서 확인할 수 있습니다
