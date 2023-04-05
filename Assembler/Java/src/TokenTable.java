import java.util.ArrayList;

/**
 * ����ڰ� �ۼ��� ���α׷� �ڵ带 �ܾ�� ���� �� ��, �ǹ̸� �м��ϰ�, ���� �ڵ�� ��ȯ�ϴ� ������ �Ѱ��ϴ� Ŭ�����̴�.
 * pass2���� object code�� ��ȯ�ϴ� ������ ȥ�� �ذ��� �� ���� symbolTable�� instTable�� ������ �ʿ��ϹǷ� �̸� ��ũ��Ų��.
 * section ���� �ν��Ͻ��� �ϳ��� �Ҵ�ȴ�.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND = 3;

	/** bit ������ �������� ���� ���� */
	public static final int nFlag = 32;
	public static final int iFlag = 16;
	public static final int xFlag = 8;
	public static final int bFlag = 4;
	public static final int pFlag = 2;
	public static final int eFlag = 1;

	/** Token�� �ٷ� �� �ʿ��� ���̺���� ��ũ��Ų��. */
	SymbolTable symTab;
	LiteralTable literalTab;
	InstTable instTab;

	/** �� line�� �ǹ̺��� �����ϰ� �м��ϴ� ����. */
	ArrayList<Token> tokenList;

	/**
	 * �ʱ�ȭ�ϸ鼭 symTable�� literalTable�� instTable�� ��ũ��Ų��.
	 * @param symTab     : �ش� section�� ����Ǿ��ִ� symbol table
	 * @param literalTab : �ش� section�� ����Ǿ��ִ� literal table
	 * @param instTab    : instruction ���� ���ǵ� instTable
	 */
	public TokenTable(SymbolTable symTab, LiteralTable literalTab, InstTable instTab) {
		tokenList = new ArrayList<Token>(); // ���ο� tokenList ����
		this.symTab = symTab; // ���� class�� symTab�� ���� symTab �־��ֱ�
		this.literalTab = literalTab; // ���� class��literalTab�� ���� literalTab �־��ֱ�
		this.instTab = instTab; // ���� class�� instTab�� ���� instTab �־��ֱ�
	}

	/**
	 * �Ϲ� ���ڿ��� �޾Ƽ� Token������ �и����� tokenList�� �߰��Ѵ�.
	 * @param line : �и����� ���� �Ϲ� ���ڿ�
	 */
	public void putToken(String line) {
		tokenList.add(new Token(line)); // tokenList�� ���ο� token(���ڷ� ���� line�� parsing)�� ���� �߰�
	}

	/**
	 * tokenList���� index�� �ش��ϴ� Token�� �����Ѵ�.
	 * @param index
	 * @return : index��ȣ�� �ش��ϴ� �ڵ带 �м��� Token Ŭ����
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}

	/**
	 * Pass2 �������� ����Ѵ�. 
	 * instruction table, symbol table literal table ���� �����Ͽ� objectcode�� �����ϰ�, �̸� �����Ѵ�.
	 * @param index
	 */
	public void makeObjectCode(int index) {
		int ref = -1;  // EXTREF�� �ִ� Token�� �ε����� ���� ������ -1�� �ʱ�ȭ
		for (int i = 0; i < tokenList.size(); i++) {// tokenList�� ũ�⸸ŭ �ݺ� (line ������ŭ)
			if (getToken(i).operator != null) { // �ش� operator�� null�� �ƴ϶�� (�ִٸ�)
				if (getToken(i).operator.equals("EXTREF")) { // �ش� operator�� EXTREF���
					ref = i; // i�� ref�� �־��ֱ�
				}
			}
		}
		if (instTab.search(getToken(index).operator) > -1) { // instTable�� �ִ� ��ɾ��� (������ -1 return)
			if (getToken(index).operator.startsWith("+")) { // �ش� operator�� "+"�� �����Ѵٸ� (������ 4�����̶��)(�ּҰ� 5�ڸ�)
				getToken(index).objectCode = String.format("%02X", instTab.search(getToken(index).operator) + (getToken(index).nixbpe >> 4));
				// operator�� opcode�� ã�� nixbpe�� ���� 2��Ʈ�� ���ؼ� objectCode�� �ֱ�
				getToken(index).objectCode += String.format("%01X", 15 & getToken(index).nixbpe); // nixbpe�� ���� 4��Ʈ�� ����ؼ� objectcode �ڿ� �־��ֱ�
				if (!getToken(index).operand[0].equals("")) { // operand�� ���ڰ� ���� �ʴٸ� (�ּ� ����)
					if (getToken(index).operand[0].startsWith("#")) { // �ش� operand�� "#"�� �����Ѵٸ� (��� �ּ� ���� ���)
						getToken(index).objectCode += String.format("%05X", Integer.parseInt(getToken(index).operand[0].substring(1)));
						// "#"�� ������ operand�� �������·� ��ȯ�Ͽ� objectcode �ڿ� �־��ֱ�
					} else if (getToken(index).operand[0].startsWith("@")) { // �ش� operand�� "@"�� �����Ѵٸ� (���� �ּ� ���� ���)
						getToken(index).objectCode += String.format("%05X", symTab.search(getToken(index).operand[0].substring(1)) - getToken(index + 1).location);
						// "@"�� ������ operand�� symTab���� �ּҸ� ã�� ��ɾ��� ���� �ּҸ� ���� objectcode �ڿ� �־��ֱ�
					} else if (getToken(index).operand[0].startsWith("=")) { // �ش� operand�� "="�� �����Ѵٸ�
						for (int j = 0; j < literalTab.literalList.size(); j++) { // literalList�� ũ�⸸ŭ �ݺ�
							if (getToken(index).operand[0].substring(3, getToken(index).operand[0].length() - 1).equals(literalTab.literalList.get(j))) {
								// �ش� operand���� "=", "C" or "X", ����ǥ�� �����ϰ� literalList�� ���Ͽ� ������
								getToken(index).objectCode += String.format("%05X", literalTab.locationList.get(j) - getToken(index + 1).location);
								// �ش� literal�� �ּҸ�  objectcode �ڿ� �־��ֱ�
							}
						}
					} else { // operand �տ� ���� �� ���ٸ�
						if (symTab.search(getToken(index).operand[0]) > -1) { // �ش� operand�� symTab�� �ִٸ� (������ -1 ����)
							if (symTab.search(getToken(index).operand[0]) >= getToken(index + 1).location) { // operand�� ����Ű�� symbol�� �ּ� (TA)�� ���� ��ɾ��� �ּ�(PC)���� ũ�ų� ���ٸ�
								getToken(index).objectCode += String.format("%05X", symTab.search(getToken(index).operand[0]) - getToken(index + 1).location);
								// TA-PC��  objectcode �ڿ� �־��ֱ�
							} else { // operand�� ����Ű�� symbol�� �ּ� (TA)�� ���� ��ɾ��� �ּ�(PC)���� �۴ٸ�
								getToken(index).objectCode += String.format("%05X", 0XFFFFF & (symTab.search(getToken(index).operand[0]) + (~getToken(index + 1).location + 1)));
								// TA�� (-PC)�� ���ؼ� ���� 5�� ���ڸ�  objectcode �ڿ� �־��ֱ�
							}
						} else { // �ش� operand�� symTab�� ���ٸ�
							for (int j = 0; j < getToken(ref).operand.length; j++) { // EXTREF�� operand ������ŭ �ݺ�
								if (getToken(index).operand[0].equals(getToken(ref).operand[j])) { // �ش� operand�� EXTREF�� operand�� �ִ� ���ڿ��̶��
									getToken(index).objectCode += String.format("%05X", 0); // 0��   objectcode �ڿ� �־��ֱ�
								}
							}
						}
					}
				} else { // operand�� ���ڰ� ���ٸ�
					getToken(index).objectCode += String.format("%05X", 0); // 0��   objectcode �ڿ� �־��ֱ�
				}
			} else if (instTab.getformat(getToken(index).operator) == 2) { // �ش� operator�� ������ 2�����̶��
				getToken(index).objectCode = String.format("%02X", instTab.search(getToken(index).operator));
				// operator�� opcode�� ã�Ƽ� objectCode�� �ֱ�
				for (int j = 0; j < 2; j++) { // 2�� �ݺ�(2�����̶� �ּҰ� 2�ڸ�)
					if (getToken(index).operand.length > j) { // operand ������ j���� ���ٸ�
						switch (getToken(index).operand[j]) { // j��° operand��
						case "A": // A���
							getToken(index).objectCode += String.format("%01X", 0); // objectcode �ڿ� 0 �־��ֱ�
							break; // swith ����
						case "X": // X���
							getToken(index).objectCode += String.format("%01X", 1); // objectcode �ڿ� 1 �־��ֱ�
							break; // swith ����
						case "S": // S���
							getToken(index).objectCode += String.format("%01X", 4); // objectcode �ڿ� 4 �־��ֱ�
							break; // swith ����
						case "T": // T���
							getToken(index).objectCode += String.format("%01X", 5); // objectcode �ڿ� 5 �־��ֱ�
							break; // swith ����
						}
					} else // operand ������ j���� ���� �ʴٸ�
						getToken(index).objectCode += String.format("%01X", 0); // objectcode �ڿ� 0 �־��ֱ�
				}
			} else { // 2���ĵ� 4���ĵ� �ƴ϶�� (3�����̶��)(�ּҰ� 3�ڸ�)
				getToken(index).objectCode = String.format("%02X", instTab.search(getToken(index).operator) + (getToken(index).nixbpe >> 4));
				// operator�� opcode�� ã�� nixbpe�� ���� 2��Ʈ�� ���ؼ� objectCode�� �ֱ�
				getToken(index).objectCode += String.format("%01X", 15 & getToken(index).nixbpe); // nixbpe�� ���� 4��Ʈ�� ����ؼ� objectcode �ڿ� �־��ֱ�
				if (!getToken(index).operand[0].equals("")) { // operand�� ���ڰ� ���� �ʴٸ� (�ּ� ����)
					if (getToken(index).operand[0].startsWith("#")) { // �ش� operand�� "#"�� �����Ѵٸ� (��� �ּ� ���� ���)
						getToken(index).objectCode += String.format("%03X", Integer.parseInt(getToken(index).operand[0].substring(1)));
						// "#"�� ������ operand�� �������·� ��ȯ�Ͽ� objectcode �ڿ� �־��ֱ�
					} else if (getToken(index).operand[0].startsWith("@")) { // �ش� operand�� "@"�� �����Ѵٸ� (���� �ּ� ���� ���)
						getToken(index).objectCode += String.format("%03X", symTab.search(getToken(index).operand[0].substring(1)) - getToken(index + 1).location);
						// "@"�� ������ operand�� symTab���� �ּҸ� ã�� ��ɾ��� ���� �ּҸ� ���� objectcode �ڿ� �־��ֱ�
					} else if (getToken(index).operand[0].startsWith("=")) { // �ش� operand�� "="�� �����Ѵٸ�
						for (int j = 0; j < literalTab.literalList.size(); j++) { // literalList�� ũ�⸸ŭ �ݺ�
							if (getToken(index).operand[0].substring(3, getToken(index).operand[0].length() - 1).equals(literalTab.literalList.get(j))) {
								// �ش� operand���� "=", "C" or "X", ����ǥ�� �����ϰ� literalList�� ���Ͽ� ������
								getToken(index).objectCode += String.format("%03X", literalTab.locationList.get(j) - getToken(index + 1).location);
								// �ش� literal�� �ּҸ�  objectcode �ڿ� �־��ֱ�
							}
						}
					} else { // operand �տ� ���� �� ���ٸ�
						if (symTab.search(getToken(index).operand[0]) > -1) { // �ش� operand�� symTab�� �ִٸ� (������ -1 ����)
							if (symTab.search(getToken(index).operand[0]) >= getToken(index + 1).location) { // operand�� ����Ű�� symbol�� �ּ� (TA)�� ���� ��ɾ��� �ּ�(PC)���� ũ�ų� ���ٸ�
								getToken(index).objectCode += String.format("%03X", symTab.search(getToken(index).operand[0]) - getToken(index + 1).location);
								// TA-PC��  objectcode �ڿ� �־��ֱ�
							} else { // operand�� ����Ű�� symbol�� �ּ� (TA)�� ���� ��ɾ��� �ּ�(PC)���� �۴ٸ�
								getToken(index).objectCode += String.format("%03X", 0XFFF & (symTab.search(getToken(index).operand[0]) + (~getToken(index + 1).location + 1)));
								// TA�� (-PC)�� ���ؼ� ���� 5�� ���ڸ�  objectcode �ڿ� �־��ֱ�
							}
						} else { // �ش� operand�� symTab�� ���ٸ�
							for (int j = 0; j < getToken(ref).operand.length; j++) { // EXTREF�� operand ������ŭ �ݺ�
								if (getToken(index).operand[0].equals(getToken(ref).operand[j])) { // �ش� operand�� EXTREF�� operand�� �ִ� ���ڿ��̶��
									getToken(index).objectCode += String.format("%03X", 0); // 0��   objectcode �ڿ� �־��ֱ�
								}
							}
						}
					}
				} else { // operand�� ���ڰ� ���ٸ�
					getToken(index).objectCode += String.format("%03X", 0); // 0��   objectcode �ڿ� �־��ֱ�
				}
			}
		} else if (getToken(index).operator.equals("WORD")) { // �ش� operator�� WORD��� (WORD�� 3bytes ��ɾ��̹Ƿ� �ּҰ� 6�ڸ�)
			
			/* COPY ���α׷����� "-"�� �־ ������ ���� */
			String[] array = getToken(index).operand[0].split("-"); // "-"�� �������� operand�� �߶� array �迭�� �ֱ�
			int ob = -1; // operand�� �ּҸ� ����ؼ� ���� ���� -1�� �ʱ�ȭ
			for (int i = 0; i < array.length; i++) { // array �迭�� ũ�⸸ŭ �ݺ�
				if (symTab.search(array[i]) > -1) { // array�� i��° �迭�� �ִ� ���ڿ��� symTab�� �ִٸ�
					if (ob < 0) { // ob�� �ʱⰪ�̶�� (i�� 0�̶��)
						ob = symTab.search(array[i]); // ob�� 0��° �迭�� �ִ� symbol�� �ּ� �ֱ�
					} else { // ob�� �ʱⰪ�� �ƴ϶�� (i�� 0�� �ƴ϶��)
						ob -= symTab.search(array[i]); // ob���� i��° �迭�� �ִ� symbol�� �ּҸ� ���� ob�� �ֱ�
					}
					getToken(index).objectCode = String.format("%06X", ob); // objectcode�� ob�� �ʱ�ȭ 
				} else { // array�� i��° �迭�� �ִ� ���ڿ��� symTab�� ���ٸ�
					getToken(index).objectCode = String.format("%06X", 0); // objectcode�� 0���� �ʱ�ȭ 
					break; // �ݺ��� ����
				}
			}
		} else if (getToken(index).operator.equals("BYTE")) { // �ش� operator�� BYTE���
			if (getToken(index).operand[0].startsWith("C")) { // �ش� operand�� C�� �����Ѵٸ� (���ڶ��)
				char[] array = literalTab.literalList.get(index).substring(2, getToken(index).operand[0].length() - 1).toCharArray();
				//"C"�� ����ǥ�� ������ operand�� �� ���ھ� �߶� array�� �־��ֱ�
				for (int j = 0; j < array.length; j++) { // array �迭�� ũ�⸸ŭ �ݺ�
					getToken(index).objectCode += String.format("%X", (byte) array[j]); // �ѹ��ھ� �ڸ� operand�� �ƽ�Ű �ڵ�� ��ȯ�Ͽ� objectcode �ڿ� �־��ֱ�
				}
			}
			if (getToken(index).operand[0].startsWith("X")) { // �ش� operand�� X�� �����Ѵٸ� (16���� �������)
				getToken(index).objectCode = String.format("%s", getToken(index).operand[0].substring(2, getToken(index).operand[0].length() - 1));
				//"X"�� ����ǥ�� ������ operand�� objectcode�� �־��ֱ�
			}
		} else if (getToken(index).operator.equals("LTORG")) { // �ش� operator�� LTORG��� 
			for (int i = 0; i < literalTab.literalList.size(); i++) { // literalList�� ũ�⸸ŭ �ݺ�
				if (getToken(index).location == literalTab.locationList.get(i)) { // ���� �ּҿ� i��° literal�� �ּҰ� ������
					if (getToken(index).byteSize == literalTab.literalList.get(i).length()) { // ���� ��ɾ��� ũ��� �ش� literal�� ���̰� ������ (���ڶ��)
						char[] array = literalTab.literalList.get(i).toCharArray(); // �ش� literal�� �� ���ھ� �߶� array�� �־��ֱ�
						for (int j = 0; j < array.length; j++) { // array �迭�� ũ�⸸ŭ �ݺ�
							getToken(index).objectCode += String.format("%X", (byte) array[j]); // �ѹ��ھ� �ڸ�  literal�� �ƽ�Ű �ڵ�� ��ȯ�Ͽ� objectcode �ڿ� �־��ֱ�
						}
					} else { // ���� ��ɾ��� ũ��� i��° literal�� ���̰� ���� ������ (16���� �������)
						getToken(index).objectCode = String.format("%s", literalTab.literalList.get(i)); // �ش� literal�� objectcode�� �־��ֱ�
					}
				}
			}
		} else if (getToken(index).operator.equals("END")) { // �ش� operator�� END��� 
			for (int i = 0; i < literalTab.literalList.size(); i++) { // literalList�� ũ�⸸ŭ �ݺ�
				if (getToken(index).location == literalTab.locationList.get(i)) { // ���� �ּҿ� i��° literal�� �ּҰ� ������
					if (getToken(index).byteSize == literalTab.literalList.get(i).length()) { // ���� ��ɾ��� ũ��� �ش� literal�� ���̰� ������ (���ڶ��)
						char[] array = literalTab.literalList.get(i).toCharArray(); // �ش� literal�� �� ���ھ� �߶� array�� �־��ֱ�
						for (int j = 0; j < array.length; j++) { // array �迭�� ũ�⸸ŭ �ݺ�
							getToken(index).objectCode += String.format("%02X", (byte) array[j]); // �ѹ��ھ� �ڸ�  literal�� �ƽ�Ű �ڵ�� ��ȯ�Ͽ� objectcode �ڿ� �־��ֱ�
						}
					} else { // ���� ��ɾ��� ũ��� i��° literal�� ���̰� ���� ������ (16���� �������)
						getToken(index).objectCode = String.format("%s", literalTab.literalList.get(i)); // �ش� literal�� objectcode�� �־��ֱ�
					}
				}
			}
		} else // ���� ������ �ƴ϶��
			return; // ����
	}

	/**
	 * index��ȣ�� �ش��ϴ� object code�� �����Ѵ�.
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}

}

/**
 * �� ���κ��� ����� �ڵ带 �ܾ� ������ ������ �� �ǹ̸� �ؼ��ϴ� ���� ���Ǵ� ������ ������ �����Ѵ�. 
 * �ǹ� �ؼ��� ������ pass2���� object code�� �����Ǿ��� ���� ����Ʈ �ڵ� ���� �����Ѵ�.
 */
