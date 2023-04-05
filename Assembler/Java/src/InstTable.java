import java.util.HashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * ��� instruction�� ������ �����ϴ� Ŭ����. instruction data���� �����Ѵ�
 * ���� instruction ���� ����, ���� ��� ����� �����ϴ� �Լ�, ���� ������ �����ϴ� �Լ� ���� ���� �Ѵ�.
 */
public class InstTable {
	/**
	 * inst.data ������ �ҷ��� �����ϴ� ����. 
	 * ��ɾ��� �̸��� ��������� �ش��ϴ� Instruction�� �������� ������ �� �ִ�.
	 */
	HashMap<String, Instruction> instMap;

	/**
	 * Ŭ���� �ʱ�ȭ. �Ľ��� ���ÿ� ó���Ѵ�.
	 * @param instFile : instuction�� ���� ���� ����� ���� �̸�
	 */
	public InstTable(String instFile) {
		instMap = new HashMap<String, Instruction>(); // ���ο� HashMap ����
		openFile(instFile); // instFile�� ��� ���� parsing instMap�� ����
	}

	/**
	 * �Է¹��� �̸��� ������ ���� �ش� ������ �Ľ��Ͽ� instMap�� �����Ѵ�.
	 */
	public void openFile(String fileName) {
		try {
			File file = new File(fileName); // ���� ��ü ����
			FileReader filereader = new FileReader(file); // �Է� ��Ʈ�� ����
			BufferedReader bufReader = new BufferedReader(filereader); // �Է� ���� ����
			String line = ""; // ���� ���۸� ���� ���� ���� �� �ʱ�ȭ
			Instruction inst; // Instruction ��ü ����
			while ((line = bufReader.readLine()) != null) { // ���۰� �ִٸ� (.readLine()�� ���� ���๮�ڸ� ���� ����)
				inst = new Instruction(line); // �Ķ���Ͱ� line�� �� Instruction ��ü ���� 
				instMap.put(inst.name, inst); // instMap�� key�� ��ɾ��� �̸����� �Ͽ� value�� ������ ��ü �߰�
			}
			bufReader.close(); // �Է� ���� �ݱ�
		} catch (FileNotFoundException e) { // ���� ������ ã�� �� ���ٸ�
			System.out.println("Instruction ������ �����ϴ�."); // ������ ���ٰ� ���
			System.exit(0); // ����
		} catch (IOException e) { // ���� �߻���
			System.out.println(e); // e ���
		}
	}
	
	/**
	 * ���ڷ� ���޵� operator�� opcode�� �˷��ش�.
	 * @param operator : �˻��� ���ϴ� ��ɾ��� operator
	 * @return ��ɾ��� opcode. �ش� ��ɾ ���� ��� -1 ����
	 */
	public int search(String operator) {
		if (operator.startsWith("+")) { // operator�� "+"�� �����Ѵٸ� (������ 4�����̶��)
			for (String key : instMap.keySet()) { // instMap�� ó������ ������ �ݺ�
				if (operator.substring(1).equals(key)) { // operator���� "+"�� �� ���ڿ��� key�� ���ٸ�
					return instMap.get(key).opcode; // �ش� key�� opcode�� ����
				}
			}
			return -1; // ��Ͽ� ���ٸ� -1 ����
		} else { // 4������ �ƴ϶��
			for (String key : instMap.keySet()) { // instMap�� ó������ ������ �ݺ�
				if (operator.equals(key)) { // operator�� key�� ���ٸ�
					return instMap.get(key).opcode; // �ش� key�� opcode�� ����
				}
			}
			return -1; // ��Ͽ� ���ٸ� -1 ����
		}
	}
	/**
	 * ���ڷ� ���޵� operator�� format�� �˷��ش�.
	 * @param operator : format�� �˰���� ��ɾ��� operator
	 * @return ��ɾ��� format. �ش� ��ɾ ���� ��� -1 ����
	 */
	public int getformat(String operator) {
		for (String key : instMap.keySet()) { // instMap�� ó������ ������ �ݺ�
			if (operator.equals(key)) { // operator�� key�� ���ٸ�
				return instMap.get(key).format; // �ش� key�� format�� ����
			}
		}
		return -1; // ��Ͽ� ���ٸ� -1 ����
	}
}

/**
 * ��ɾ� �ϳ��ϳ��� ��ü���� ������ InstructionŬ������ ����. 
 * instruction�� ���õ� �������� �����ϰ� �������� ������ �����Ѵ�.
 */
class Instruction {

	String name; // ��ɾ� �̸�
	int opcode; // ��ɾ��� opcode
	int operandnum; // ��ɾ ������ operand�� ����

	/** instruction�� �� ����Ʈ ��ɾ����� ����. ���� ���Ǽ��� ���� */
	int format; // ��ɾ��� ����

	/**
	 * Ŭ������ �����ϸ鼭 �Ϲݹ��ڿ��� ��� ������ �°� �Ľ��Ѵ�.
	 * 
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public Instruction(String line) {
		parsing(line);
	}

	/**
	 * �Ϲ� ���ڿ��� �Ľ��Ͽ� instruction ������ �ľ��ϰ� �����Ѵ�.
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public void parsing(String line) {
		String[] array = line.split("\t"); // "\t"�� �������� ���ڷ� ���� line�� �߶� array �迭�� �ֱ�

		/** inst.txt�� ������ �̸� ���� opcode operand���� �� */
		name = array[0];
		format = Integer.parseInt(array[1]);
		opcode = Integer.parseInt(array[2], 16); // 16������ ����
		operandnum = Integer.parseInt(array[3]);
	}
}
