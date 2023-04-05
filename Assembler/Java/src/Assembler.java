import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Assembler : 이 프로그램은 SIC/XE 머신을 위한 Assembler 프로그램의 메인 루틴이다. 
 * 프로그램의 수행 작업은 다음과 같다. 
 * 1) 처음 시작하면 Instruction 명세를 읽어들여서 assembler를 세팅한다. 
 * 2) 사용자가 작성한 input 파일을 읽어들인 후 저장한다. 
 * 3) input 파일의 문장들을 단어별로 분할하고 의미를 파악해서 정리한다. (pass1) 
 * 4) 분석된 내용을 바탕으로 컴퓨터가 사용할 수 있는 object code를 생성한다. (pass2)
 * 
 * 
 * 작성중의 유의사항 : 
 * 1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은 안된다. 
 * 2) 마찬가지로 작성된 코드를 삭제하지 않으면 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨. 
 * 3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능. 
 * 4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)
 * 
 * 
 * + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수
 * 있습니다.
 */
public class Assembler {
	/** instruction 명세를 저장한 공간 */
	InstTable instTable;
	/** 읽어들인 input 파일의 내용을 한 줄 씩 저장하는 공간. */
	ArrayList<String> lineList;
	/** 프로그램의 section별로 symbol table을 저장하는 공간 */
	ArrayList<SymbolTable> symtabList;
	/** 프로그램의 section별로 literal table을 저장하는 공간 */
	ArrayList<LiteralTable> literaltabList;
	/** 프로그램의 section별로 프로그램을 저장하는 공간 */
	ArrayList<TokenTable> TokenList;
	/**
	 * Token, 또는 지시어에 따라 만들어진 오브젝트 코드들을 출력 형태로 저장하는 공간. 
	 * 필요한 경우 String 대신 별도의 클래스를 선언하여 ArrayList를 교체해도 무방함.
	 */
	ArrayList<String> codeList;

	/**
	 * 클래스 초기화. instruction Table을 초기화와 동시에 세팅한다.
	 * 
	 * @param instFile : instruction 명세를 작성한 파일 이름.
	 */
	public Assembler(String instFile) {
		instTable = new InstTable(instFile);
		lineList = new ArrayList<String>();
		symtabList = new ArrayList<SymbolTable>();
		literaltabList = new ArrayList<LiteralTable>();
		TokenList = new ArrayList<TokenTable>();
		codeList = new ArrayList<String>();
	}

	/**
	 * 어셈블러의 메인 루틴
	 */
	public static void main(String[] args) {
		Assembler assembler = new Assembler("inst.txt");
		assembler.loadInputFile("input.txt");
		assembler.pass1();

		assembler.printSymbolTable("symtab_20180262.txt");
		assembler.printLiteralTable("literaltab_20180262.txt");
		assembler.pass2();
		assembler.printObjectCode("output_20180262.txt");

	}

	/**
	 * inputFile을 읽어들여서 lineList에 저장한다.
	 * @param inputFile : input 파일 이름.
	 */
	private void loadInputFile(String inputFile) {
		try {
			File file = new File(inputFile); // 파일 객체 생성
			FileReader filereader = new FileReader(file); // 입력 스트림 생성
			BufferedReader bufReader = new BufferedReader(filereader); // 입력 버퍼 생성
			String line = ""; // 읽은 버퍼를 넣을 변수 선언 및 초기화
			while ((line = bufReader.readLine()) != null) { // 버퍼가 있다면 (.readLine()은 끝에 개행문자를 읽지 않음)
				lineList.add(line); // lineList에 line 추가
			}
			bufReader.close(); // 입력 버퍼 닫기
		} catch (FileNotFoundException e) { // 읽을 파일을 찾을 수 없다면
			System.out.println("Input 파일이 없습니다."); // 파일이 없다고 출력
			System.exit(0); // 종료
		} catch (IOException e) { // 예외 발생시
			System.out.println(e); // e 출력
		}

	}