class Token {
	/** �ǹ� �м� �ܰ迡�� ���Ǵ� ������ */
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;

	/** object code ���� �ܰ迡�� ���Ǵ� ������ */
	String objectCode;
	int byteSize;

	/**
	 * Ŭ������ �ʱ�ȭ �ϸ鼭 �ٷ� line�� �ǹ� �м��� �����Ѵ�.
	 * @param line ��������� ����� ���α׷� �ڵ�
	 */
	public Token(String line) {
		location = 0; 
		nixbpe = 0;
		objectCode = "";
		byteSize = 0;
		parsing(line);
	}

	/**
	 * line�� �������� �м��� �����ϴ� �Լ�. Token�� �� ������ �м��� ����� �����Ѵ�.
	 * @param line ��������� ����� ���α׷� �ڵ�.
	 */
	public void parsing(String line) {
		String[] array = line.split("\t"); // "\t"�� �������� line�� �߶� array �迭�� �ֱ�
		String oper = ""; // operand �迭�� �ֱ� ���� parsing�� operand�� ���� ���� ���� �� �ʱ�ȭ
		if (!array[0].equals("")) { // 0��° array �迭�� ������� �ʴٸ�
			label = array[0]; // �ش� �迭�� label�� �־��ֱ�
		}
		if (array.length > 1) { // array�� ũ�Ⱑ 1���� ũ�ٸ�
			if (array[1] != null) { // 1��° array �迭�� null�� �ƴ϶��
				operator = array[1]; // �ش� �迭�� operator�� �־��ֱ�
			}
			if (array.length > 2) { // array�� ũ�Ⱑ 2���� ũ�ٸ�
				if (array[2] != null) { // 2��° array �迭�� null�� �ƴ϶��
					oper = array[2]; // �ش� �迭�� oper�� �־��ֱ�
					operand = oper.split(","); // ","�� �������� oper�� �߶� operand �迭�� �ֱ�
				}
				if (array.length > 3) { // array�� ũ�Ⱑ 3���� ũ�ٸ�
					if (array[3] != null) { // 3��° array �迭�� null�� �ƴ϶��
						comment = array[3]; // �ش� �迭�� comment�� �־��ֱ�
					}
				}
			}
		}
	}

