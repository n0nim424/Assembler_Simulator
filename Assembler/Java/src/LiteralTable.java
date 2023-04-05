import java.util.ArrayList;

/**
 * literal과 관련된 데이터와 연산을 소유한다. 
 * section 별로 하나씩 인스턴스를 할당한다.
 */
public class LiteralTable {
	ArrayList<String> literalList;
	ArrayList<Integer> locationList;

	/**
	 * 새로운 Literal을 table에 추가한다.
	 * 
	 * @param literal  : 새로 추가되는 literal의 label
	 * @param location : 해당 literal이 가지는 주소값 
	 * 주의 : 만약 중복된 literal이 putLiteral을 통해서 입력된다면 이는 프로그램 코드에 문제가 있음을 나타낸다. 
	 * 매칭되는 주소값의 변경은 modifyLiteral()을 통해서 이루어져야 한다.
	 */
	public void putLiteral(String literal, int location) {
		int err = 0; // 중복 literal을 판별하기 위한 변수
		if (literalList != null) { // literalList가 null이 아니라면 (비어있지 않다면)
			for (int i = 0; i < literalList.size(); i++) { // literalList의 크기만큼 반복
				if (literal.equals(literalList.get(i))) { // 인자로 전달된 literal이 literalList에 i번째 문자열과 같다면
					System.out.println("중복된 literal입니다."); // 오류메세지 출력
					err = -1; // err에 -1 넣기
				}
			}
		} else { // literalList가 null이라면 (비어있다면)
			literalList = new ArrayList<String>(); // 새로운 literalList 생성
			locationList = new ArrayList<Integer>(); // 새로운 locationList 생성
		}
		if (err != -1) { // 중복된 literal이 아니라면
			literalList.add(literal); // literalList에 literal 추가
			locationList.add(location); // locationList에 location 추가
		}
	}

	/**
	 * 기존에 존재하는 literal 값에 대해서 가리키는 주소값을 변경한다.
	 * @param literal     : 변경을 원하는 literal의 label
	 * @param newLocation : 새로 바꾸고자 하는 주소값
	 */
	public void modifyLiteral(String literal, int newLocation) {
		for (int i = 0; i < literalList.size(); i++) { // literalList의 크기만큼 반복
			if (literal.equals(literalList.get(i))) { // 인자로 전달된 literal이 literalList에 i번째 문자열과 같다면
				locationList.add(i, newLocation); // locationList의 i번째에 newLocation 넣어주기
			}
		}
	}

	/**
	 * 인자로 전달된 literal이 어떤 주소를 지칭하는지 알려준다.
	 * @param literal : 검색을 원하는 literal의 label
	 * @return literal이 가지고 있는 주소값. 해당 literal이 없을 경우 -1 리턴
	 */
	public int search(String literal) {
		int address = -1; // literal이 없을 경우 -1을 리턴하기 위해 초기값을 0에서 -1로 변경
		for (int i = 0; i < literalList.size(); i++) { // literalList의 크기만큼 반복
			if (literal.equals(literalList.get(i))) { // 인자로 전달된 literal이 literalList에 i번째 문자열과 같다면
				address = locationList.get(i); // 해당 주소를 address에 넣어주기
				break; // 반복문 종료
			}
		}
		return address;
	}
}
