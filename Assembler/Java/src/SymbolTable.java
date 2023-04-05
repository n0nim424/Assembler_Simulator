import java.util.ArrayList;

/**
 * symbol�� ���õ� �����Ϳ� ������ �����Ѵ�. 
 * section ���� �ϳ��� �ν��Ͻ��� �Ҵ��Ѵ�.
 */
public class SymbolTable {
	ArrayList<String> symbolList;
	ArrayList<Integer> locationList;

	/**
	 * ���ο� Symbol�� table�� �߰��Ѵ�.
	 * 
	 * @param symbol   : ���� �߰��Ǵ� symbol�� label
	 * @param location : �ش� symbol�� ������ �ּҰ� 
	 * ���� : ���� �ߺ��� symbol�� putSymbol�� ���ؼ� �Էµȴٸ� �̴� ���α׷� �ڵ忡 ������ ������ ��Ÿ����. 
	 * ��Ī�Ǵ� �ּҰ��� ������ modifySymbol()�� ���ؼ� �̷������ �Ѵ�.
	 */
	public void putSymbol(String symbol, int location) {
		int err = 0; // �ߺ� symbol�� �Ǻ��ϱ� ���� ����
		if (symbolList != null) { // symbolList�� null�� �ƴ϶�� (������� �ʴٸ�)
			for (int i = 0; i < symbolList.size(); i++) { // symbolList�� ũ�⸸ŭ �ݺ�
				if (symbol.equals(symbolList.get(i))) { // ���ڷ� ���޵� symbol�� symbolList�� i��° ���ڿ��� ���ٸ�
					System.out.println("�ߺ��� symbol�Դϴ�."); // �����޼��� ���
					err = -1; // err�� -1 �ֱ�
				}
			}
		} else { // symbolList�� null�̶�� (����ִٸ�)
			symbolList = new ArrayList<String>(); // ���ο� symbolList ����
			locationList = new ArrayList<Integer>(); // ���ο� locationList ����
		}
		if (err != -1) { // �ߺ��� symbol�� �ƴ϶��
			symbolList.add(symbol); // symbolList�� symbol �߰�
			locationList.add(location); // locationList�� location �߰�
		}
	}

	/**
	 * ������ �����ϴ� symbol ���� ���ؼ� ����Ű�� �ּҰ��� �����Ѵ�.
	 * @param symbol      : ������ ���ϴ� symbol�� label
	 * @param newLocation : ���� �ٲٰ��� �ϴ� �ּҰ�
	 */
	public void modifySymbol(String symbol, int newLocation) {
		for (int i = 0; i < symbolList.size(); i++) { // symbolList�� ũ�⸸ŭ �ݺ�
			if (symbol.equals(symbolList.get(i))) { // ���ڷ� ���޵� symbol�� symbolList�� i��° ���ڿ��� ���ٸ�
				locationList.add(i, newLocation); // locationList�� i��°�� newLocation �־��ֱ�
			}
		}
	}

	/**
	 * ���ڷ� ���޵� symbol�� � �ּҸ� ��Ī�ϴ��� �˷��ش�.
	 * @param symbol : �˻��� ���ϴ� symbol�� label
	 * @return symbol�� ������ �ִ� �ּҰ�. �ش� symbol�� ���� ��� -1 ����
	 */
	public int search(String symbol) {
		int address = -1; // symbol�� ���� ��� -1�� �����ϱ� ���� �ʱⰪ�� 0���� -1�� ����
		for (int i = 0; i < symbolList.size(); i++) { // symbolList�� ũ�⸸ŭ �ݺ�
			if (symbol.equals(symbolList.get(i))) { // ���ڷ� ���޵� symbol�� symbolList�� i��° ���ڿ��� ���ٸ�
				address = locationList.get(i); // �ش� �ּҸ� address�� �־��ֱ�
				break; // �ݺ��� ����
			}
		}
		return address;
	}

}