	/**
	 * pass1 과정을 수행한다. 
	 * 1) 프로그램 소스를 스캔하여 토큰단위로 분리한 뒤 토큰테이블 생성 
	 * 2) label을 symbolTable에 정리
	 * 
	 * 주의사항 : SymbolTable과 TokenTable은 프로그램의 section별로 하나씩 선언되어야 한다.
	 */
	private void pass1() {
		int locctr = 0; // 현재 주소를 나타내는 변수
		/** TokenTable을 section별로 하나씩 만들어서 TokenList에 연결하기 위한 반복문 */
		int section = 0; // 현재 section을 나타내는 변수
		TokenList.add(new TokenTable(new SymbolTable(), new LiteralTable(), instTable)); // 새로운 TokenTable, 새로운 SymbolTable, 새로운 LiteralTable을 생성해서 instTable과 함께 TokenList에 추가
		symtabList.add(TokenList.get(section).symTab); // SymtabList에 현재 section에서 새로 만든 SymbolTable을 추가 
		literaltabList.add(TokenList.get(section).literalTab); // literaltabList에 현재 section에서 새로 만든literalTable을 추가 
		for (int i = 0; i < lineList.size(); i++) { // input 파일에서 읽어들인 line의 수만큼 반복 (input 파일 처음부터 끝까지)
			TokenList.get(section).putToken(lineList.get(i)); // 현재 section의 TokenTable에 읽어들인 line을 파싱하여 Token을 추가
			if (lineList.size() > i + 1) { // 다음 인덱스가 lineList보다 작다면 (마지막 line이 아니라면)
				if (lineList.get(i + 1).contains("CSECT")) { // 다음 줄에 CSECT 명령어가 있다면 (section을 바꿔주기 위해) 
					section++; // section을 1만큼 증가 (다음 section으로 이동)
					TokenList.add(new TokenTable(new SymbolTable(), new LiteralTable(), instTable)); // 새로운 TokenTable, 새로운 SymbolTable, 새로운 LiteralTable을 생성해서 instTable과 함께 TokenList에 추가
					symtabList.add(TokenList.get(section).symTab); // SymtabList에 현재 section에서 새로 만든 SymbolTable을 추가 
					literaltabList.add(TokenList.get(section).literalTab); // literaltabList에 현재 section에서 새로 만든literalTable을 추가 
				}
			}
		}
		
		/** 주소를 계산해서 location에 넣어주고 literal과 symbol을 table에 넣기 위한 반복문 */
		for (int sec = 0; sec < TokenList.size(); sec++) { // TokenList의 크기만큼 반복 (section의 개수만큼)
			for (int index = 0; index < TokenList.get(sec).tokenList.size(); index++) { // 해당 section의 TokenTable의 크기만큼 반복 (해당 section의 line 개수만큼)
				if (TokenList.get(sec).getToken(index).operator != null) { // 해당 operator가 null이 아니라면 (있다면)
					if (TokenList.get(sec).getToken(index).operator.equals("START")) { // 해당 operator가 START라면
						locctr = Integer.parseInt(TokenList.get(sec).getToken(index).operand[0]); // operand에 있는 시작주소를 정수 형태로 변환하여 locctr에 넣어주기
						TokenList.get(sec).getToken(index).location = locctr; // 현재 location에 locctr 넣어주기
						if (TokenList.get(sec).tokenList.size() > index + 1) {  // 다음 인덱스가 tokenList보다 작다면 (해당 section의 마지막 token이 아니라면)
							TokenList.get(sec).getToken(index + 1).location = locctr; //다음 location에도 locctr 넣어주기 (START는 크기가 없는 명령어)
						}
					} else if (TokenList.get(sec).getToken(index).operator.equals("CSECT")) { // 해당 operator가 CSECT라면
						locctr = 0; // locctr에 0 넣어주기 (새로운 section의 시작이므로 주소 초기화)
						TokenList.get(sec).getToken(index).location = locctr; // 현재 location에 locctr 넣어주기
						if (TokenList.get(sec).tokenList.size() > index + 1) {  // 다음 인덱스가 tokenList보다 작다면 (해당 section의 마지막 token이 아니라면)
							TokenList.get(sec).getToken(index + 1).location = locctr; //다음 location에도 locctr 넣어주기 (CSECT는 크기가 없는 명령어)
						}
					} else if (instTable.search(TokenList.get(sec).getToken(index).operator) > -1) { // instTable에 있는 명령어라면 (없으면 -1 return)
						if (instTable.getformat(TokenList.get(sec).getToken(index).operator) == 2) { // 해당 operator의 형식이 2형식이라면
							TokenList.get(sec).getToken(index).byteSize = 2; // 현재 명령어의 byteSize에 2 넣어주기 (2형식 명령어의 크기는 2bytes)
							locctr += TokenList.get(sec).getToken(index).byteSize; // locctr에 현재 locctr에 byteSize만큼 더해서 넣어주기
							if (TokenList.get(sec).tokenList.size() > index + 1) { // 다음 인덱스가 tokenList보다 작다면 (해당 section의 마지막 token이 아니라면)
								TokenList.get(sec).getToken(index + 1).location = locctr; //다음 location에도 locctr 넣어주기
							}
						} else if (TokenList.get(sec).getToken(index).operator.startsWith("+")) { // 해당 operator가 "+"로 시작한다면 (형식이 4형식이라면)
							TokenList.get(sec).getToken(index).byteSize = 4; // 현재 명령어의 byteSize에 4 넣어주기 (4형식 명령어의 크기는 4bytes)
							locctr += TokenList.get(sec).getToken(index).byteSize; // locctr에 현재 locctr에 byteSize만큼 더해서 넣어주기
							if (TokenList.get(sec).tokenList.size() > index + 1) { // 다음 인덱스가 tokenList보다 작다면 (해당 section의 마지막 token이 아니라면)
								TokenList.get(sec).getToken(index + 1).location = locctr; //다음 location에도 locctr 넣어주기
							}
						} else { // 2형식도 4형식도 아니라면 (3형식이라면)
							TokenList.get(sec).getToken(index).byteSize = 3; // 현재 명령어의 byteSize에 3 넣어주기 (3형식 명령어의 크기는 3bytes)
							locctr += TokenList.get(sec).getToken(index).byteSize; // locctr에 현재 locctr에 byteSize만큼 더해서 넣어주기
							if (TokenList.get(sec).tokenList.size() > index + 1) { // 다음 인덱스가 tokenList보다 작다면 (해당 section의 마지막 token이 아니라면)
								TokenList.get(sec).getToken(index + 1).location = locctr; //다음 location에도 locctr 넣어주기
							}
						}
					} else if (TokenList.get(sec).getToken(index).operator.equals("WORD")) { // 해당 operator가 WORD라면
						TokenList.get(sec).getToken(index).byteSize = 3; // 현재 명령어의 byteSize에 3 넣어주기 (WORD 명령어의 크기는 3bytes)
						locctr += TokenList.get(sec).getToken(index).byteSize; // locctr에 현재 locctr에 byteSize만큼 더해서 넣어주기
						if (TokenList.get(sec).tokenList.size() > index + 1) { // 다음 인덱스가 tokenList보다 작다면 (해당 section의 마지막 token이 아니라면)
							TokenList.get(sec).getToken(index + 1).location = locctr; //다음 location에도 locctr 넣어주기
						}
					} else if (TokenList.get(sec).getToken(index).operator.equals("RESW")) { // 해당 operator가 RESW라면
						TokenList.get(sec).getToken(index).byteSize = 3	* Integer.parseInt(TokenList.get(sec).getToken(index).operand[0]);
						// 현재 명령어의 byteSize에 operand에 있는 WORD 개수를 정수 형태로 변환하여 3을 곱하여 넣어주기 (1WORD = 3bytes)
						locctr += TokenList.get(sec).getToken(index).byteSize; // locctr에 현재 locctr에 byteSize만큼 더해서 넣어주기
						if (TokenList.get(sec).tokenList.size() > index + 1) { // 다음 인덱스가 tokenList보다 작다면 (해당 section의 마지막 token이 아니라면)
							TokenList.get(sec).getToken(index + 1).location = locctr; //다음 location에도 locctr 넣어주기
						}
					} else if (TokenList.get(sec).getToken(index).operator.equals("RESB")) { // 해당 operator가 RESB라면
						TokenList.get(sec).getToken(index).byteSize = Integer.parseInt(TokenList.get(sec).getToken(index).operand[0]);
						// 현재 명령어의 byteSize에 operand에 있는 Byte 개수를 정수 형태로 변환하여 넣어주기
						locctr += TokenList.get(sec).getToken(index).byteSize; // locctr에 현재 locctr에 byteSize만큼 더해서 넣어주기
						if (TokenList.get(sec).tokenList.size() > index + 1) { // 다음 인덱스가 tokenList보다 작다면 (해당 section의 마지막 token이 아니라면)
							TokenList.get(sec).getToken(index + 1).location = locctr; //다음 location에도 locctr 넣어주기
						}
					} else if (TokenList.get(sec).getToken(index).operator.equals("BYTE")) { // 해당 operator가 BYTE라면
						if (TokenList.get(sec).getToken(index).operand[0].startsWith("C")) { // 해당 operand가 C로 시작한다면 (문자라면)
							TokenList.get(sec).getToken(index).byteSize = TokenList.get(sec).getToken(index).operand[0].length() - 3;
							// 현재 명령어의 byteSize에 "C"와 따옴표를 제외한 operand의 길이를 넣어주기 (문자 1개는 1byte)
							locctr += TokenList.get(sec).getToken(index).byteSize; // locctr에 현재 locctr에 byteSize만큼 더해서 넣어주기
							if (TokenList.get(sec).tokenList.size() > index + 1) { // 다음 인덱스가 tokenList보다 작다면 (해당 section의 마지막 token이 아니라면)
								TokenList.get(sec).getToken(index + 1).location = locctr; //다음 location에도 locctr 넣어주기
							}
						} else if (TokenList.get(sec).getToken(index).operand[0].startsWith("X")) { // 해당 operand가 X로 시작한다면 (16진수 정수라면)
							TokenList.get(sec).getToken(index).byteSize = (TokenList.get(sec).getToken(index).operand[0].length() - 3) / 2;
							// 현재 명령어의 byteSize에 "X"와 따옴표를 제외한 operand의 길이에서 2를 나누고 넣어주기  (16진수 숫자 2자리가 1byte)
							locctr += TokenList.get(sec).getToken(index).byteSize; // locctr에 현재 locctr에 byteSize만큼 더해서 넣어주기
							if (TokenList.get(sec).tokenList.size() > index + 1) { // 다음 인덱스가 tokenList보다 작다면 (해당 section의 마지막 token이 아니라면)
								TokenList.get(sec).getToken(index + 1).location = locctr; //다음 location에도 locctr 넣어주기
							}
						}
					} else if (TokenList.get(sec).getToken(index).operator.equals("LTORG")) { // 해당 operator가 LTORG라면
						for (int i = 0; i < literaltabList.get(sec).literalList.size(); i++) { // 해당 section의 LieteralTable의 크기만큼 반복 (해당 section의 literal 개수만큼)
							String str = TokenList.get(sec).getToken(literaltabList.get(sec).locationList.get(i)).operand[0];
							// 해당 literal이 선언된 주소의 operand를 str에 넣어주기
							String literal = literaltabList.get(sec).literalList.get(i); // 해당 literal을 literal 변수에 넣어주기
							if (str.substring(1).startsWith("C")) { // 해당 operand가 C로 시작한다면 (앞에 "=" 제외)
								TokenList.get(sec).getToken(index).byteSize = literaltabList.get(sec).literalList.get(i).length();
								 // 현재 명령어의 byteSize에 해당 literal의 길이를 넣어주기 (문자 1개는 1byte) 
								locctr += TokenList.get(sec).getToken(index).byteSize; // locctr에 현재 locctr에 byteSize만큼 더해서 넣어주기
								literaltabList.get(sec).modifyLiteral(literal, TokenList.get(sec).getToken(index).location);
								// LieteralTable에 있는 해당 literal의 주소가 선언된 위치로 되어 있는데 LTORG가 있는 위치로 변경
							} else if (str.substring(1).startsWith("X")) { // 해당 operand가 X로 시작한다면 (앞에 "=" 제외)
								TokenList.get(sec).getToken(index).byteSize = (literaltabList.get(sec).literalList.get(i).length()) / 2;
								// 현재 명령어의 byteSize에 해당 literal의 길이를 2로 나누고 넣어주기  (16진수 숫자 2자리가 1byte)
								locctr += TokenList.get(sec).getToken(index).byteSize; // locctr에 현재 locctr에 byteSize만큼 더해서 넣어주기
								literaltabList.get(sec).modifyLiteral(literal, TokenList.get(sec).getToken(index).location);
								// LieteralTable에 있는 해당 literal의 주소가 선언된 위치로 되어 있는데 LTORG가 있는 위치로 변경
							}
						}
						if (TokenList.get(sec).tokenList.size() > index + 1) { // 다음 인덱스가 tokenList보다 작다면 (해당 section의 마지막 token이 아니라면)
							TokenList.get(sec).getToken(index + 1).location = locctr; //다음 location에도 locctr 넣어주기
						}
					} else if (TokenList.get(sec).getToken(index).operator.equals("END")) { // 해당 operator가 END라면
						for (int i = 0; i < literaltabList.get(sec).literalList.size(); i++) { // 해당 section의 LieteralTable의 크기만큼 반복 (해당 section의 literal 개수만큼)
							String str = TokenList.get(sec).getToken(literaltabList.get(sec).locationList.get(i)).operand[0];
							// 해당 literal이 선언된 주소의 operand를 str에 넣어주기
							String literal = literaltabList.get(sec).literalList.get(i); // 해당 literal을 literal 변수에 넣어주기
							if (str.substring(1).startsWith("C")) { // 해당 operand가 C로 시작한다면 (앞에 "=" 제외)
								TokenList.get(sec).getToken(index).byteSize = literaltabList.get(sec).literalList.get(i).length();
								 // 현재 명령어의 byteSize에 해당 literal의 길이를 넣어주기 (문자 1개는 1byte) 
								locctr += TokenList.get(sec).getToken(index).byteSize; // locctr에 현재 locctr에 byteSize만큼 더해서 넣어주기
								literaltabList.get(sec).modifyLiteral(literal, TokenList.get(sec).getToken(index).location);
								// LieteralTable에 있는 해당 literal의 주소가 선언된 위치로 되어 있는데 LTORG가 있는 위치로 변경
							} else if (str.substring(1).startsWith("X")) { // 해당 operand가 X로 시작한다면 (앞에 "=" 제외)
								TokenList.get(sec).getToken(index).byteSize = (literaltabList.get(sec).literalList.get(i).length()) / 2;
								// 현재 명령어의 byteSize에 해당 literal의 길이를 2로 나누고 넣어주기  (16진수 숫자 2자리가 1byte)
								locctr += TokenList.get(sec).getToken(index).byteSize; // locctr에 현재 locctr에 byteSize만큼 더해서 넣어주기
								literaltabList.get(sec).modifyLiteral(literal, TokenList.get(sec).getToken(index).location);
								// LieteralTable에 있는 해당 literal의 주소가 선언된 위치로 되어 있는데 LTORG가 있는 위치로 변경
							}
						}
						break; // 반복문 종료 (END가 나오면 읽기 종료)
					}
					
					/** literalList에 literal 추가*/
					if (TokenList.get(sec).getToken(index).operand != null) { // 해당 operand가 null이 아니라면 (있다면)
						if (TokenList.get(sec).getToken(index).operand[0].startsWith("=")) {  // 해당 operand가 "="으로 시작한다면 (literal 선언이라면)
							String literal = TokenList.get(sec).getToken(index).operand[0].substring(3, TokenList.get(sec).getToken(index).operand[0].length() - 1);
							 // 해당 operand에서 "=", "C" or "X", 따옴표를 제외하고 literal 변수에 넣어주기
							if (TokenList.get(sec).literalTab.literalList != null) { // literalList가 null이 아니라면 (literal이 있다면)
								if (literaltabList.get(sec).search(literal) != -1) { // literalTable에 해당 literal이 있다면 (없다면 -1 리턴)
									literaltabList.get(sec).modifyLiteral(literal, index); // 기존의 literal 주소를 새로운 주소로 수정
								} else { // literalTable에 literal이 없다면
									literaltabList.get(sec).putLiteral(literal, index); // literalTable에 literal과 literal이 선언된 위치를 넣어주기
								}
							} else { // literalList가 비어있다면
								literaltabList.get(sec).putLiteral(literal, index); // literalTable에 literal과 literal이 선언된 위치를 넣어주기
							}
						}
					}
					
					/** symbolList에 symbol 추가*/
					if (TokenList.get(sec).getToken(index).label != null) { // 해당 label이 null이 아니라면 (있다면)
						if (!TokenList.get(sec).getToken(index).label.startsWith(".")) { // label이 "."으로 시작하지 않는다면 (의미없는 line의 symbol 넣지 않기 위해)
							String label = TokenList.get(sec).getToken(index).label; // 해당 label을 label 변수에 넣어주기
							int location = TokenList.get(sec).getToken(index).location; // 현재 주소를 location 변수에 넣어주기
							symtabList.get(sec).putSymbol(label, location); // symbolTable에 labell과 location 넣어주기
						}
					}
					
					/** EQU 명령어의 주소 처리 */
					if (TokenList.get(sec).getToken(index).operator.equals("EQU")) { // 해당 operator가 EQU라면
						for (int i = 0; i < symtabList.get(sec).symbolList.size(); i++) { // 해당 section의 symbolTable의 크기만큼 반복 (해당 section의 symbol 개수만큼)
							String symbol = symtabList.get(sec).symbolList.get(i); // 해당 symbol을 symbol 변수에 넣기
							if (TokenList.get(sec).getToken(index).operand[0].contains(symbol)) { // EQU의 operand에 symbol이 있다면
								
								/* COPY 프로그램에는 "-"만 있어서 뺄셈만 구현 */
								String[] array = TokenList.get(sec).getToken(index).operand[0].split("-"); // "-"를 기준으로 operand를 잘라서 array 배열에 넣기
								int newLocation = symtabList.get(sec).search(array[0]); // 가장 먼저 나온 symbol의 주소를 찾아서 newLocation 변수에 넣기
								for (int j = 1; j < array.length; j++) { // 이미 사용한 0번째 배열을 제외하고 1번째 배열부터 배열의 크기만큼 반복
									newLocation -= symtabList.get(sec).search(array[j]); // newLocation에서 j번째 배열에 있는 symbol의 주소를 찾아서 빼고 newLocation 변수에 넣기
								}
								symtabList.get(sec).modifySymbol(TokenList.get(sec).getToken(index).label, newLocation); // symbolTable에 있는 해당 symbol의 주소를 계산한 새 주소로 변경
								TokenList.get(sec).getToken(index).location = newLocation; // 현재 token의 주소도 새 주소로 변경
								break; // 반복문 종료
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 작성된 SymbolTable들을 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printSymbolTable(String fileName) { // 
		try {
			File file = new File(fileName); // 파일 객체 생성
			PrintWriter pw = new PrintWriter(file); // 출력 스트림 생성
			for (int sec = 0; sec < TokenList.size(); sec++) { // TokenList의 크기만큼 반복 (section의 개수만큼)
				if (symtabList.get(sec).symbolList != null) { // 해당 section의 symbolTable이 null이 아니라면 (있다면)
					for (int i = 0; i < symtabList.get(sec).symbolList.size(); i++) { // 해당 section의 SymbolTable의 크기만큼 반복 (해당 section의 symbol 개수만큼)
						pw.format("%s\t%X\n", symtabList.get(sec).symbolList.get(i), symtabList.get(sec).locationList.get(i));
						//symbol location 순으로 파일 출력 (location은 16진수로 출력)
					}
				}
				pw.println(); // section이 끝나고 구분하기 위해 줄바꿈
			}
			pw.close(); // 출력 스트림 닫기
		} catch (IOException e) { // 예외 발생시
			System.out.println("작성할 파일이 없습니다."); // 파일이 없다고 출력
		}

	}

	/**
	 * 작성된 LiteralTable들을 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printLiteralTable(String fileName) {
		try {
			File file = new File(fileName); // 파일 객체 생성
			PrintWriter pw = new PrintWriter(file); // 출력 스트림 생성
			for (int sec = 0; sec < TokenList.size(); sec++) { // TokenList의 크기만큼 반복 (section의 개수만큼)
				if (literaltabList.get(sec).literalList != null) { // 해당 section의 literalTable이 null이 아니라면 (있다면)
					for (int i = 0; i < literaltabList.get(sec).literalList.size(); i++) { // 해당 section의 LiteralTable의 크기만큼 반복 (해당 section의 literal 개수만큼)
						pw.format("%s\t%X\n", literaltabList.get(sec).literalList.get(i), literaltabList.get(sec).locationList.get(i));
						//literal location 순으로 파일 출력 (location은 16진수로 출력)
					}
				}
			}
			pw.close(); // 출력 스트림 닫기
		} catch (IOException e) { // 예외 발생시
			System.out.println("작성할 파일이 없습니다."); // 파일이 없다고 출력
		}
	}

	/**
	 * pass2 과정을 수행한다. 
	 * 1) 분석된 내용을 바탕으로 object code를 생성하여 codeList에 저장.
	 */
	private void pass2() {
		/** nixbpe 비트를 계산해서 nixbpe에 넣어주기 위한 반복문 */
		for (int sec = 0; sec < TokenList.size(); sec++) { // TokenList의 크기만큼 반복 (section의 개수만큼)
			for (int index = 0; index < TokenList.get(sec).tokenList.size(); index++) { // 해당 section의 TokenTable의 크기만큼 반복 (해당 section의 line 개수만큼)
				if (TokenList.get(sec).getToken(index).operator != null) { // 해당 operator가 null이 아니라면 (있다면)
					if (instTable.search(TokenList.get(sec).getToken(index).operator) > -1) { // instTable에 있는 명령어라면 (없으면 -1 return)
						if (instTable.getformat(TokenList.get(sec).getToken(index).operator) == 2) { // 해당 operator의 형식이 2형식이라면
							TokenList.get(sec).getToken(index).nixbpe = 0; // nixbpe에 0 넣어주기 (2형식은 nixbpe 사용 안 함)
						} else if (TokenList.get(sec).getToken(index).operator.startsWith("+")) { // 해당 operator가 "+"로 시작한다면 (형식이 4형식이라면)
							TokenList.get(sec).getToken(index).setFlag(TokenTable.eFlag, 1); // eFlag에 1 넣어주기 (4형식이므로 확장)
							TokenList.get(sec).getToken(index).setFlag(TokenTable.bFlag, 0); // bFlag에 0 넣어주기 (확장해서 상대 주소 사용 안 함)
							TokenList.get(sec).getToken(index).setFlag(TokenTable.pFlag, 0); // pFlag에 0 넣어주기 (확장해서 상대 주소 사용 안 함)
							if (TokenList.get(sec).getToken(index).operand[0].startsWith("#")) { // 해당 operand가 "#"로 시작한다면 (즉시 주소 지정 방식)
								TokenList.get(sec).getToken(index).setFlag(TokenTable.nFlag, 0); // nFlag에 0넣어주기
								TokenList.get(sec).getToken(index).setFlag(TokenTable.iFlag, 1); // iFlag에 1 넣어주기
								for (int i = 0; i < TokenList.get(sec).getToken(index).operand.length; i++) { // operand 개수 만큼 반복
									if (TokenList.get(sec).getToken(index).operand[i].equals("X")) { // 해당 operand가 X라면 (index)
										TokenList.get(sec).getToken(index).setFlag(TokenTable.xFlag, 1); // xFlag에 1 넣어주기
										break; //X가 하나만 있어도 반복문 종료
									} else // 해당 operand가 X가 아니라면
										TokenList.get(sec).getToken(index).setFlag(TokenTable.xFlag, 0); // xFlag에 0 넣어주기
								}
							} else if (TokenList.get(sec).getToken(index).operand[0].startsWith("@")) { // 해당 operand가 "@"로 시작한다면 (간접 주소 지정 방식)
								TokenList.get(sec).getToken(index).setFlag(TokenTable.nFlag, 1); // nFlag에 1넣어주기
								TokenList.get(sec).getToken(index).setFlag(TokenTable.iFlag, 0); // iFlag에 0넣어주기
								for (int i = 0; i < TokenList.get(sec).getToken(index).operand.length; i++) { // operand 개수 만큼 반복
									if (TokenList.get(sec).getToken(index).operand[i].equals("X")) { // 해당 operand가 X라면 (index)
										TokenList.get(sec).getToken(index).setFlag(TokenTable.xFlag, 1); // xFlag에 1 넣어주기
										break; //X가 하나만 있어도 반복문 종료
									} else // 해당 operand가 X가 아니라면
										TokenList.get(sec).getToken(index).setFlag(TokenTable.xFlag, 0); // xFlag에 0 넣어주기
								}
							} else { // 해당 operand가 "#" 또는 "@"로 시작하지 않는다면  (simple addressing)
								TokenList.get(sec).getToken(index).setFlag(TokenTable.nFlag, 1); // nFlag에 1넣어주기
								TokenList.get(sec).getToken(index).setFlag(TokenTable.iFlag, 1); // iFlag에 1넣어주기
								for (int i = 0; i < TokenList.get(sec).getToken(index).operand.length; i++) { // operand 개수 만큼 반복
									if (TokenList.get(sec).getToken(index).operand[i].equals("X")) { // 해당 operand가 X라면 (index)
										TokenList.get(sec).getToken(index).setFlag(TokenTable.xFlag, 1); // xFlag에 1 넣어주기
										break; //X가 하나만 있어도 반복문 종료
									} else // 해당 operand가 X가 아니라면
										TokenList.get(sec).getToken(index).setFlag(TokenTable.xFlag, 0); // xFlag에 0 넣어주기
								}
							}
						} else { // 2형식도 4형식도 아니라면 (3형식이라면)
							TokenList.get(sec).getToken(index).setFlag(TokenTable.eFlag, 0); // eFlag에 0 넣어주기 (3형식이므로 확장하지 않음)
							if (TokenList.get(sec).getToken(index).operand[0].startsWith("#")) { // 해당 operand가 "#"로 시작한다면 (즉시 주소 지정 방식)
								TokenList.get(sec).getToken(index).setFlag(TokenTable.nFlag, 0); // nFlag에 0넣어주기
								TokenList.get(sec).getToken(index).setFlag(TokenTable.iFlag, 1); // iFlag에 1 넣어주기
								TokenList.get(sec).getToken(index).setFlag(TokenTable.bFlag, 0); // bFlag에 0 넣어주기 (즉시 주소이므로 상대 주소 사용 안 함)
								TokenList.get(sec).getToken(index).setFlag(TokenTable.pFlag, 0); // pFlag에 0 넣어주기 (즉시 주소이므로 상대 주소 사용 안 함)
								for (int i = 0; i < TokenList.get(sec).getToken(index).operand.length; i++) { // operand 개수 만큼 반복
									if (TokenList.get(sec).getToken(index).operand[i].equals("X")) { // 해당 operand가 X라면 (index)
										TokenList.get(sec).getToken(index).setFlag(TokenTable.xFlag, 1); // xFlag에 1 넣어주기
										break; //X가 하나만 있어도 반복문 종료
									} else // 해당 operand가 X가 아니라면
										TokenList.get(sec).getToken(index).setFlag(TokenTable.xFlag, 0); // xFlag에 0 넣어주기
								}
							} else if (TokenList.get(sec).getToken(index).operand[0].startsWith("@")) { // 해당 operand가 "@"로 시작한다면 (간접 주소 지정 방식)
								TokenList.get(sec).getToken(index).setFlag(TokenTable.nFlag, 1); // nFlag에 1넣어주기
								TokenList.get(sec).getToken(index).setFlag(TokenTable.iFlag, 0); // iFlag에 0넣어주기
								TokenList.get(sec).getToken(index).setFlag(TokenTable.bFlag, 0); // bFlag에 0 넣어주기 (COPY프로그램은 nobase 프로그램)
								TokenList.get(sec).getToken(index).setFlag(TokenTable.pFlag, 1); // bFlag에 0 넣어주기 (control section은 모두 PC상대주소)
								for (int i = 0; i < TokenList.get(sec).getToken(index).operand.length; i++) { // operand 개수 만큼 반복
									if (TokenList.get(sec).getToken(index).operand[i].equals("X")) { // 해당 operand가 X라면 (index)
										TokenList.get(sec).getToken(index).setFlag(TokenTable.xFlag, 1); // xFlag에 1 넣어주기
										break; //X가 하나만 있어도 반복문 종료
									} else // 해당 operand가 X가 아니라면
										TokenList.get(sec).getToken(index).setFlag(TokenTable.xFlag, 0); // xFlag에 0 넣어주기
								}
							} else { // 해당 operand가 "#" 또는 "@"로 시작하지 않는다면  (simple addressing)
								TokenList.get(sec).getToken(index).setFlag(TokenTable.nFlag, 1); // nFlag에 1넣어주기
								TokenList.get(sec).getToken(index).setFlag(TokenTable.iFlag, 1); // iFlag에 1넣어주기
								if (TokenList.get(sec).getToken(index).operand[0].equals("")) { // operand에 문자가 없다면 (주소 지정할 필요 없음)
									TokenList.get(sec).getToken(index).setFlag(TokenTable.xFlag, 0); // xFlag에 0 넣어주기
									TokenList.get(sec).getToken(index).setFlag(TokenTable.bFlag, 0); // bFlag에 0 넣어주기
									TokenList.get(sec).getToken(index).setFlag(TokenTable.pFlag, 0); // pFlag에 0 넣어주기
								} else { // operand에 문자가 있다면
									TokenList.get(sec).getToken(index).setFlag(TokenTable.bFlag, 0); // bFlag에 0 넣어주기 (COPY프로그램은 nobase 프로그램)
									TokenList.get(sec).getToken(index).setFlag(TokenTable.pFlag, 1); // bFlag에 0 넣어주기 (control section은 모두 PC상대주소)
									for (int i = 0; i < TokenList.get(sec).getToken(index).operand.length; i++) { // operand 개수 만큼 반복
										if (TokenList.get(sec).getToken(index).operand[i].equals("X")) { // 해당 operand가 X라면 (index)
											TokenList.get(sec).getToken(index).setFlag(TokenTable.xFlag, 1); // xFlag에 1 넣어주기
											break; //X가 하나만 있어도 반복문 종료
										} else // 해당 operand가 X가 아니라면
											TokenList.get(sec).getToken(index).setFlag(TokenTable.xFlag, 0); // xFlag에 0 넣어주기
									}
								}
							}
						}
					}
				}
			}
		}
		
		/** ObjectCode를 처음부터 끝까지 만들기 위한 반복문 */
		for (int sec = 0; sec < TokenList.size(); sec++) { // TokenList의 크기만큼 반복 (section의 개수만큼)
			for (int index = 0; index < TokenList.get(sec).tokenList.size(); index++) { // 해당 section의 TokenTable의 크기만큼 반복 (해당 section의 line 개수만큼)
				if (TokenList.get(sec).getToken(index).operator != null) { // 해당 operator가 null이 아니라면 (있다면)
					TokenList.get(sec).makeObjectCode(index); // 현재 명령어의 ObjectCode 생성
				}
			}
		}
		
		/** 각 section의 프로그램 길이을 계산하기 위한 반복문 */
		ArrayList<Integer> pLength = new ArrayList<Integer>(); // section 별 프로그램 길이를 넣기 위해 ArrayList 생성
		for (int sec = 0; sec < TokenList.size(); sec++) { // TokenList의 크기만큼 반복 (section의 개수만큼)
			int pLen = 0; // byteSize를 더해서 넣을 변수 초기화
			for (int index = 0; index < TokenList.get(sec).tokenList.size(); index++) { // 해당 section의 TokenTable의 크기만큼 반복 (해당 section의 line 개수만큼)
				pLen += TokenList.get(sec).getToken(index).byteSize; // pLen에 현재 pLen에 byteSize만큼 더해서 넣어주기
			}
			pLength.add(pLen); // pLength에 현재 section의 모든 byteSize를 더한 pLen 추가
		}
		
		/** Object code를 codeList에 저장하기 위한 반복문 */
		String code = ""; // // object code를 넣을 변수 초기화
		ArrayList<String> textList = new ArrayList<String>(); // text record의 object code를 넣기 위해 ArrayList 생성
		for (int sec = 0; sec < TokenList.size(); sec++) { // TokenList의 크기만큼 반복 (section의 개수만큼)
			int sLen = 0; // text record의 문장의 길이를 넣을 변수 초기화
			int sStart = 0; // text record의 문장의 시작주소를 넣을 변수 초기화
			for (int index = 0; index < TokenList.get(sec).tokenList.size(); index++) { // 해당 section의 TokenTable의 크기만큼 반복 (해당 section의 line 개수만큼)
				if (TokenList.get(sec).getToken(index).operator != null) { // 해당 operator가 null이 아니라면 (있다면)
					
					/** Header record */
					if (TokenList.get(sec).getToken(index).operator.equals("START") || TokenList.get(sec).getToken(index).operator.equals("CSECT")) {
						// 해당 operator가 START or CSECT라면 (section의 시작)(header record 작성)
						code = String.format("H%-6s%06X%06X\n", TokenList.get(sec).getToken(index).label, TokenList.get(sec).getToken(index).location, pLength.get(sec));
						// H^프로그램명^시작주소^프로그램 총 길이 포맷으로 code에 넣어주기
						codeList.add(code); // code를 codeList에 추가
						
					/** Define record */
					} else if (TokenList.get(sec).getToken(index).operator.equals("EXTDEF")) { // 해당 operator가 EXTDEF라면 (define record 작성)
						code = "D"; // code에 "D" 넣어주기
						codeList.add(code); // code를 codeList에 추가
						for (int i = 0; i < TokenList.get(sec).getToken(index).operand.length; i++) { // operand 개수 만큼 반복
							String def = TokenList.get(sec).getToken(index).operand[i]; // i번째 operand를 def 변수에 넣어주기
							int address = symtabList.get(sec).search(def); // def의 주소를 symtabList에서 찾아서 address 변수에 넣어주기
							code = String.format("%-6s%06X", def, address); // symbol이름^주소 포맷으로 code에 넣어주기
							codeList.add(code); // code를 codeList에 추가
						}
						code = "\n"; // define record 작성 종료 후 줄바꾸기 위해 "\n"을 code에 넣어주기
						codeList.add(code); // code를 codeList에 추가
						
					/** Refer record */
					} else if (TokenList.get(sec).getToken(index).operator.equals("EXTREF")) { // 해당 operator가 EXTDEF라면 (refer record 작성)
						code = "R"; // code에 "R" 넣어주기
						codeList.add(code); // code를 codeList에 추가
						for (int i = 0; i < TokenList.get(sec).getToken(index).operand.length; i++) { // operand 개수 만큼 반복
							String ref = TokenList.get(sec).getToken(index).operand[i]; // i번째 operand를 ref 변수에 넣어주기
							code = String.format("%-6s", ref); // symbol이름을 code에 넣어주기
							codeList.add(code); // code를 codeList에 추가
						}
						code = "\n"; // refer record 작성 종료 후 줄바꾸기 위해 "\n"을 code에 넣어주기
						codeList.add(code); // code를 codeList에 추가
						
					/** Text record */
					} else { // START, CSECT, EXTDEF, EXTREF 가 아니라면
						if (!TokenList.get(sec).getToken(index).objectCode.equals("")) { // object code가 ""가 아니라면 (비어있지 않다면)
							if (sLen == 0) { // sLen이 0이라면 (text record 시작 명령어)
								sStart = TokenList.get(sec).getToken(index).location; // sStart에 현재 location을 넣어주기
							}
							sLen += TokenList.get(sec).getToken(index).byteSize; // sLen에 byteSize 더해서 넣어주기 (현재 명령어가 문장의 다음 문장의 명령어인지 판별하기 위해 미리 길이를 늘려줌)
							if (sLen > 0x1E) { // sLen이 1E보다 크다면 (다음 문장의 명령어, 한 문장에 올 수 있는 문장의 길이는 최대 1E)
								sLen -= TokenList.get(sec).getToken(index).byteSize; // sLen에서 더했던 byteSize만큼 빼주기 (현재 명령어는 다음 문장의 명령어)
								code = String.format("T%06X%02X", sStart, sLen); // T^문장 시작주소^문장 길이 포맷으로 code에 넣어주기
								codeList.add(code); // code를 codeList에 추가
								for (int i = 0; i < textList.size(); i++) { // textList 처음부터 끝까지 반복
									code = textList.get(i); // textList의 i번째에 있는 내용을 code에 넣어주기
									codeList.add(code); // code를 codeList에 추가
								}
								code = "\n"; // text record 한 문장 종료 후 줄바꾸기 위해 "\n"을 code에 넣어주기
								codeList.add(code); // code를 codeList에 추가
								textList.clear(); // 다음 문장의 text를 넣어주기 위해 textList 비우기
								sStart = TokenList.get(sec).getToken(index).location; // 현재 명령어는 다음 문장의 시작 명령어 이기 때문에 현재 주소를 sStart에 넣어주기
								sLen = TokenList.get(sec).getToken(index).byteSize; // sLen을 현재 명령어의 크기로 초기화
								textList.add(TokenList.get(sec).getToken(index).objectCode); // textList에 현재 명령어의 Object code 추가
							} else if (TokenList.get(sec).tokenList.size() == index + 1	|| TokenList.get(sec).getToken(index + 1).objectCode.equals("")) {
								// 다음 인덱스가 TokenTable의 크기와 같거나 (현재 인덱스가 TokenTable의 마지막 인덱스) 다음 명령어의 object code가 ""라면 (비어있다면)
								textList.add(TokenList.get(sec).getToken(index).objectCode); // textList에 현재 명령어의 Object code 추가
								code = String.format("T%06X%02X", sStart, sLen); // T^문장 시작주소^문장 길이 포맷으로 code에 넣어주기
								codeList.add(code);; // code를 codeList에 추가
								for (int i = 0; i < textList.size(); i++) { // textList 처음부터 끝까지 반복
									code = textList.get(i); // textList의 i번째에 있는 내용을 code에 넣어주기
									codeList.add(code); // code를 codeList에 추가
								}
								code = "\n"; // text record 한 문장 종료 후 줄바꾸기 위해 "\n"을 code에 넣어주기
								codeList.add(code); // code를 codeList에 추가
								sLen = 0; // sLen을 0으로 초기화
								textList.clear(); // 다음 문장의 text를 넣어주기 위해 textList 비우기
							} else { // 위의 조건이 아니라면
								textList.add(TokenList.get(sec).getToken(index).objectCode); // textList에 현재 명령어의 Object code 추가
							}
						}
					}
					if (TokenList.get(sec).tokenList.size() == index + 1) { // 다음 인덱스가 TokenTable의 크기와 같다면 (현재 인덱스가 TokenTable의 마지막 인덱스)
						
						/** Modification record */
						int ref = -1; // EXTREF가 있는 Token의 인덱스를 넣을 변수를 -1로 초기화
						for (int i = 0; i < TokenList.get(sec).tokenList.size(); i++) { // 해당 section의 TokenTable의 크기만큼 반복 (해당 section의 line 개수만큼)
							if (TokenList.get(sec).getToken(i).operator != null) { // 해당 operator가 null이 아니라면 (있다면)
								if (TokenList.get(sec).getToken(i).operator.equals("EXTREF")) { // 해당 operator가 EXTREF라면
									ref = i; // i를 ref에 넣어주기
								}
							}
							char[] cntzero = TokenList.get(sec).getToken(i).objectCode.toCharArray(); // Object code를 한 문자씩 잘라서 배열에 넣기 (끝에 연속된 0의 개수를 세기 위해)
							int cnt = 0; // 0의 개수를 넣을 cnt 변수 초기화
							for (int cnti = TokenList.get(sec).getToken(i).objectCode.length() - 1; cnti > -1; cnti--) { // Object의 끝에서부터 첫번째 문자까지 반복
								if (cntzero[cnti] == '0') { // 해당 문자가 0이라면
									cnt++; //cnt를 1 증가
								} else // 0이 아니라면
									break; // 반복문 종료
							}
							if (ref != -1 && i != ref && TokenList.get(sec).getToken(i).operand != null && !TokenList.get(sec).getToken(i).objectCode.equals("")) {
								// EXTREF가 나오기 전의 Token이 아니고(초기값 -1 리턴) 현재 명령어가 EXTREF이 아니고 operand가 비어있지 않으며 object code가 비어있지 않으면
								
								/* COPY 프로그램에는 "-"만 있어서 뺄셈만 구현 */
								if (TokenList.get(sec).getToken(i).operand[0].contains("-")) { //operand에 "-"가 있다면
									String[] array = TokenList.get(sec).getToken(index).operand[0].split("-"); // "-"를 기준으로 operand를 잘라서 array 배열에 넣기
									for (int k = 0; k < array.length; k++) { // array 배열의 길이만큼 반복
										for (int j = 0; j < TokenList.get(sec).getToken(ref).operand.length; j++) { // EXTREF의 operand 개수만큼 반복
											if (array[k].equals(TokenList.get(sec).getToken(ref).operand[j])) { // array의 k번째 배열에 있는 문자열이 EXTREF의 operand에 있는 문자열이라면
												if (cnt == 6) { // cnt가 6이라면
													code = String.format("M%06X%02X", TokenList.get(sec).getToken(i).location, cnt);
													// M^주소^끝에 있는 0의 개수 포맷으로 code에 넣어주기
													codeList.add(code); // code를 codeList에 추가
												} else if (cnt == 5) { // cnt가 5라면
													code = String.format("M%06X%02X", TokenList.get(sec).getToken(i).location + 1, cnt);
													// M^주소^끝에 있는 0의 개수 포맷으로 code에 넣어주기(0의 개수가 5일때는 location을 1 증가)
													codeList.add(code); // code를 codeList에 추가
												}
												if (k == 0) { // 가장 앞에 있는 문자열이라면 (부호가 없는건 + 생략)
													code = "+"; // code에 "+" 넣어주기
													codeList.add(code); // code를 codeList에 추가
												} else { // 가장 앞에 있는 문자열이아니라면 ("-"를 기준으로 잘랐기 때문에 부호는 -)
													code = "-"; // code에 "+" 넣어주기
													codeList.add(code); // code를 codeList에 추가
												}
												code = String.format("%s\n", array[k]); // array의 k번째 배열을 code에 넣어주기
												codeList.add(code); // code를 codeList에 추가
											}
										}
									}
								} else { //operand에 "-"가 없다면
									for (int j = 0; j < TokenList.get(sec).getToken(ref).operand.length; j++) { // EXTREF의 operand 개수만큼 반복
										if (TokenList.get(sec).getToken(i).operand[0].equals(TokenList.get(sec).getToken(ref).operand[j])) {
											 // 현재 명령어의 operand가 EXTREF의 operand에 있는 문자열이라면
											if (cnt == 6) { // cnt가 6이라면
												code = String.format("M%06X%02X+%s\n", TokenList.get(sec).getToken(i).location, cnt, TokenList.get(sec).getToken(i).operand[0]);
												// M^주소^끝에 있는 0의 개수^+현재 명령어의 operand 포맷으로 code에 넣어주기
												codeList.add(code); // code를 codeList에 추가
											} else if (cnt == 5) { // cnt가 5라면
												code = String.format("M%06X%02X+%s\n", TokenList.get(sec).getToken(i).location + 1, cnt, TokenList.get(sec).getToken(i).operand[0]);
												// M^주소^끝에 있는 0의 개수^+현재 명령어의 operand 포맷으로 code에 넣어주기(0의 개수가 5일때는 location을 1 증가)
												codeList.add(code); // code를 codeList에 추가
											}
										}
									}
								}
							}
						}
						
						/** End record */
						code = "E"; // code에 "E" 넣어주기
						codeList.add(code); // code를 codeList에 추가
						
						/** START가 있는 section만 End record에 시작주소를 출력하기 위한 반복문 */
						for (int i = 0; i < TokenList.get(sec).tokenList.size(); i++) { // 해당 section의 TokenTable의 크기만큼 반복 (해당 section의 line 개수만큼)
							if (TokenList.get(sec).getToken(i).operator != null) { // 해당 operator가 null이 아니라면 (있다면)
								if (TokenList.get(sec).getToken(i).operator.equals("START")) { // 해당 operator가 START 라면
									code = String.format("%06X", TokenList.get(sec).getToken(i).location); // 현재 주소를 code에 넣기
									codeList.add(code); // code를 codeList에 추가
									break; // 반복문 종료
								}
							}
						}
					}
				}
			}
			code = "\n\n"; // 한 section 작성 종료 후 한줄 띄우기 위해 "\n\n"을 code에 넣어주기
			codeList.add(code); // code를 codeList에 추가
		}
	}

	/**
	 * 작성된 codeList를 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printObjectCode(String fileName) {
		try {
			File file = new File(fileName); // 파일 객체 생성
			PrintWriter pw = new PrintWriter(file); // 출력 스트림 생성
			for (int i = 0; i < codeList.size(); i++) { // codeList의 크기만큼 반복
				pw.print(codeList.get(i)); // 순서대로 codeList 파일 출력
			}
			pw.close(); // 출력 스트림 닫기
		} catch (IOException e) { // 예외 발생시
			System.out.println("작성할 파일이 없습니다."); // 파일이 없다고 출력
		}
	}

}
