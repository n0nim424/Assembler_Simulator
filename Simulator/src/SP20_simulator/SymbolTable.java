package SP20_simulator;
import java.util.ArrayList;

/**
 * symbol과 관련된 데이터와 연산을 소유한다.
 * section 별로 하나씩 인스턴스를 할당한다.
 */
public class SymbolTable {
	ArrayList<String> symbolList;
	ArrayList<Integer> addressList;
	// 기타 literal, external 선언 및 처리방법을 구현한다.
	

	/**
	 * 새로운 Symbol을 table에 추가한다.
	 * @param symbol : 새로 추가되는 symbol의 label
	 * @param address : 해당 symbol이 가지는 주소값
	 * <br><br>
	 * 주의 : 만약 중복된 symbol이 putSymbol을 통해서 입력된다면 이는 프로그램 코드에 문제가 있음을 나타낸다. 
	 * 매칭되는 주소값의 변경은 modifySymbol()을 통해서 이루어져야 한다.
	 */
	public void putSymbol(String symbol, int address) {
		int err = 0; // 중복 symbol을 판별하기 위한 변수
		if (symbolList != null) { // symbolList가 null이 아니라면 (비어있지 않다면)
			for (int i = 0; i < symbolList.size(); i++) { // symbolList의 크기만큼 반복
				if (symbol.equals(symbolList.get(i))) { // 인자로 전달된 symbol이 symbolList에 i번째 문자열과 같다면
					System.out.println("중복된 symbol입니다."); // 오류메세지 출력
					err = -1; // err에 -1 넣기
				}
			}
		} else { // symbolList가 null이라면 (비어있다면)
			symbolList = new ArrayList<String>(); // 새로운 symbolList 생성
			addressList = new ArrayList<Integer>(); // 새로운 addressList 생성
		}
		if (err != -1) { // 중복된 symbol이 아니라면
			symbolList.add(symbol); // symbolList에 symbol 추가
			addressList.add(address); // addressList에 address 추가
		}
	}
	
	/**
	 * 기존에 존재하는 symbol 값에 대해서 가리키는 주소값을 변경한다.
	 * @param symbol : 변경을 원하는 symbol의 label
	 * @param newaddress : 새로 바꾸고자 하는 주소값
	 */
	public void modifySymbol(String symbol, int newaddress) {
		for (int i = 0; i < symbolList.size(); i++) { // symbolList의 크기만큼 반복
			if (symbol.equals(symbolList.get(i))) { // 인자로 전달된 symbol이 symbolList에 i번째 문자열과 같다면
				addressList.add(i, newaddress); // addressList의 i번째에 newaddress 넣어주기
			}
		}
	}
	
	/**
	 * 인자로 전달된 symbol이 어떤 주소를 지칭하는지 알려준다. 
	 * @param symbol : 검색을 원하는 symbol의 label
	 * @return symbol이 가지고 있는 주소값. 해당 symbol이 없을 경우 -1 리턴
	 */
	public int search(String symbol) {
		int address = -1; // symbol이 없을 경우 -1을 리턴하기 위해 초기값을 0에서 -1로 변경
		for (int i = 0; i < symbolList.size(); i++) { // symbolList의 크기만큼 반복
			if (symbol.equals(symbolList.get(i))) { // 인자로 전달된 symbol이 symbolList에 i번째 문자열과 같다면
				address = addressList.get(i); // 해당 주소를 address에 넣어주기
				break; // 반복문 종료
			}
		}
		return address;
	}
	
	
	
}
