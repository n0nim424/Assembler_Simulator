import java.util.ArrayList;

/**
 * 사용자가 작성한 프로그램 코드를 단어별로 분할 한 후, 의미를 분석하고, 최종 코드로 변환하는 과정을 총괄하는 클래스이다.
 * pass2에서 object code로 변환하는 과정은 혼자 해결할 수 없고 symbolTable과 instTable의 정보가 필요하므로 이를 링크시킨다.
 * section 마다 인스턴스가 하나씩 할당된다.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND = 3;

	/** bit 조작의 가독성을 위한 선언 */
	public static final int nFlag = 32;
	public static final int iFlag = 16;
	public static final int xFlag = 8;
	public static final int bFlag = 4;
	public static final int pFlag = 2;
	public static final int eFlag = 1;

	/** Token을 다룰 때 필요한 테이블들을 링크시킨다. */
	SymbolTable symTab;
	LiteralTable literalTab;
	InstTable instTab;

	/** 각 line을 의미별로 분할하고 분석하는 공간. */
	ArrayList<Token> tokenList;

	/**
	 * 초기화하면서 symTable과 literalTable과 instTable을 링크시킨다.
	 * @param symTab     : 해당 section과 연결되어있는 symbol table
	 * @param literalTab : 해당 section과 연결되어있는 literal table
	 * @param instTab    : instruction 명세가 정의된 instTable
	 */
	public TokenTable(SymbolTable symTab, LiteralTable literalTab, InstTable instTab) {
		tokenList = new ArrayList<Token>(); // 새로운 tokenList 생성
		this.symTab = symTab; // 현재 class의 symTab에 인자 symTab 넣어주기
		this.literalTab = literalTab; // 현재 class의literalTab에 인자 literalTab 넣어주기
		this.instTab = instTab; // 현재 class의 instTab에 인자 instTab 넣어주기
	}

	/**
	 * 일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
	 * @param line : 분리되지 않은 일반 문자열
	 */
	public void putToken(String line) {
		tokenList.add(new Token(line)); // tokenList에 새로운 token(인자로 들어온 line을 parsing)을 만들어서 추가
	}

	/**
	 * tokenList에서 index에 해당하는 Token을 리턴한다.
	 * @param index
	 * @return : index번호에 해당하는 코드를 분석한 Token 클래스
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}

	/**
	 * Pass2 과정에서 사용한다. 
	 * instruction table, symbol table literal table 등을 참조하여 objectcode를 생성하고, 이를 저장한다.
	 * @param index
	 */
	public void makeObjectCode(int index) {
		int ref = -1;  // EXTREF가 있는 Token의 인덱스를 넣을 변수를 -1로 초기화
		for (int i = 0; i < tokenList.size(); i++) {// tokenList의 크기만큼 반복 (line 개수만큼)
			if (getToken(i).operator != null) { // 해당 operator가 null이 아니라면 (있다면)
				if (getToken(i).operator.equals("EXTREF")) { // 해당 operator가 EXTREF라면
					ref = i; // i를 ref에 넣어주기
				}
			}
		}
		if (instTab.search(getToken(index).operator) > -1) { // instTable에 있는 명령어라면 (없으면 -1 return)
			if (getToken(index).operator.startsWith("+")) { // 해당 operator가 "+"로 시작한다면 (형식이 4형식이라면)(주소가 5자리)
				getToken(index).objectCode = String.format("%02X", instTab.search(getToken(index).operator) + (getToken(index).nixbpe >> 4));
				// operator의 opcode를 찾고 nixbpe의 상위 2비트만 더해서 objectCode에 넣기
				getToken(index).objectCode += String.format("%01X", 15 & getToken(index).nixbpe); // nixbpe의 하위 4비트를 계산해서 objectcode 뒤에 넣어주기
				if (!getToken(index).operand[0].equals("")) { // operand에 문자가 없지 않다면 (주소 지정)
					if (getToken(index).operand[0].startsWith("#")) { // 해당 operand가 "#"로 시작한다면 (즉시 주소 지정 방식)
						getToken(index).objectCode += String.format("%05X", Integer.parseInt(getToken(index).operand[0].substring(1)));
						// "#"을 제거한 operand를 정수형태로 변환하여 objectcode 뒤에 넣어주기
					} else if (getToken(index).operand[0].startsWith("@")) { // 해당 operand가 "@"로 시작한다면 (간접 주소 지정 방식)
						getToken(index).objectCode += String.format("%05X", symTab.search(getToken(index).operand[0].substring(1)) - getToken(index + 1).location);
						// "@"을 제거한 operand를 symTab에서 주소를 찾아 명령어의 다음 주소를 빼서 objectcode 뒤에 넣어주기
					} else if (getToken(index).operand[0].startsWith("=")) { // 해당 operand가 "="로 시작한다면
						for (int j = 0; j < literalTab.literalList.size(); j++) { // literalList의 크기만큼 반복
							if (getToken(index).operand[0].substring(3, getToken(index).operand[0].length() - 1).equals(literalTab.literalList.get(j))) {
								// 해당 operand에서 "=", "C" or "X", 따옴표를 제외하고 literalList와 비교하여 같으면
								getToken(index).objectCode += String.format("%05X", literalTab.locationList.get(j) - getToken(index + 1).location);
								// 해당 literal의 주소를  objectcode 뒤에 넣어주기
							}
						}
					} else { // operand 앞에 붙은 게 없다면
						if (symTab.search(getToken(index).operand[0]) > -1) { // 해당 operand가 symTab에 있다면 (없으면 -1 리턴)
							if (symTab.search(getToken(index).operand[0]) >= getToken(index + 1).location) { // operand가 가리키는 symbol의 주소 (TA)가 다음 명령어의 주소(PC)보다 크거나 같다면
								getToken(index).objectCode += String.format("%05X", symTab.search(getToken(index).operand[0]) - getToken(index + 1).location);
								// TA-PC를  objectcode 뒤에 넣어주기
							} else { // operand가 가리키는 symbol의 주소 (TA)가 다음 명령어의 주소(PC)보다 작다면
								getToken(index).objectCode += String.format("%05X", 0XFFFFF & (symTab.search(getToken(index).operand[0]) + (~getToken(index + 1).location + 1)));
								// TA와 (-PC)를 더해서 하위 5개 숫자만  objectcode 뒤에 넣어주기
							}
						} else { // 해당 operand가 symTab에 없다면
							for (int j = 0; j < getToken(ref).operand.length; j++) { // EXTREF의 operand 개수만큼 반복
								if (getToken(index).operand[0].equals(getToken(ref).operand[j])) { // 해당 operand가 EXTREF의 operand에 있는 문자열이라면
									getToken(index).objectCode += String.format("%05X", 0); // 0을   objectcode 뒤에 넣어주기
								}
							}
						}
					}
				} else { // operand에 문자가 없다면
					getToken(index).objectCode += String.format("%05X", 0); // 0을   objectcode 뒤에 넣어주기
				}
			} else if (instTab.getformat(getToken(index).operator) == 2) { // 해당 operator의 형식이 2형식이라면
				getToken(index).objectCode = String.format("%02X", instTab.search(getToken(index).operator));
				// operator의 opcode를 찾아서 objectCode에 넣기
				for (int j = 0; j < 2; j++) { // 2번 반복(2형식이라서 주소가 2자리)
					if (getToken(index).operand.length > j) { // operand 개수가 j보다 많다면
						switch (getToken(index).operand[j]) { // j번째 operand가
						case "A": // A라면
							getToken(index).objectCode += String.format("%01X", 0); // objectcode 뒤에 0 넣어주기
							break; // swith 종료
						case "X": // X라면
							getToken(index).objectCode += String.format("%01X", 1); // objectcode 뒤에 1 넣어주기
							break; // swith 종료
						case "S": // S라면
							getToken(index).objectCode += String.format("%01X", 4); // objectcode 뒤에 4 넣어주기
							break; // swith 종료
						case "T": // T라면
							getToken(index).objectCode += String.format("%01X", 5); // objectcode 뒤에 5 넣어주기
							break; // swith 종료
						}
					} else // operand 개수가 j보다 많지 않다면
						getToken(index).objectCode += String.format("%01X", 0); // objectcode 뒤에 0 넣어주기
				}
			} else { // 2형식도 4형식도 아니라면 (3형식이라면)(주소가 3자리)
				getToken(index).objectCode = String.format("%02X", instTab.search(getToken(index).operator) + (getToken(index).nixbpe >> 4));
				// operator의 opcode를 찾고 nixbpe의 상위 2비트만 더해서 objectCode에 넣기
				getToken(index).objectCode += String.format("%01X", 15 & getToken(index).nixbpe); // nixbpe의 하위 4비트를 계산해서 objectcode 뒤에 넣어주기
				if (!getToken(index).operand[0].equals("")) { // operand에 문자가 없지 않다면 (주소 지정)
					if (getToken(index).operand[0].startsWith("#")) { // 해당 operand가 "#"로 시작한다면 (즉시 주소 지정 방식)
						getToken(index).objectCode += String.format("%03X", Integer.parseInt(getToken(index).operand[0].substring(1)));
						// "#"을 제거한 operand를 정수형태로 변환하여 objectcode 뒤에 넣어주기
					} else if (getToken(index).operand[0].startsWith("@")) { // 해당 operand가 "@"로 시작한다면 (간접 주소 지정 방식)
						getToken(index).objectCode += String.format("%03X", symTab.search(getToken(index).operand[0].substring(1)) - getToken(index + 1).location);
						// "@"을 제거한 operand를 symTab에서 주소를 찾아 명령어의 다음 주소를 빼서 objectcode 뒤에 넣어주기
					} else if (getToken(index).operand[0].startsWith("=")) { // 해당 operand가 "="로 시작한다면
						for (int j = 0; j < literalTab.literalList.size(); j++) { // literalList의 크기만큼 반복
							if (getToken(index).operand[0].substring(3, getToken(index).operand[0].length() - 1).equals(literalTab.literalList.get(j))) {
								// 해당 operand에서 "=", "C" or "X", 따옴표를 제외하고 literalList와 비교하여 같으면
								getToken(index).objectCode += String.format("%03X", literalTab.locationList.get(j) - getToken(index + 1).location);
								// 해당 literal의 주소를  objectcode 뒤에 넣어주기
							}
						}
					} else { // operand 앞에 붙은 게 없다면
						if (symTab.search(getToken(index).operand[0]) > -1) { // 해당 operand가 symTab에 있다면 (없으면 -1 리턴)
							if (symTab.search(getToken(index).operand[0]) >= getToken(index + 1).location) { // operand가 가리키는 symbol의 주소 (TA)가 다음 명령어의 주소(PC)보다 크거나 같다면
								getToken(index).objectCode += String.format("%03X", symTab.search(getToken(index).operand[0]) - getToken(index + 1).location);
								// TA-PC를  objectcode 뒤에 넣어주기
							} else { // operand가 가리키는 symbol의 주소 (TA)가 다음 명령어의 주소(PC)보다 작다면
								getToken(index).objectCode += String.format("%03X", 0XFFF & (symTab.search(getToken(index).operand[0]) + (~getToken(index + 1).location + 1)));
								// TA와 (-PC)를 더해서 하위 5개 숫자만  objectcode 뒤에 넣어주기
							}
						} else { // 해당 operand가 symTab에 없다면
							for (int j = 0; j < getToken(ref).operand.length; j++) { // EXTREF의 operand 개수만큼 반복
								if (getToken(index).operand[0].equals(getToken(ref).operand[j])) { // 해당 operand가 EXTREF의 operand에 있는 문자열이라면
									getToken(index).objectCode += String.format("%03X", 0); // 0을   objectcode 뒤에 넣어주기
								}
							}
						}
					}
				} else { // operand에 문자가 없다면
					getToken(index).objectCode += String.format("%03X", 0); // 0을   objectcode 뒤에 넣어주기
				}
			}
		} else if (getToken(index).operator.equals("WORD")) { // 해당 operator가 WORD라면 (WORD는 3bytes 명령어이므로 주소가 6자리)
			
			/* COPY 프로그램에는 "-"만 있어서 뺄셈만 구현 */
			String[] array = getToken(index).operand[0].split("-"); // "-"를 기준으로 operand를 잘라서 array 배열에 넣기
			int ob = -1; // operand로 주소를 계산해서 넣을 변수 -1로 초기화
			for (int i = 0; i < array.length; i++) { // array 배열의 크기만큼 반복
				if (symTab.search(array[i]) > -1) { // array의 i번째 배열에 있는 문자열이 symTab에 있다면
					if (ob < 0) { // ob가 초기값이라면 (i가 0이라면)
						ob = symTab.search(array[i]); // ob에 0번째 배열에 있는 symbol의 주소 넣기
					} else { // ob가 초기값이 아니라면 (i가 0이 아니라면)
						ob -= symTab.search(array[i]); // ob에서 i번째 배열에 있는 symbol의 주소를 빼서 ob에 넣기
					}
					getToken(index).objectCode = String.format("%06X", ob); // objectcode를 ob로 초기화 
				} else { // array의 i번째 배열에 있는 문자열이 symTab에 없다면
					getToken(index).objectCode = String.format("%06X", 0); // objectcode를 0으로 초기화 
					break; // 반복문 종료
				}
			}
		} else if (getToken(index).operator.equals("BYTE")) { // 해당 operator가 BYTE라면
			if (getToken(index).operand[0].startsWith("C")) { // 해당 operand가 C로 시작한다면 (문자라면)
				char[] array = literalTab.literalList.get(index).substring(2, getToken(index).operand[0].length() - 1).toCharArray();
				//"C"와 따옴표를 제외한 operand를 한 문자씩 잘라서 array에 넣어주기
				for (int j = 0; j < array.length; j++) { // array 배열의 크기만큼 반복
					getToken(index).objectCode += String.format("%X", (byte) array[j]); // 한문자씩 자른 operand를 아스키 코드로 변환하여 objectcode 뒤에 넣어주기
				}
			}
			if (getToken(index).operand[0].startsWith("X")) { // 해당 operand가 X로 시작한다면 (16진수 정수라면)
				getToken(index).objectCode = String.format("%s", getToken(index).operand[0].substring(2, getToken(index).operand[0].length() - 1));
				//"X"와 따옴표를 제외한 operand를 objectcode에 넣어주기
			}
		} else if (getToken(index).operator.equals("LTORG")) { // 해당 operator가 LTORG라면 
			for (int i = 0; i < literalTab.literalList.size(); i++) { // literalList의 크기만큼 반복
				if (getToken(index).location == literalTab.locationList.get(i)) { // 현재 주소와 i번째 literal의 주소가 같으면
					if (getToken(index).byteSize == literalTab.literalList.get(i).length()) { // 현재 명령어의 크기와 해당 literal의 길이가 같으면 (문자라면)
						char[] array = literalTab.literalList.get(i).toCharArray(); // 해당 literal을 한 문자씩 잘라서 array에 넣어주기
						for (int j = 0; j < array.length; j++) { // array 배열의 크기만큼 반복
							getToken(index).objectCode += String.format("%X", (byte) array[j]); // 한문자씩 자른  literal를 아스키 코드로 변환하여 objectcode 뒤에 넣어주기
						}
					} else { // 현재 명령어의 크기와 i번째 literal의 길이가 같지 않으면 (16진수 정수라면)
						getToken(index).objectCode = String.format("%s", literalTab.literalList.get(i)); // 해당 literal을 objectcode에 넣어주기
					}
				}
			}
		} else if (getToken(index).operator.equals("END")) { // 해당 operator가 END라면 
			for (int i = 0; i < literalTab.literalList.size(); i++) { // literalList의 크기만큼 반복
				if (getToken(index).location == literalTab.locationList.get(i)) { // 현재 주소와 i번째 literal의 주소가 같으면
					if (getToken(index).byteSize == literalTab.literalList.get(i).length()) { // 현재 명령어의 크기와 해당 literal의 길이가 같으면 (문자라면)
						char[] array = literalTab.literalList.get(i).toCharArray(); // 해당 literal을 한 문자씩 잘라서 array에 넣어주기
						for (int j = 0; j < array.length; j++) { // array 배열의 크기만큼 반복
							getToken(index).objectCode += String.format("%02X", (byte) array[j]); // 한문자씩 자른  literal를 아스키 코드로 변환하여 objectcode 뒤에 넣어주기
						}
					} else { // 현재 명령어의 크기와 i번째 literal의 길이가 같지 않으면 (16진수 정수라면)
						getToken(index).objectCode = String.format("%s", literalTab.literalList.get(i)); // 해당 literal을 objectcode에 넣어주기
					}
				}
			}
		} else // 위의 조건이 아니라면
			return; // 종료
	}

	/**
	 * index번호에 해당하는 object code를 리턴한다.
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}

}

/**
 * 각 라인별로 저장된 코드를 단어 단위로 분할한 후 의미를 해석하는 데에 사용되는 변수와 연산을 정의한다. 
 * 의미 해석이 끝나면 pass2에서 object code로 변형되었을 때의 바이트 코드 역시 저장한다.
 */
