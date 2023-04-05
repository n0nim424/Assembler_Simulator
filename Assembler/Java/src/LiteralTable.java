import java.util.ArrayList;

/**
 * literal�� ���õ� �����Ϳ� ������ �����Ѵ�. 
 * section ���� �ϳ��� �ν��Ͻ��� �Ҵ��Ѵ�.
 */
public class LiteralTable {
	ArrayList<String> literalList;
	ArrayList<Integer> locationList;

	/**
	 * ���ο� Literal�� table�� �߰��Ѵ�.
	 * 
	 * @param literal  : ���� �߰��Ǵ� literal�� label
	 * @param location : �ش� literal�� ������ �ּҰ� 
	 * ���� : ���� �ߺ��� literal�� putLiteral�� ���ؼ� �Էµȴٸ� �̴� ���α׷� �ڵ忡 ������ ������ ��Ÿ����. 
	 * ��Ī�Ǵ� �ּҰ��� ������ modifyLiteral()�� ���ؼ� �̷������ �Ѵ�.
	 */
	public void putLiteral(String literal, int location) {
		int err = 0; // �ߺ� literal�� �Ǻ��ϱ� ���� ����
		if (literalList != null) { // literalList�� null�� �ƴ϶�� (������� �ʴٸ�)
			for (int i = 0; i < literalList.size(); i++) { // literalList�� ũ�⸸ŭ �ݺ�
				if (literal.equals(literalList.get(i))) { // ���ڷ� ���޵� literal�� literalList�� i��° ���ڿ��� ���ٸ�
					System.out.println("�ߺ��� literal�Դϴ�."); // �����޼��� ���
					err = -1; // err�� -1 �ֱ�
				}
			}
		} else { // literalList�� null�̶�� (����ִٸ�)
			literalList = new ArrayList<String>(); // ���ο� literalList ����
			locationList = new ArrayList<Integer>(); // ���ο� locationList ����
		}
		if (err != -1) { // �ߺ��� literal�� �ƴ϶��
			literalList.add(literal); // literalList�� literal �߰�
			locationList.add(location); // locationList�� location �߰�
		}
	}

	/**
	 * ������ �����ϴ� literal ���� ���ؼ� ����Ű�� �ּҰ��� �����Ѵ�.
	 * @param literal     : ������ ���ϴ� literal�� label
	 * @param newLocation : ���� �ٲٰ��� �ϴ� �ּҰ�
	 */
	public void modifyLiteral(String literal, int newLocation) {
		for (int i = 0; i < literalList.size(); i++) { // literalList�� ũ�⸸ŭ �ݺ�
			if (literal.equals(literalList.get(i))) { // ���ڷ� ���޵� literal�� literalList�� i��° ���ڿ��� ���ٸ�
				locationList.add(i, newLocation); // locationList�� i��°�� newLocation �־��ֱ�
			}
		}
	}

	/**
	 * ���ڷ� ���޵� literal�� � �ּҸ� ��Ī�ϴ��� �˷��ش�.
	 * @param literal : �˻��� ���ϴ� literal�� label
	 * @return literal�� ������ �ִ� �ּҰ�. �ش� literal�� ���� ��� -1 ����
	 */
	public int search(String literal) {
		int address = -1; // literal�� ���� ��� -1�� �����ϱ� ���� �ʱⰪ�� 0���� -1�� ����
		for (int i = 0; i < literalList.size(); i++) { // literalList�� ũ�⸸ŭ �ݺ�
			if (literal.equals(literalList.get(i))) { // ���ڷ� ���޵� literal�� literalList�� i��° ���ڿ��� ���ٸ�
				address = locationList.get(i); // �ش� �ּҸ� address�� �־��ֱ�
				break; // �ݺ��� ����
			}
		}
		return address;
	}
}
