package SP20_simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * SicLoader는 프로그램을 해석해서 메모리에 올리는 역할을 수행한다. 이 과정에서 linker의 역할 또한 수행한다. 
 * <br><br>
 * SicLoader가 수행하는 일을 예를 들면 다음과 같다.<br>
 * - program code를 메모리에 적재시키기<br>
 * - 주어진 공간만큼 메모리에 빈 공간 할당하기<br>
 * - 과정에서 발생하는 symbol, 프로그램 시작주소, control section 등 실행을 위한 정보 생성 및 관리
 */
public class SicLoader {
	ResourceManager rMgr;
	
	public SicLoader(ResourceManager resourceManager) {
		// 필요하다면 초기화
		setResourceManager(resourceManager);
	}

	/**
	 * Loader와 프로그램을 적재할 메모리를 연결시킨다.
	 * @param rMgr
	 */
	public void setResourceManager(ResourceManager resourceManager) {
		this.rMgr=resourceManager;
	}
	
	/**
	 * object code를 읽어서 load과정을 수행한다. load한 데이터는 resourceManager가 관리하는 메모리에 올라가도록 한다.
	 * load과정에서 만들어진 symbol table 등 자료구조 역시 resourceManager에 전달한다.
	 * @param objectCode 읽어들인 파일
	 */
	public void load(File objectCode){
		try {
			FileReader filereader = new FileReader(objectCode); // 입력 스트림 생성
			BufferedReader bufReader = new BufferedReader(filereader); // 입력 버퍼 생성
			String line = ""; // 읽은 버퍼를 넣을 변수 선언 및 초기화
			int currentSection = 0; // 현재 섹션을 표시할 변수
			while ((line = bufReader.readLine()) != null) { // 버퍼가 있다면 (.readLine()은 끝에 개행문자를 읽지 않음)
				if (!line.equals("")) {
					switch(line.charAt(0)) { // 라인의 첫번째 문자로 해당 줄 의 역할 파악
					case 'H' : // 헤더 레코드일때
						/* 프로그램 이름 , 프로그램 길이, 시작 주소를 가져오고 프로그램의 이름과 주소를 심볼 테이블 안에 넣기 */
						rMgr.setProgname(line.substring(1, 7), currentSection); // 
						rMgr.setProgLength(line.substring(13, 19), currentSection);
						rMgr.setStartADDR(line.substring(7, 13),currentSection);
						rMgr.symtabList.putSymbol(line.substring(1, 7), rMgr.StringToInt(rMgr.getStartADDR(currentSection)));
						break;
					case 'D' : // Define 레코드일때
						/* 정의한 심볼과 주소를 문장 끝까지 파싱하여 심볼리스트에 넣기 */
						for (int i = 0; (i*12)+13 <= line.length(); i++) {
							int address = 0;
							address = rMgr.StringToInt(rMgr.getStartADDR(currentSection)) + rMgr.StringToInt(line.substring((i*12)+7,(i*12)+13));
							rMgr.symtabList.putSymbol(line.substring((i*12)+1,(i*12)+7), address);
						}
						break;
					case 'R' :
						break;
					case 'T' : // Text 레코드일때
						/* 문장의 시작 주소와 길이를 기준으로 라인을 한꺼번에 char형으로 바꿔 메모리에 로드 */
						int locate = rMgr.StringToInt(rMgr.getStartADDR(currentSection)) + rMgr.StringToInt(line.substring(1, 7));
						char[] data = new char[60];
						int num = rMgr.StringToInt(line.substring(7,9));
						for(int i=9;i<line.length();i++){ 
							data[i-9]=(line.charAt(i));//스트링을 한글자씩 끊어 배열에 저장
						}
						rMgr.setMemory(locate, data, num);
						break;
					case 'M' :
						break;
					case 'E' : // End 레코드일때
						/* 끝나는 주소가 있다면 가져오기 */
						if (line.length() >= 7) {
							rMgr.setEndADDR(line.substring(1, 7));
						}
						currentSection++; // 섹션 이동
						break;
					default : // 그 외의 문자로 시작하는 것은 Object Code가 아님
						System.out.println("Object Code가 아닙니다.");
					}
				}
			}
			bufReader.close(); // 1 Pass 종료
			
			/* 비어있는 메모리를 Modified 레코드에 맞게 채워주기 위한 2Pass 시작 */
			FileReader filereader2 = new FileReader(objectCode); // 입력 스트림 생성
			BufferedReader bufReader2 = new BufferedReader(filereader2); // 입력 버퍼 생성
			String line2 = ""; // 읽은 버퍼를 넣을 변수 선언 및 초기화
			int currentSection2 = 0; // 현재 섹션을 표시할 변수
			while ((line2 = bufReader2.readLine()) != null) { // 버퍼가 있다면 (.readLine()은 끝에 개행문자를 읽지 않음)
				if (!line2.equals("")) { // 라인의 첫번째 문자로 해당 줄 의 역할 파악
					if(line2.charAt(0) == 'M') { // 2Pass에서는 Modified 레코드만 필요
						/* 문장의 주소를 섹션 시작 주소에 문장 시작 주소를 더한 값으로 바꾸고 길이 가져오기 */
						int Sloc = rMgr.StringToInt(rMgr.getStartADDR(currentSection2)) + rMgr.StringToInt(line2.substring(1, 7));
						int length = rMgr.StringToInt(line2.substring(7, 9));
						String Taddr = ""; // 기존의 0으로 되어있는 메모리 가져올 때 사용
						int Maddr = rMgr.symtabList.search(line2.substring(10)); // 심볼 테이블에 있는 주소에 맞게 바꿔줄 새로운 주소
						
						/* 바꿔야 하는 내용이 몇자리인지 확인 및 논리 연산하기 위한 bin 변수 */ 
						int bin = 1;
						for (int i = 0; i < length; i++) {
							bin *= 16;
						}
						bin -= 1;
						if (length % 2 > 0) { //길이가 홀수 (5)
							for (int i=0; i<((length+1)/2); i++) {
								Taddr += rMgr.getMemory(Sloc, (length+1)/2)[i]; // 기존의 메모리 내용을 Taddr을 이용하여 빼기
							}
							Taddr = Taddr.replace(Taddr.substring(1), String.format("%05X",((rMgr.StringToInt(Taddr) & bin) + Maddr))); // Maddr로 계산한 새로운 메모리를 해당 위치에 대체해주기
							
						}
						else { //길이가 짝수 (6)
							for (int i=0; i<(length/2); i++) {
								Taddr += rMgr.getMemory(Sloc, length/2)[i]; // 기존의 메모리 내용을 Taddr을 이용하여 빼기
							}
							if(line2.charAt(9) == '+') {
								Taddr = Taddr.replace(Taddr, String.format("%06X",((rMgr.StringToInt(Taddr) & bin) + Maddr))); // Maddr로 계산한 새로운 메모리를 해당 위치에 대체해주기
							}
							else {
								Taddr = Taddr.replace(Taddr, String.format("%06X",((rMgr.StringToInt(Taddr) & bin) - Maddr))); // 뺄셈일경우
							}
						}
						
						// 새롭게 계산된 메모리 내용을 다시 메모리에 적재 시키기 위해 char 배열로 변경 
						char[] T = new char[60];
						for(int i=0;i<Taddr.length();i++){ 
							T[i]=(Taddr.charAt(i));//스트링을 한글자씩 끊어 배열에 저장
						}
						rMgr.setMemory(Sloc, T, (length+1)/2);
					} 
					/* End 레코드 나오면 섹션 바꿔주기 */
					if(line2.charAt(0) == 'E') {
						currentSection2++;
					}
				}
			}
			bufReader2.close(); // 입력 버퍼 닫기
			analysisOb(); // 로더 완료시 가장 먼저 실행할 명령어를 Instruction 위에 올려주기 위해 실행
		} catch (FileNotFoundException e) { // 읽을 파일을 찾을 수 없다면
			System.out.println("Input 파일이 없습니다."); // 파일이 없다고 출력
			System.exit(0); // 종료
		} catch (IOException e) { // 예외 발생시
			System.out.println(e); // e 출력
		}
		
	};
	
