import java.util.HashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * 모든 instruction의 정보를 관리하는 클래스. instruction data들을 저장한다
 * 또한 instruction 관련 연산, 예를 들면 목록을 구축하는 함수, 관련 정보를 제공하는 함수 등을 제공 한다.
 */
public class InstTable {
	/**
	 * inst.data 파일을 불러와 저장하는 공간. 
	 * 명령어의 이름을 집어넣으면 해당하는 Instruction의 정보들을 리턴할 수 있다.
	 */
	HashMap<String, Instruction> instMap;

	/**
	 * 클래스 초기화. 파싱을 동시에 처리한다.
	 * @param instFile : instuction에 대한 명세가 저장된 파일 이름
	 */
	public InstTable(String instFile) {
		instMap = new HashMap<String, Instruction>(); // 새로운 HashMap 생성
		openFile(instFile); // instFile을 열어서 내용 parsing instMap에 저장
	}

	/**
	 * 입력받은 이름의 파일을 열고 해당 내용을 파싱하여 instMap에 저장한다.
	 */
	public void openFile(String fileName) {
		try {
			File file = new File(fileName); // 파일 객체 생성
			FileReader filereader = new FileReader(file); // 입력 스트림 생성
			BufferedReader bufReader = new BufferedReader(filereader); // 입력 버퍼 생성
			String line = ""; // 읽은 버퍼를 넣을 변수 선언 및 초기화
			Instruction inst; // Instruction 객체 선언
			while ((line = bufReader.readLine()) != null) { // 버퍼가 있다면 (.readLine()은 끝에 개행문자를 읽지 않음)
				inst = new Instruction(line); // 파라미터가 line인 새 Instruction 객체 생성 
				instMap.put(inst.name, inst); // instMap에 key를 명령어의 이름으로 하여 value에 생성한 객체 추가
			}
			bufReader.close(); // 입력 버퍼 닫기
		} catch (FileNotFoundException e) { // 읽을 파일을 찾을 수 없다면
			System.out.println("Instruction 파일이 없습니다."); // 파일이 없다고 출력
			System.exit(0); // 종료
		} catch (IOException e) { // 예외 발생시
			System.out.println(e); // e 출력
		}
	}
	
	/**
	 * 인자로 전달된 operator의 opcode를 알려준다.
	 * @param operator : 검색을 원하는 명령어의 operator
	 * @return 명령어의 opcode. 해당 명령어가 없을 경우 -1 리턴
	 */
	public int search(String operator) {
		if (operator.startsWith("+")) { // operator가 "+"로 시작한다면 (형식이 4형식이라면)
			for (String key : instMap.keySet()) { // instMap의 처음부터 끝까지 반복
				if (operator.substring(1).equals(key)) { // operator에서 "+"를 뺀 문자열과 key가 같다면
					return instMap.get(key).opcode; // 해당 key의 opcode를 리턴
				}
			}
			return -1; // 목록에 없다면 -1 리턴
		} else { // 4형식이 아니라면
			for (String key : instMap.keySet()) { // instMap의 처음부터 끝까지 반복
				if (operator.equals(key)) { // operator와 key가 같다면
					return instMap.get(key).opcode; // 해당 key의 opcode를 리턴
				}
			}
			return -1; // 목록에 없다면 -1 리턴
		}
	}
	/**
	 * 인자로 전달된 operator의 format을 알려준다.
	 * @param operator : format을 알고싶은 명령어의 operator
	 * @return 명령어의 format. 해당 명령어가 없을 경우 -1 리턴
	 */
	public int getformat(String operator) {
		for (String key : instMap.keySet()) { // instMap의 처음부터 끝까지 반복
			if (operator.equals(key)) { // operator와 key가 같다면
				return instMap.get(key).format; // 해당 key의 format을 리턴
			}
		}
		return -1; // 목록에 없다면 -1 리턴
	}
}

/**
 * 명령어 하나하나의 구체적인 정보는 Instruction클래스에 담긴다. 
 * instruction과 관련된 정보들을 저장하고 기초적인 연산을 수행한다.
 */
class Instruction {

	String name; // 명령어 이름
	int opcode; // 명령어의 opcode
	int operandnum; // 명령어가 가지는 operand의 개수

	/** instruction이 몇 바이트 명령어인지 저장. 이후 편의성을 위함 */
	int format; // 명령어의 형식

	/**
	 * 클래스를 선언하면서 일반문자열을 즉시 구조에 맞게 파싱한다.
	 * 
	 * @param line : instruction 명세파일로부터 한줄씩 가져온 문자열
	 */
	public Instruction(String line) {
		parsing(line);
	}

	/**
	 * 일반 문자열을 파싱하여 instruction 정보를 파악하고 저장한다.
	 * @param line : instruction 명세파일로부터 한줄씩 가져온 문자열
	 */
	public void parsing(String line) {
		String[] array = line.split("\t"); // "\t"을 기준으로 인자로 들어온 line을 잘라서 array 배열에 넣기

		/** inst.txt의 형식은 이름 형식 opcode operand개수 순 */
		name = array[0];
		format = Integer.parseInt(array[1]);
		opcode = Integer.parseInt(array[2], 16); // 16진수로 저장
		operandnum = Integer.parseInt(array[3]);
	}
}
