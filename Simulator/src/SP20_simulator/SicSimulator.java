package SP20_simulator;

import java.io.File;

/**
 * 시뮬레이터로서의 작업을 담당한다. VisualSimulator에서 사용자의 요청을 받으면 이에 따라
 * ResourceManager에 접근하여 작업을 수행한다.  
 * 
 * 작성중의 유의사항 : <br>
 *  1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은 지양할 것.<br>
 *  2) 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.<br>
 *  3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.<br>
 *  4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)<br>
 * 
 * <br><br>
 *  + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수 있습니다.
 */
public class SicSimulator { 
	/* 시뮬레이터에 인스트런쳐 연결 */
	ResourceManager rMgr;
	InstLuncher inst;
	int index = 0;

	public SicSimulator(ResourceManager resourceManager, InstLuncher instLuncher) {
		this.rMgr = resourceManager;
		this.inst = instLuncher;
	}

	/**
	 * 레지스터, 메모리 초기화 등 프로그램 load와 관련된 작업 수행.
	 * 단, object code의 메모리 적재 및 해석은 SicLoader에서 수행하도록 한다. 
	 */
	public void load(File program) {
		rMgr.setRegister(8, 0); // PC 레지스터 0으로 초기화
	}

	/**
	 * 1개의 instruction이 수행된 모습을 보인다. 
	 */
	public void oneStep() {
		inst.run(); // 인스트런쳐의 run 함수 실행
		addLog(rMgr.getObjectCode(index).Operator + "\n"); // 로그 리스트에 로그 넣기
		index++; // 다음 인덱스로 이동
	}
	
	/**
	 * 남은 모든 instruction이 수행된 모습을 보인다.
	 */
	public void allStep() {
		/* onestep을 프로그램 끝날때까지 반복*/
		while (!(index  != 0 && rMgr.getRegister(8) == rMgr.StringToInt(rMgr.EndADDR))) {
			oneStep(); 
		}
	}
	
	/**
	 * 각 단계를 수행할 때 마다 관련된 기록을 남기도록 한다.
	 */
	public void addLog(String log) {
		rMgr.Log.add(log); // 로그 리스트에 로그 추가
	}	
}
