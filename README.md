# Assembler_Simulator


## 과제 내용	

### 어셈블러	
- ControlSection 방식의 SIC/XE 소스를 Object Program Code로 바꾸는 어셈블러
- SIC/XE 소스를 라인별로 처리해서 Object Code로 바꾼 후, Object Program Code로 변환하는 프로그램 

### 시뮬레이터	
-	ControlSection 방식으로 생성된 ObjectCode(어셈블러의 결과물)를 실행하고 시뮬레이션할 수 있는 시뮬레이터
-	시뮬레이션 과정이 Step-by-Step으로 Visual하게 보여주는 Java GUI 프로그램
-	GUI를 위한 모듈, 연산 모듈, 가상 장치(메모리, 레지스터) 모듈, 로더를 통하여 시뮬레이터 구현


## 과제 목적	

### 어셈블러	
-	SIC/XE 소스를 Object Program Code로 변환해봄으로써 SIC/XE 어셈블러의 동작을 이해
-	SIC/XE 소스를 Object Program Code로 변환하는 과정을 이해하고 이 후 확장되는 과제 내용에 맞추어 프로그램의 확장성을 효과적으로 증진시키기 위한 기본 지식을 학습
-	C와 자바 코드 비교
	
### 시뮬레이터	
-	ControlSection 방식으로 생성된 ObjectCode를 입력으로 삼아 실제 코드가 동작하는 방식을 시뮬레이션할 수 있는 GUI Java 프로그램


## 개발 배경 및 목적

-	SIC머신과 SIC/XE머신에 대한 이론을 배우게 되었고, 소스코드를 보고 object code를 작성할 수 있게 되었습니다. 배운 이론을 바탕으로 SIC/XE 소스를 Object 	Program Code로 변환해봄으로써 SIC/XE 어셈블러의 동작을 이해할 수 있도록 Input으로 ControlSection 방식의 SIC/XE 소스를 주면 Output으로 Object Program Code를 출력하는 어셈블러를 C와 자바로 구현하고자 합니다. 어셈블러를 구현하려면 Input으로 들어온 SIC/XE 소스 분석을 가장 먼저 해야 합니다. SIC/XE 소스를 라인별로 처리해서 Object Code로 바꾼 	후, Object Program Code로 변환하는 프로그램을 구현하는게 목적입니다.
-	SIC머신과 SIC/XE머신에 대한 이론을 배우게 되었고, 배운 이론을 바탕으로 SIC/XE 소스를 Object Program Code로 변환해봄으로써 SIC/XE 어셈블러의 동작을 이해할 수 있도록 Control Section 방식의 어셈블러를 C와 자바로 구현했습니다. SIC/XE 소스가 작동 하려면 변환된 Object Program Code가 로더를 통해 메모리에 올라가야 하고, 올라간 메모리를 해석하여 해당 코드를 실행 시켜주는 것이 시뮬레이터입니다. 시뮬레이터를 구현함으로써 한 학기 동안 공부했던 SIC/XE 코드가 작동하는 방식에 대하여 알아보고자 합니다.
-	시뮬레이터를 구현하려면 어셈블러를 통해 생성한 Object Program Code가 메모리에 올라가야 하며, 비워뒀던 주소에 로딩을 함으로써 알게 된 주소를 채워넣는 리로케이션 작업이 필요합니다. 또한, 각 명령어가 작동할 수 있게 명령어의 용도에 맞게 구현하는 것이 중요합니다. 이번 프로젝트는 Object Program Code를 input으로 받아 GUI를 통해 명령어가 잘 작동하는지 눈으로 볼 수 있게 하고, COPY작업이 실행되는 프로그램을 구현하는게 목적입니다.


### 시뮬레이터 구현 GUI 기능

-	프로그램 종료 버튼
-	파일 오픈기능
-	레지스터 영역(SIC 및 SIC/XE머신의 레지스터를 모두 포함)
-	메모리 영역(다음과 같은 두 가지 표현 방식 중 한가지 방식으로 보여주어야 한다)
    1. 가상으로 설정한 메모리를 직접 보여주고, 현재 수행되고 있는 Instruction의 주소를 포인팅(가능하면 해당 Instruction을 영역지정까지)
    2. 메모리에 올라간 코드를 파싱하여 명령어 목록을 만들고, 해당 리스트를 표시하고, 현재 수행되고 있는 명령어를 리스트에서 선택하여 표시
-	프로그램 정보
  (프로그램 길이, 현재 포인팅 주소, 사용중인 장치, 현재 수행되고 있는 명령어 정보 등)
-	1 step 및 all step 기능