	public void analysisOb () {
		/* 가장 먼저 실행할 명령어를 올려주기 위한 함수로 InstLuncher에도 비슷한 함수 존재, InstLuncher에서 더 세밀하게 분리 */
		int nFlag = 0;
		int iFlag = 0;
		int xFlag = 0;
		int bFlag = 0;
		int pFlag = 0;
		int eFlag = 0;
		String Ob = "";
		int Op = -1;
		int index = 0;
		
		for (int i = 0; i < rMgr.memory.length; i++) { // 메모리 끝까지 실행
			if (rMgr.memory[i] != null) { // 메모리가 비어있지 않다면
				int first = rMgr.StringToInt(rMgr.getMemory(i, 2)[0]); // 메모리 한개를 읽어와서 first에 넣고 
				if(rMgr.getMemory(i, 2)[1] != null) { // 그 다음 메모리도 비어있지 않다면
					int second = rMgr.StringToInt(rMgr.getMemory(i, 2)[1]); // 읽어와서 second에 넣기
					
					
					/* nixbpe Flag 계산해서 넣어주기 */
					iFlag = first % 2;
					nFlag = (first / 2) % 2;
					eFlag = ((second & 0xF0) >> 4) % 2;
					pFlag = (((second & 0xF0) >> 4) /2) % 2;
					bFlag = ((((second & 0xF0) >> 4) /2) /2) % 2;
					xFlag = (((((second & 0xF0) >> 4) /2) /2) /2) % 2;
					
					/* Flag를 이용하여 각 몇바이트 명령어인지 알아내고 해당 길이 만큼 Ob에 넣고 Op를 계산 */
					if(nFlag == 0 && iFlag == 0) { //2bytes
						Ob = rMgr.getMemory(i, 2)[0] + rMgr.getMemory(i, 2)[1];
						Op = rMgr.StringToInt(rMgr.getMemory(i, 2)[0]);
					}
					else {
						if (eFlag == 1) { //4bytes
							Ob = rMgr.getMemory(i, 4)[0] + rMgr.getMemory(i, 4)[1] + rMgr.getMemory(i, 4)[2] + rMgr.getMemory(i, 4)[3];
							Op = rMgr.StringToInt(rMgr.getMemory(i, 4)[0]) - (nFlag * 2) - (iFlag * 1);
							i+=2;
						}
						else { //3bytes
							Ob = rMgr.getMemory(i, 3)[0] + rMgr.getMemory(i, 3)[1] + rMgr.getMemory(i, 3)[2];
							Op = rMgr.StringToInt(rMgr.getMemory(i, 4)[0]) - (nFlag * 2) - (iFlag * 1);
							i++;
						}
					}
					i++;
				}
				
				/* 두번째가 비어있다면 한바이트만 가져오고 Op는 -1로 두기 */
				else {
					Ob = rMgr.getMemory(i, 2)[0];
					Op = -1;
				}
				
				/* 계산한 내용을 ObjectCode 클래스의 리스트에 넣기 */
				
				rMgr.addObjectCode(index, Ob, Op, nFlag, iFlag, xFlag, bFlag, pFlag, eFlag);
				index++;
			}
		}
	}

}