	/**
	 * n,i,x,b,p,e flag�� �����Ѵ�.
	 * 
	 * ��� �� : setFlag(nFlag, 1); 
	 * 	�Ǵ� 	  setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag  : ���ϴ� ��Ʈ ��ġ
	 * @param value : ����ְ��� �ϴ� ��. 1�Ǵ� 0���� �����Ѵ�.
	 */
	public void setFlag(int flag, int value) {
		nixbpe += (char) (flag * value); // �������� flag�� value�� char������ ��ȯ�Ͽ�(2���� ����� �ϱ� ����) ���� nixbpe�� ���ؼ� nixbpe�� �־��ֱ�
	}

	/**
	 * ���ϴ� flag���� ���� ���� �� �ִ�. flag�� ������ ���� ���ÿ� �������� �÷��׸� ��� �� ���� �����ϴ�
	 * 
	 * ��� �� : getFlag(nFlag) 
	 * 	�Ǵ� 	  getFlag(nFlag|iFlag)
	 * 
	 * @param flags : ���� Ȯ���ϰ��� �ϴ� ��Ʈ ��ġ
	 * @return : ��Ʈ��ġ�� �� �ִ� ��. �÷��׺��� ���� 32, 16, 8, 4, 2, 1�� ���� ������ ����.
	 */
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
}