class Token {
	/** 의미 분석 단계에서 사용되는 변수들 */
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;

	/** object code 생성 단계에서 사용되는 변수들 */
	String objectCode;
	int byteSize;

	/**
	 * 클래스를 초기화 하면서 바로 line의 의미 분석을 수행한다.
	 * @param line 문장단위로 저장된 프로그램 코드
	 */
	public Token(String line) {
		location = 0; 
		nixbpe = 0;
		objectCode = "";
		byteSize = 0;
		parsing(line);
	}

	/**
	 * line의 실질적인 분석을 수행하는 함수. Token의 각 변수에 분석한 결과를 저장한다.
	 * @param line 문장단위로 저장된 프로그램 코드.
	 */
	public void parsing(String line) {
		String[] array = line.split("\t"); // "\t"을 기준으로 line을 잘라서 array 배열에 넣기
		String oper = ""; // operand 배열에 넣기 전에 parsing할 operand를 넣을 변수 선언 및 초기화
		if (!array[0].equals("")) { // 0번째 array 배열이 비어있지 않다면
			label = array[0]; // 해당 배열을 label에 넣어주기
		}
		if (array.length > 1) { // array의 크기가 1보다 크다면
			if (array[1] != null) { // 1번째 array 배열이 null이 아니라면
				operator = array[1]; // 해당 배열을 operator에 넣어주기
			}
			if (array.length > 2) { // array의 크기가 2보다 크다면
				if (array[2] != null) { // 2번째 array 배열이 null이 아니라면
					oper = array[2]; // 해당 배열을 oper에 넣어주기
					operand = oper.split(","); // ","를 기준으로 oper를 잘라서 operand 배열에 넣기
				}
				if (array.length > 3) { // array의 크기가 3보다 크다면
					if (array[3] != null) { // 3번째 array 배열이 null이 아니라면
						comment = array[3]; // 해당 배열을 comment에 넣어주기
					}
				}
			}
		}
	}

	/**
	 * n,i,x,b,p,e flag를 설정한다.
	 * 
	 * 사용 예 : setFlag(nFlag, 1); 
	 * 	또는 	  setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag  : 원하는 비트 위치
	 * @param value : 집어넣고자 하는 값. 1또는 0으로 선언한다.
	 */
	public void setFlag(int flag, int value) {
		nixbpe += (char) (flag * value); // 정수형의 flag와 value를 char형으로 변환하여(2진수 계산을 하기 위해) 기존 nixbpe와 더해서 nixbpe에 넣어주기
	}

	/**
	 * 원하는 flag들의 값을 얻어올 수 있다. flag의 조합을 통해 동시에 여러개의 플래그를 얻는 것 역시 가능하다
	 * 
	 * 사용 예 : getFlag(nFlag) 
	 * 	또는 	  getFlag(nFlag|iFlag)
	 * 
	 * @param flags : 값을 확인하고자 하는 비트 위치
	 * @return : 비트위치에 들어가 있는 값. 플래그별로 각각 32, 16, 8, 4, 2, 1의 값을 리턴할 것임.
	 */
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
}
