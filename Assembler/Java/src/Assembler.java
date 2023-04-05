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
 * Assembler : �� ���α׷��� SIC/XE �ӽ��� ���� Assembler ���α׷��� ���� ��ƾ�̴�. 
 * ���α׷��� ���� �۾��� ������ ����. 
 * 1) ó�� �����ϸ� Instruction ���� �о�鿩�� assembler�� �����Ѵ�. 
 * 2) ����ڰ� �ۼ��� input ������ �о���� �� �����Ѵ�. 
 * 3) input ������ ������� �ܾ�� �����ϰ� �ǹ̸� �ľ��ؼ� �����Ѵ�. (pass1) 
 * 4) �м��� ������ �������� ��ǻ�Ͱ� ����� �� �ִ� object code�� �����Ѵ�. (pass2)
 * 
 * 
 * �ۼ����� ���ǻ��� : 
 * 1) ���ο� Ŭ����, ���ο� ����, ���ο� �Լ� ������ �󸶵��� ����. ��, ������ ������ �Լ����� �����ϰų� ������ ��ü�ϴ� ���� �ȵȴ�. 
 * 2) ���������� �ۼ��� �ڵ带 �������� ������ �ʿ信 ���� ����ó��, �������̽� �Ǵ� ��� ��� ���� ����. 
 * 3) ��� void Ÿ���� ���ϰ��� ������ �ʿ信 ���� �ٸ� ���� Ÿ������ ���� ����. 
 * 4) ����, �Ǵ� �ܼ�â�� �ѱ��� ��½�Ű�� �� ��. (ä������ ����. �ּ��� ���Ե� �ѱ��� ��� ����)
 * 
 * 
 * + �����ϴ� ���α׷� ������ ��������� �����ϰ� ���� �е��� ������ ��� �޺κп� ÷�� �ٶ��ϴ�. ���뿡 ���� �������� ���� ��
 * �ֽ��ϴ�.
 */
public class Assembler {
	/** instruction ���� ������ ���� */
	InstTable instTable;
	/** �о���� input ������ ������ �� �� �� �����ϴ� ����. */
	ArrayList<String> lineList;
	/** ���α׷��� section���� symbol table�� �����ϴ� ���� */
	ArrayList<SymbolTable> symtabList;
	/** ���α׷��� section���� literal table�� �����ϴ� ���� */
	ArrayList<LiteralTable> literaltabList;
	/** ���α׷��� section���� ���α׷��� �����ϴ� ���� */
	ArrayList<TokenTable> TokenList;
	/**
	 * Token, �Ǵ� ���þ ���� ������� ������Ʈ �ڵ���� ��� ���·� �����ϴ� ����. 
	 * �ʿ��� ��� String ��� ������ Ŭ������ �����Ͽ� ArrayList�� ��ü�ص� ������.
	 */
	ArrayList<String> codeList;

	/**
	 * Ŭ���� �ʱ�ȭ. instruction Table�� �ʱ�ȭ�� ���ÿ� �����Ѵ�.
	 * 
	 * @param instFile : instruction ���� �ۼ��� ���� �̸�.
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
	 * ������� ���� ��ƾ
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
	 * inputFile�� �о�鿩�� lineList�� �����Ѵ�.
	 * @param inputFile : input ���� �̸�.
	 */
	private void loadInputFile(String inputFile) {
		try {
			File file = new File(inputFile); // ���� ��ü ����
			FileReader filereader = new FileReader(file); // �Է� ��Ʈ�� ����
			BufferedReader bufReader = new BufferedReader(filereader); // �Է� ���� ����
			String line = ""; // ���� ���۸� ���� ���� ���� �� �ʱ�ȭ
			while ((line = bufReader.readLine()) != null) { // ���۰� �ִٸ� (.readLine()�� ���� ���๮�ڸ� ���� ����)
				lineList.add(line); // lineList�� line �߰�
			}
			bufReader.close(); // �Է� ���� �ݱ�
		} catch (FileNotFoundException e) { // ���� ������ ã�� �� ���ٸ�
			System.out.println("Input ������ �����ϴ�."); // ������ ���ٰ� ���
			System.exit(0); // ����
		} catch (IOException e) { // ���� �߻���
			System.out.println(e); // e ���
		}

	}

	/**
	 * pass1 ������ �����Ѵ�. 
	 * 1) ���α׷� �ҽ��� ��ĵ�Ͽ� ��ū������ �и��� �� ��ū���̺� ���� 
	 * 2) label�� symbolTable�� ����
	 * 
	 * ���ǻ��� : SymbolTable�� TokenTable�� ���α׷��� section���� �ϳ��� ����Ǿ�� �Ѵ�.
	 */
	private void pass1() {
		int locctr = 0; // ���� �ּҸ� ��Ÿ���� ����
		/** TokenTable�� section���� �ϳ��� ���� TokenList�� �����ϱ� ���� �ݺ��� */
		int section = 0; // ���� section�� ��Ÿ���� ����
		TokenList.add(new TokenTable(new SymbolTable(), new LiteralTable(), instTable)); // ���ο� TokenTable, ���ο� SymbolTable, ���ο� LiteralTable�� �����ؼ� instTable�� �Բ� TokenList�� �߰�
		symtabList.add(TokenList.get(section).symTab); // SymtabList�� ���� section���� ���� ���� SymbolTable�� �߰� 
		literaltabList.add(TokenList.get(section).literalTab); // literaltabList�� ���� section���� ���� ����literalTable�� �߰� 
		for (int i = 0; i < lineList.size(); i++) { // input ���Ͽ��� �о���� line�� ����ŭ �ݺ� (input ���� ó������ ������)
			TokenList.get(section).putToken(lineList.get(i)); // ���� section�� TokenTable�� �о���� line�� �Ľ��Ͽ� Token�� �߰�
			if (lineList.size() > i + 1) { // ���� �ε����� lineList���� �۴ٸ� (������ line�� �ƴ϶��)
				if (lineList.get(i + 1).contains("CSECT")) { // ���� �ٿ� CSECT ��ɾ �ִٸ� (section�� �ٲ��ֱ� ����) 
					section++; // section�� 1��ŭ ���� (���� section���� �̵�)
					TokenList.add(new TokenTable(new SymbolTable(), new LiteralTable(), instTable)); // ���ο� TokenTable, ���ο� SymbolTable, ���ο� LiteralTable�� �����ؼ� instTable�� �Բ� TokenList�� �߰�
					symtabList.add(TokenList.get(section).symTab); // SymtabList�� ���� section���� ���� ���� SymbolTable�� �߰� 
					literaltabList.add(TokenList.get(section).literalTab); // literaltabList�� ���� section���� ���� ����literalTable�� �߰� 
				}
			}
		}
		
		/** �ּҸ� ����ؼ� location�� �־��ְ� literal�� symbol�� table�� �ֱ� ���� �ݺ��� */
		for (int sec = 0; sec < TokenList.size(); sec++) { // TokenList�� ũ�⸸ŭ �ݺ� (section�� ������ŭ)
			for (int index = 0; index < TokenList.get(sec).tokenList.size(); index++) { // �ش� section�� TokenTable�� ũ�⸸ŭ �ݺ� (�ش� section�� line ������ŭ)
				if (TokenList.get(sec).getToken(index).operator != null) { // �ش� operator�� null�� �ƴ϶�� (�ִٸ�)
					if (TokenList.get(sec).getToken(index).operator.equals("START")) { // �ش� operator�� START���
						locctr = Integer.parseInt(TokenList.get(sec).getToken(index).operand[0]); // operand�� �ִ� �����ּҸ� ���� ���·� ��ȯ�Ͽ� locctr�� �־��ֱ�
						TokenList.get(sec).getToken(index).location = locctr; // ���� location�� locctr �־��ֱ�
						if (TokenList.get(sec).tokenList.size() > index + 1) {  // ���� �ε����� tokenList���� �۴ٸ� (�ش� section�� ������ token�� �ƴ϶��)
							TokenList.get(sec).getToken(index + 1).location = locctr; //���� location���� locctr �־��ֱ� (START�� ũ�Ⱑ ���� ��ɾ�)
						}
					} else if (TokenList.get(sec).getToken(index).operator.equals("CSECT")) { // �ش� operator�� CSECT���
						locctr = 0; // locctr�� 0 �־��ֱ� (���ο� section�� �����̹Ƿ� �ּ� �ʱ�ȭ)
						TokenList.get(sec).getToken(index).location = locctr; // ���� location�� locctr �־��ֱ�
						if (TokenList.get(sec).tokenList.size() > index + 1) {  // ���� �ε����� tokenList���� �۴ٸ� (�ش� section�� ������ token�� �ƴ϶��)
							TokenList.get(sec).getToken(index + 1).location = locctr; //���� location���� locctr �־��ֱ� (CSECT�� ũ�Ⱑ ���� ��ɾ�)
						}
					} else if (instTable.search(TokenList.get(sec).getToken(index).operator) > -1) { // instTable�� �ִ� ��ɾ��� (������ -1 return)
						if (instTable.getformat(TokenList.get(sec).getToken(index).operator) == 2) { // �ش� operator�� ������ 2�����̶��
							TokenList.get(sec).getToken(index).byteSize = 2; // ���� ��ɾ��� byteSize�� 2 �־��ֱ� (2���� ��ɾ��� ũ��� 2bytes)
							locctr += TokenList.get(sec).getToken(index).byteSize; // locctr�� ���� locctr�� byteSize��ŭ ���ؼ� �־��ֱ�
							if (TokenList.get(sec).tokenList.size() > index + 1) { // ���� �ε����� tokenList���� �۴ٸ� (�ش� section�� ������ token�� �ƴ϶��)
								TokenList.get(sec).getToken(index + 1).location = locctr; //���� location���� locctr �־��ֱ�
							}
						} else if (TokenList.get(sec).getToken(index).operator.startsWith("+")) { // �ش� operator�� "+"�� �����Ѵٸ� (������ 4�����̶��)
							TokenList.get(sec).getToken(index).byteSize = 4; // ���� ��ɾ��� byteSize�� 4 �־��ֱ� (4���� ��ɾ��� ũ��� 4bytes)
							locctr += TokenList.get(sec).getToken(index).byteSize; // locctr�� ���� locctr�� byteSize��ŭ ���ؼ� �־��ֱ�
							if (TokenList.get(sec).tokenList.size() > index + 1) { // ���� �ε����� tokenList���� �۴ٸ� (�ش� section�� ������ token�� �ƴ϶��)
								TokenList.get(sec).getToken(index + 1).location = locctr; //���� location���� locctr �־��ֱ�
							}
						} else { // 2���ĵ� 4���ĵ� �ƴ϶�� (3�����̶��)
							TokenList.get(sec).getToken(index).byteSize = 3; // ���� ��ɾ��� byteSize�� 3 �־��ֱ� (3���� ��ɾ��� ũ��� 3bytes)
							locctr += TokenList.get(sec).getToken(index).byteSize; // locctr�� ���� locctr�� byteSize��ŭ ���ؼ� �־��ֱ�
							if (TokenList.get(sec).tokenList.size() > index + 1) { // ���� �ε����� tokenList���� �۴ٸ� (�ش� section�� ������ token�� �ƴ϶��)
								TokenList.get(sec).getToken(index + 1).location = locctr; //���� location���� locctr �־��ֱ�
							}
						}
					} else if (TokenList.get(sec).getToken(index).operator.equals("WORD")) { // �ش� operator�� WORD���
						TokenList.get(sec).getToken(index).byteSize = 3; // ���� ��ɾ��� byteSize�� 3 �־��ֱ� (WORD ��ɾ��� ũ��� 3bytes)
						locctr += TokenList.get(sec).getToken(index).byteSize; // locctr�� ���� locctr�� byteSize��ŭ ���ؼ� �־��ֱ�
						if (TokenList.get(sec).tokenList.size() > index + 1) { // ���� �ε����� tokenList���� �۴ٸ� (�ش� section�� ������ token�� �ƴ϶��)
							TokenList.get(sec).getToken(index + 1).location = locctr; //���� location���� locctr �־��ֱ�
						}
					} else if (TokenList.get(sec).getToken(index).operator.equals("RESW")) { // �ش� operator�� RESW���
						TokenList.get(sec).getToken(index).byteSize = 3	* Integer.parseInt(TokenList.get(sec).getToken(index).operand[0]);
						// ���� ��ɾ��� byteSize�� operand�� �ִ� WORD ������ ���� ���·� ��ȯ�Ͽ� 3�� ���Ͽ� �־��ֱ� (1WORD = 3bytes)
						locctr += TokenList.get(sec).getToken(index).byteSize; // locctr�� ���� locctr�� byteSize��ŭ ���ؼ� �־��ֱ�
						if (TokenList.get(sec).tokenList.size() > index + 1) { // ���� �ε����� tokenList���� �۴ٸ� (�ش� section�� ������ token�� �ƴ϶��)
							TokenList.get(sec).getToken(index + 1).location = locctr; //���� location���� locctr �־��ֱ�
						}
					} else if (TokenList.get(sec).getToken(index).operator.equals("RESB")) { // �ش� operator�� RESB���
						TokenList.get(sec).getToken(index).byteSize = Integer.parseInt(TokenList.get(sec).getToken(index).operand[0]);
						// ���� ��ɾ��� byteSize�� operand�� �ִ� Byte ������ ���� ���·� ��ȯ�Ͽ� �־��ֱ�
						locctr += TokenList.get(sec).getToken(index).byteSize; // locctr�� ���� locctr�� byteSize��ŭ ���ؼ� �־��ֱ�
						if (TokenList.get(sec).tokenList.size() > index + 1) { // ���� �ε����� tokenList���� �۴ٸ� (�ش� section�� ������ token�� �ƴ϶��)
							TokenList.get(sec).getToken(index + 1).location = locctr; //���� location���� locctr �־��ֱ�
						}
					} else if (TokenList.get(sec).getToken(index).operator.equals("BYTE")) { // �ش� operator�� BYTE���
						if (TokenList.get(sec).getToken(index).operand[0].startsWith("C")) { // �ش� operand�� C�� �����Ѵٸ� (���ڶ��)
							TokenList.get(sec).getToken(index).byteSize = TokenList.get(sec).getToken(index).operand[0].length() - 3;
							// ���� ��ɾ��� byteSize�� "C"�� ����ǥ�� ������ operand�� ���̸� �־��ֱ� (���� 1���� 1byte)
							locctr += TokenList.get(sec).getToken(index).byteSize; // locctr�� ���� locctr�� byteSize��ŭ ���ؼ� �־��ֱ�
							if (TokenList.get(sec).tokenList.size() > index + 1) { // ���� �ε����� tokenList���� �۴ٸ� (�ش� section�� ������ token�� �ƴ϶��)
								TokenList.get(sec).getToken(index + 1).location = locctr; //���� location���� locctr �־��ֱ�
							}
						} else if (TokenList.get(sec).getToken(index).operand[0].startsWith("X")) { // �ش� operand�� X�� �����Ѵٸ� (16���� �������)
							TokenList.get(sec).getToken(index).byteSize = (TokenList.get(sec).getToken(index).operand[0].length() - 3) / 2;
							// ���� ��ɾ��� byteSize�� "X"�� ����ǥ�� ������ operand�� ���̿��� 2�� ������ �־��ֱ�  (16���� ���� 2�ڸ��� 1byte)
							locctr += TokenList.get(sec).getToken(index).byteSize; // locctr�� ���� locctr�� byteSize��ŭ ���ؼ� �־��ֱ�
							if (TokenList.get(sec).tokenList.size() > index + 1) { // ���� �ε����� tokenList���� �۴ٸ� (�ش� section�� ������ token�� �ƴ϶��)
								TokenList.get(sec).getToken(index + 1).location = locctr; //���� location���� locctr �־��ֱ�
							}
						}
					} else if (TokenList.get(sec).getToken(index).operator.equals("LTORG")) { // �ش� operator�� LTORG���
						for (int i = 0; i < literaltabList.get(sec).literalList.size(); i++) { // �ش� section�� LieteralTable�� ũ�⸸ŭ �ݺ� (�ش� section�� literal ������ŭ)
							String str = TokenList.get(sec).getToken(literaltabList.get(sec).locationList.get(i)).operand[0];
							// �ش� literal�� ����� �ּ��� operand�� str�� �־��ֱ�
							String literal = literaltabList.get(sec).literalList.get(i); // �ش� literal�� literal ������ �־��ֱ�
							if (str.substring(1).startsWith("C")) { // �ش� operand�� C�� �����Ѵٸ� (�տ� "=" ����)
								TokenList.get(sec).getToken(index).byteSize = literaltabList.get(sec).literalList.get(i).length();
								 // ���� ��ɾ��� byteSize�� �ش� literal�� ���̸� �־��ֱ� (���� 1���� 1byte) 
								locctr += TokenList.get(sec).getToken(index).byteSize; // locctr�� ���� locctr�� byteSize��ŭ ���ؼ� �־��ֱ�
								literaltabList.get(sec).modifyLiteral(literal, TokenList.get(sec).getToken(index).location);
								// LieteralTable�� �ִ� �ش� literal�� �ּҰ� ����� ��ġ�� �Ǿ� �ִµ� LTORG�� �ִ� ��ġ�� ����
							} else if (str.substring(1).startsWith("X")) { // �ش� operand�� X�� �����Ѵٸ� (�տ� "=" ����)
								TokenList.get(sec).getToken(index).byteSize = (literaltabList.get(sec).literalList.get(i).length()) / 2;
								// ���� ��ɾ��� byteSize�� �ش� literal�� ���̸� 2�� ������ �־��ֱ�  (16���� ���� 2�ڸ��� 1byte)
								locctr += TokenList.get(sec).getToken(index).byteSize; // locctr�� ���� locctr�� byteSize��ŭ ���ؼ� �־��ֱ�
								literaltabList.get(sec).modifyLiteral(literal, TokenList.get(sec).getToken(index).location);
								// LieteralTable�� �ִ� �ش� literal�� �ּҰ� ����� ��ġ�� �Ǿ� �ִµ� LTORG�� �ִ� ��ġ�� ����
							}
						}
						if (TokenList.get(sec).tokenList.size() > index + 1) { // ���� �ε����� tokenList���� �۴ٸ� (�ش� section�� ������ token�� �ƴ϶��)
							TokenList.get(sec).getToken(index + 1).location = locctr; //���� location���� locctr �־��ֱ�
						}
					} else if (TokenList.get(sec).getToken(index).operator.equals("END")) { // �ش� operator�� END���
						for (int i = 0; i < literaltabList.get(sec).literalList.size(); i++) { // �ش� section�� LieteralTable�� ũ�⸸ŭ �ݺ� (�ش� section�� literal ������ŭ)
							String str = TokenList.get(sec).getToken(literaltabList.get(sec).locationList.get(i)).operand[0];
							// �ش� literal�� ����� �ּ��� operand�� str�� �־��ֱ�
							String literal = literaltabList.get(sec).literalList.get(i); // �ش� literal�� literal ������ �־��ֱ�
							if (str.substring(1).startsWith("C")) { // �ش� operand�� C�� �����Ѵٸ� (�տ� "=" ����)
								TokenList.get(sec).getToken(index).byteSize = literaltabList.get(sec).literalList.get(i).length();
								 // ���� ��ɾ��� byteSize�� �ش� literal�� ���̸� �־��ֱ� (���� 1���� 1byte) 
								locctr += TokenList.get(sec).getToken(index).byteSize; // locctr�� ���� locctr�� byteSize��ŭ ���ؼ� �־��ֱ�
								literaltabList.get(sec).modifyLiteral(literal, TokenList.get(sec).getToken(index).location);
								// LieteralTable�� �ִ� �ش� literal�� �ּҰ� ����� ��ġ�� �Ǿ� �ִµ� LTORG�� �ִ� ��ġ�� ����
							} else if (str.substring(1).startsWith("X")) { // �ش� operand�� X�� �����Ѵٸ� (�տ� "=" ����)
								TokenList.get(sec).getToken(index).byteSize = (literaltabList.get(sec).literalList.get(i).length()) / 2;
								// ���� ��ɾ��� byteSize�� �ش� literal�� ���̸� 2�� ������ �־��ֱ�  (16���� ���� 2�ڸ��� 1byte)
								locctr += TokenList.get(sec).getToken(index).byteSize; // locctr�� ���� locctr�� byteSize��ŭ ���ؼ� �־��ֱ�
								literaltabList.get(sec).modifyLiteral(literal, TokenList.get(sec).getToken(index).location);
								// LieteralTable�� �ִ� �ش� literal�� �ּҰ� ����� ��ġ�� �Ǿ� �ִµ� LTORG�� �ִ� ��ġ�� ����
							}
						}
						break; // �ݺ��� ���� (END�� ������ �б� ����)
					}
					
					/** literalList�� literal �߰�*/
					if (TokenList.get(sec).getToken(index).operand != null) { // �ش� operand�� null�� �ƴ϶�� (�ִٸ�)
						if (TokenList.get(sec).getToken(index).operand[0].startsWith("=")) {  // �ش� operand�� "="���� �����Ѵٸ� (literal �����̶��)
							String literal = TokenList.get(sec).getToken(index).operand[0].substring(3, TokenList.get(sec).getToken(index).operand[0].length() - 1);
							 // �ش� operand���� "=", "C" or "X", ����ǥ�� �����ϰ� literal ������ �־��ֱ�
							if (TokenList.get(sec).literalTab.literalList != null) { // literalList�� null�� �ƴ϶�� (literal�� �ִٸ�)
								if (literaltabList.get(sec).search(literal) != -1) { // literalTable�� �ش� literal�� �ִٸ� (���ٸ� -1 ����)
									literaltabList.get(sec).modifyLiteral(literal, index); // ������ literal �ּҸ� ���ο� �ּҷ� ����
								} else { // literalTable�� literal�� ���ٸ�
									literaltabList.get(sec).putLiteral(literal, index); // literalTable�� literal�� literal�� ����� ��ġ�� �־��ֱ�
								}
							} else { // literalList�� ����ִٸ�
								literaltabList.get(sec).putLiteral(literal, index); // literalTable�� literal�� literal�� ����� ��ġ�� �־��ֱ�
							}
						}
					}
					
					/** symbolList�� symbol �߰�*/
					if (TokenList.get(sec).getToken(index).label != null) { // �ش� label�� null�� �ƴ϶�� (�ִٸ�)
						if (!TokenList.get(sec).getToken(index).label.startsWith(".")) { // label�� "."���� �������� �ʴ´ٸ� (�ǹ̾��� line�� symbol ���� �ʱ� ����)
							String label = TokenList.get(sec).getToken(index).label; // �ش� label�� label ������ �־��ֱ�
							int location = TokenList.get(sec).getToken(index).location; // ���� �ּҸ� location ������ �־��ֱ�
							symtabList.get(sec).putSymbol(label, location); // symbolTable�� labell�� location �־��ֱ�
						}
					}
					
					/** EQU ��ɾ��� �ּ� ó�� */
					if (TokenList.get(sec).getToken(index).operator.equals("EQU")) { // �ش� operator�� EQU���
						for (int i = 0; i < symtabList.get(sec).symbolList.size(); i++) { // �ش� section�� symbolTable�� ũ�⸸ŭ �ݺ� (�ش� section�� symbol ������ŭ)
							String symbol = symtabList.get(sec).symbolList.get(i); // �ش� symbol�� symbol ������ �ֱ�
							if (TokenList.get(sec).getToken(index).operand[0].contains(symbol)) { // EQU�� operand�� symbol�� �ִٸ�
								
								/* COPY ���α׷����� "-"�� �־ ������ ���� */
								String[] array = TokenList.get(sec).getToken(index).operand[0].split("-"); // "-"�� �������� operand�� �߶� array �迭�� �ֱ�
								int newLocation = symtabList.get(sec).search(array[0]); // ���� ���� ���� symbol�� �ּҸ� ã�Ƽ� newLocation ������ �ֱ�
								for (int j = 1; j < array.length; j++) { // �̹� ����� 0��° �迭�� �����ϰ� 1��° �迭���� �迭�� ũ�⸸ŭ �ݺ�
									newLocation -= symtabList.get(sec).search(array[j]); // newLocation���� j��° �迭�� �ִ� symbol�� �ּҸ� ã�Ƽ� ���� newLocation ������ �ֱ�
								}
								symtabList.get(sec).modifySymbol(TokenList.get(sec).getToken(index).label, newLocation); // symbolTable�� �ִ� �ش� symbol�� �ּҸ� ����� �� �ּҷ� ����
								TokenList.get(sec).getToken(index).location = newLocation; // ���� token�� �ּҵ� �� �ּҷ� ����
								break; // �ݺ��� ����
							}
						}
					}
				}
			}
		}
	}

	/**
	 * �ۼ��� SymbolTable���� ������¿� �°� ����Ѵ�.
	 * @param fileName : ����Ǵ� ���� �̸�
	 */
	private void printSymbolTable(String fileName) { // 
		try {
			File file = new File(fileName); // ���� ��ü ����
			PrintWriter pw = new PrintWriter(file); // ��� ��Ʈ�� ����
			for (int sec = 0; sec < TokenList.size(); sec++) { // TokenList�� ũ�⸸ŭ �ݺ� (section�� ������ŭ)
				if (symtabList.get(sec).symbolList != null) { // �ش� section�� symbolTable�� null�� �ƴ϶�� (�ִٸ�)
					for (int i = 0; i < symtabList.get(sec).symbolList.size(); i++) { // �ش� section�� SymbolTable�� ũ�⸸ŭ �ݺ� (�ش� section�� symbol ������ŭ)
						pw.format("%s\t%X\n", symtabList.get(sec).symbolList.get(i), symtabList.get(sec).locationList.get(i));
						//symbol location ������ ���� ��� (location�� 16������ ���)
					}
				}
				pw.println(); // section�� ������ �����ϱ� ���� �ٹٲ�
			}
			pw.close(); // ��� ��Ʈ�� �ݱ�
		} catch (IOException e) { // ���� �߻���
			System.out.println("�ۼ��� ������ �����ϴ�."); // ������ ���ٰ� ���
		}

	}

	/**
	 * �ۼ��� LiteralTable���� ������¿� �°� ����Ѵ�.
	 * @param fileName : ����Ǵ� ���� �̸�
	 */
	private void printLiteralTable(String fileName) {
		try {
			File file = new File(fileName); // ���� ��ü ����
			PrintWriter pw = new PrintWriter(file); // ��� ��Ʈ�� ����
			for (int sec = 0; sec < TokenList.size(); sec++) { // TokenList�� ũ�⸸ŭ �ݺ� (section�� ������ŭ)
				if (literaltabList.get(sec).literalList != null) { // �ش� section�� literalTable�� null�� �ƴ϶�� (�ִٸ�)
					for (int i = 0; i < literaltabList.get(sec).literalList.size(); i++) { // �ش� section�� LiteralTable�� ũ�⸸ŭ �ݺ� (�ش� section�� literal ������ŭ)
						pw.format("%s\t%X\n", literaltabList.get(sec).literalList.get(i), literaltabList.get(sec).locationList.get(i));
						//literal location ������ ���� ��� (location�� 16������ ���)
					}
				}
			}
			pw.close(); // ��� ��Ʈ�� �ݱ�
		} catch (IOException e) { // ���� �߻���
			System.out.println("�ۼ��� ������ �����ϴ�."); // ������ ���ٰ� ���
		}
	}

	/**
	 * pass2 ������ �����Ѵ�. 
	 * 1) �м��� ������ �������� object code�� �����Ͽ� codeList�� ����.
	 */
	private void pass2() {
		/** nixbpe ��Ʈ�� ����ؼ� nixbpe�� �־��ֱ� ���� �ݺ��� */
		for (int sec = 0; sec < TokenList.size(); sec++) { // TokenList�� ũ�⸸ŭ �ݺ� (section�� ������ŭ)
			for (int index = 0; index < TokenList.get(sec).tokenList.size(); index++) { // �ش� section�� TokenTable�� ũ�⸸ŭ �ݺ� (�ش� section�� line ������ŭ)
				if (TokenList.get(sec).getToken(index).operator != null) { // �ش� operator�� null�� �ƴ϶�� (�ִٸ�)
					if (instTable.search(TokenList.get(sec).getToken(index).operator) > -1) { // instTable�� �ִ� ��ɾ��� (������ -1 return)
						if (instTable.getformat(TokenList.get(sec).getToken(index).operator) == 2) { // �ش� operator�� ������ 2�����̶��
							TokenList.get(sec).getToken(index).nixbpe = 0; // nixbpe�� 0 �־��ֱ� (2������ nixbpe ��� �� ��)
						} else if (TokenList.get(sec).getToken(index).operator.startsWith("+")) { // �ش� operator�� "+"�� �����Ѵٸ� (������ 4�����̶��)
							TokenList.get(sec).getToken(index).setFlag(TokenTable.eFlag, 1); // eFlag�� 1 �־��ֱ� (4�����̹Ƿ� Ȯ��)
							TokenList.get(sec).getToken(index).setFlag(TokenTable.bFlag, 0); // bFlag�� 0 �־��ֱ� (Ȯ���ؼ� ��� �ּ� ��� �� ��)
							TokenList.get(sec).getToken(index).setFlag(TokenTable.pFlag, 0); // pFlag�� 0 �־��ֱ� (Ȯ���ؼ� ��� �ּ� ��� �� ��)
							if (TokenList.get(sec).getToken(index).operand[0].startsWith("#")) { // �ش� operand�� "#"�� �����Ѵٸ� (��� �ּ� ���� ���)
								TokenList.get(sec).getToken(index).setFlag(TokenTable.nFlag, 0); // nFlag�� 0�־��ֱ�
								TokenList.get(sec).getToken(index).setFlag(TokenTable.iFlag, 1); // iFlag�� 1 �־��ֱ�
								for (int i = 0; i < TokenList.get(sec).getToken(index).operand.length; i++) { // operand ���� ��ŭ �ݺ�
									if (TokenList.get(sec).getToken(index).operand[i].equals("X")) { // �ش� operand�� X��� (index)
										TokenList.get(sec).getToken(index).setFlag(TokenTable.xFlag, 1); // xFlag�� 1 �־��ֱ�
										break; //X�� �ϳ��� �־ �ݺ��� ����
									} else // �ش� operand�� X�� �ƴ϶��
										TokenList.get(sec).getToken(index).setFlag(TokenTable.xFlag, 0); // xFlag�� 0 �־��ֱ�
								}
							} else if (TokenList.get(sec).getToken(index).operand[0].startsWith("@")) { // �ش� operand�� "@"�� �����Ѵٸ� (���� �ּ� ���� ���)
								TokenList.get(sec).getToken(index).setFlag(TokenTable.nFlag, 1); // nFlag�� 1�־��ֱ�
								TokenList.get(sec).getToken(index).setFlag(TokenTable.iFlag, 0); // iFlag�� 0�־��ֱ�
								for (int i = 0; i < TokenList.get(sec).getToken(index).operand.length; i++) { // operand ���� ��ŭ �ݺ�
									if (TokenList.get(sec).getToken(index).operand[i].equals("X")) { // �ش� operand�� X��� (index)
										TokenList.get(sec).getToken(index).setFlag(TokenTable.xFlag, 1); // xFlag�� 1 �־��ֱ�
										break; //X�� �ϳ��� �־ �ݺ��� ����
									} else // �ش� operand�� X�� �ƴ϶��
										TokenList.get(sec).getToken(index).setFlag(TokenTable.xFlag, 0); // xFlag�� 0 �־��ֱ�
								}
							} else { // �ش� operand�� "#" �Ǵ� "@"�� �������� �ʴ´ٸ�  (simple addressing)
								TokenList.get(sec).getToken(index).setFlag(TokenTable.nFlag, 1); // nFlag�� 1�־��ֱ�
								TokenList.get(sec).getToken(index).setFlag(TokenTable.iFlag, 1); // iFlag�� 1�־��ֱ�
								for (int i = 0; i < TokenList.get(sec).getToken(index).operand.length; i++) { // operand ���� ��ŭ �ݺ�
									if (TokenList.get(sec).getToken(index).operand[i].equals("X")) { // �ش� operand�� X��� (index)
										TokenList.get(sec).getToken(index).setFlag(TokenTable.xFlag, 1); // xFlag�� 1 �־��ֱ�
										break; //X�� �ϳ��� �־ �ݺ��� ����
									} else // �ش� operand�� X�� �ƴ϶��
										TokenList.get(sec).getToken(index).setFlag(TokenTable.xFlag, 0); // xFlag�� 0 �־��ֱ�
								}
							}
						} else { // 2���ĵ� 4���ĵ� �ƴ϶�� (3�����̶��)
							TokenList.get(sec).getToken(index).setFlag(TokenTable.eFlag, 0); // eFlag�� 0 �־��ֱ� (3�����̹Ƿ� Ȯ������ ����)
							if (TokenList.get(sec).getToken(index).operand[0].startsWith("#")) { // �ش� operand�� "#"�� �����Ѵٸ� (��� �ּ� ���� ���)
								TokenList.get(sec).getToken(index).setFlag(TokenTable.nFlag, 0); // nFlag�� 0�־��ֱ�
								TokenList.get(sec).getToken(index).setFlag(TokenTable.iFlag, 1); // iFlag�� 1 �־��ֱ�
								TokenList.get(sec).getToken(index).setFlag(TokenTable.bFlag, 0); // bFlag�� 0 �־��ֱ� (��� �ּ��̹Ƿ� ��� �ּ� ��� �� ��)
								TokenList.get(sec).getToken(index).setFlag(TokenTable.pFlag, 0); // pFlag�� 0 �־��ֱ� (��� �ּ��̹Ƿ� ��� �ּ� ��� �� ��)
								for (int i = 0; i < TokenList.get(sec).getToken(index).operand.length; i++) { // operand ���� ��ŭ �ݺ�
									if (TokenList.get(sec).getToken(index).operand[i].equals("X")) { // �ش� operand�� X��� (index)
										TokenList.get(sec).getToken(index).setFlag(TokenTable.xFlag, 1); // xFlag�� 1 �־��ֱ�
										break; //X�� �ϳ��� �־ �ݺ��� ����
									} else // �ش� operand�� X�� �ƴ϶��
										TokenList.get(sec).getToken(index).setFlag(TokenTable.xFlag, 0); // xFlag�� 0 �־��ֱ�
								}
							} else if (TokenList.get(sec).getToken(index).operand[0].startsWith("@")) { // �ش� operand�� "@"�� �����Ѵٸ� (���� �ּ� ���� ���)
								TokenList.get(sec).getToken(index).setFlag(TokenTable.nFlag, 1); // nFlag�� 1�־��ֱ�
								TokenList.get(sec).getToken(index).setFlag(TokenTable.iFlag, 0); // iFlag�� 0�־��ֱ�
								TokenList.get(sec).getToken(index).setFlag(TokenTable.bFlag, 0); // bFlag�� 0 �־��ֱ� (COPY���α׷��� nobase ���α׷�)
								TokenList.get(sec).getToken(index).setFlag(TokenTable.pFlag, 1); // bFlag�� 0 �־��ֱ� (control section�� ��� PC����ּ�)
								for (int i = 0; i < TokenList.get(sec).getToken(index).operand.length; i++) { // operand ���� ��ŭ �ݺ�
									if (TokenList.get(sec).getToken(index).operand[i].equals("X")) { // �ش� operand�� X��� (index)
										TokenList.get(sec).getToken(index).setFlag(TokenTable.xFlag, 1); // xFlag�� 1 �־��ֱ�
										break; //X�� �ϳ��� �־ �ݺ��� ����
									} else // �ش� operand�� X�� �ƴ϶��
										TokenList.get(sec).getToken(index).setFlag(TokenTable.xFlag, 0); // xFlag�� 0 �־��ֱ�
								}
							} else { // �ش� operand�� "#" �Ǵ� "@"�� �������� �ʴ´ٸ�  (simple addressing)
								TokenList.get(sec).getToken(index).setFlag(TokenTable.nFlag, 1); // nFlag�� 1�־��ֱ�
								TokenList.get(sec).getToken(index).setFlag(TokenTable.iFlag, 1); // iFlag�� 1�־��ֱ�
								if (TokenList.get(sec).getToken(index).operand[0].equals("")) { // operand�� ���ڰ� ���ٸ� (�ּ� ������ �ʿ� ����)
									TokenList.get(sec).getToken(index).setFlag(TokenTable.xFlag, 0); // xFlag�� 0 �־��ֱ�
									TokenList.get(sec).getToken(index).setFlag(TokenTable.bFlag, 0); // bFlag�� 0 �־��ֱ�
									TokenList.get(sec).getToken(index).setFlag(TokenTable.pFlag, 0); // pFlag�� 0 �־��ֱ�
								} else { // operand�� ���ڰ� �ִٸ�
									TokenList.get(sec).getToken(index).setFlag(TokenTable.bFlag, 0); // bFlag�� 0 �־��ֱ� (COPY���α׷��� nobase ���α׷�)
									TokenList.get(sec).getToken(index).setFlag(TokenTable.pFlag, 1); // bFlag�� 0 �־��ֱ� (control section�� ��� PC����ּ�)
									for (int i = 0; i < TokenList.get(sec).getToken(index).operand.length; i++) { // operand ���� ��ŭ �ݺ�
										if (TokenList.get(sec).getToken(index).operand[i].equals("X")) { // �ش� operand�� X��� (index)
											TokenList.get(sec).getToken(index).setFlag(TokenTable.xFlag, 1); // xFlag�� 1 �־��ֱ�
											break; //X�� �ϳ��� �־ �ݺ��� ����
										} else // �ش� operand�� X�� �ƴ϶��
											TokenList.get(sec).getToken(index).setFlag(TokenTable.xFlag, 0); // xFlag�� 0 �־��ֱ�
									}
								}
							}
						}
					}
				}
			}
		}
		
		/** ObjectCode�� ó������ ������ ����� ���� �ݺ��� */
		for (int sec = 0; sec < TokenList.size(); sec++) { // TokenList�� ũ�⸸ŭ �ݺ� (section�� ������ŭ)
			for (int index = 0; index < TokenList.get(sec).tokenList.size(); index++) { // �ش� section�� TokenTable�� ũ�⸸ŭ �ݺ� (�ش� section�� line ������ŭ)
				if (TokenList.get(sec).getToken(index).operator != null) { // �ش� operator�� null�� �ƴ϶�� (�ִٸ�)
					TokenList.get(sec).makeObjectCode(index); // ���� ��ɾ��� ObjectCode ����
				}
			}
		}
		
		/** �� section�� ���α׷� ������ ����ϱ� ���� �ݺ��� */
		ArrayList<Integer> pLength = new ArrayList<Integer>(); // section �� ���α׷� ���̸� �ֱ� ���� ArrayList ����
		for (int sec = 0; sec < TokenList.size(); sec++) { // TokenList�� ũ�⸸ŭ �ݺ� (section�� ������ŭ)
			int pLen = 0; // byteSize�� ���ؼ� ���� ���� �ʱ�ȭ
			for (int index = 0; index < TokenList.get(sec).tokenList.size(); index++) { // �ش� section�� TokenTable�� ũ�⸸ŭ �ݺ� (�ش� section�� line ������ŭ)
				pLen += TokenList.get(sec).getToken(index).byteSize; // pLen�� ���� pLen�� byteSize��ŭ ���ؼ� �־��ֱ�
			}
			pLength.add(pLen); // pLength�� ���� section�� ��� byteSize�� ���� pLen �߰�
		}
		
		/** Object code�� codeList�� �����ϱ� ���� �ݺ��� */
		String code = ""; // // object code�� ���� ���� �ʱ�ȭ
		ArrayList<String> textList = new ArrayList<String>(); // text record�� object code�� �ֱ� ���� ArrayList ����
		for (int sec = 0; sec < TokenList.size(); sec++) { // TokenList�� ũ�⸸ŭ �ݺ� (section�� ������ŭ)
			int sLen = 0; // text record�� ������ ���̸� ���� ���� �ʱ�ȭ
			int sStart = 0; // text record�� ������ �����ּҸ� ���� ���� �ʱ�ȭ
			for (int index = 0; index < TokenList.get(sec).tokenList.size(); index++) { // �ش� section�� TokenTable�� ũ�⸸ŭ �ݺ� (�ش� section�� line ������ŭ)
				if (TokenList.get(sec).getToken(index).operator != null) { // �ش� operator�� null�� �ƴ϶�� (�ִٸ�)
					
					/** Header record */
					if (TokenList.get(sec).getToken(index).operator.equals("START") || TokenList.get(sec).getToken(index).operator.equals("CSECT")) {
						// �ش� operator�� START or CSECT��� (section�� ����)(header record �ۼ�)
						code = String.format("H%-6s%06X%06X\n", TokenList.get(sec).getToken(index).label, TokenList.get(sec).getToken(index).location, pLength.get(sec));
						// H^���α׷���^�����ּ�^���α׷� �� ���� �������� code�� �־��ֱ�
						codeList.add(code); // code�� codeList�� �߰�
						
					/** Define record */
					} else if (TokenList.get(sec).getToken(index).operator.equals("EXTDEF")) { // �ش� operator�� EXTDEF��� (define record �ۼ�)
						code = "D"; // code�� "D" �־��ֱ�
						codeList.add(code); // code�� codeList�� �߰�
						for (int i = 0; i < TokenList.get(sec).getToken(index).operand.length; i++) { // operand ���� ��ŭ �ݺ�
							String def = TokenList.get(sec).getToken(index).operand[i]; // i��° operand�� def ������ �־��ֱ�
							int address = symtabList.get(sec).search(def); // def�� �ּҸ� symtabList���� ã�Ƽ� address ������ �־��ֱ�
							code = String.format("%-6s%06X", def, address); // symbol�̸�^�ּ� �������� code�� �־��ֱ�
							codeList.add(code); // code�� codeList�� �߰�
						}
						code = "\n"; // define record �ۼ� ���� �� �ٹٲٱ� ���� "\n"�� code�� �־��ֱ�
						codeList.add(code); // code�� codeList�� �߰�
						
					/** Refer record */
					} else if (TokenList.get(sec).getToken(index).operator.equals("EXTREF")) { // �ش� operator�� EXTDEF��� (refer record �ۼ�)
						code = "R"; // code�� "R" �־��ֱ�
						codeList.add(code); // code�� codeList�� �߰�
						for (int i = 0; i < TokenList.get(sec).getToken(index).operand.length; i++) { // operand ���� ��ŭ �ݺ�
							String ref = TokenList.get(sec).getToken(index).operand[i]; // i��° operand�� ref ������ �־��ֱ�
							code = String.format("%-6s", ref); // symbol�̸��� code�� �־��ֱ�
							codeList.add(code); // code�� codeList�� �߰�
						}
						code = "\n"; // refer record �ۼ� ���� �� �ٹٲٱ� ���� "\n"�� code�� �־��ֱ�
						codeList.add(code); // code�� codeList�� �߰�
						
					/** Text record */
					} else { // START, CSECT, EXTDEF, EXTREF �� �ƴ϶��
						if (!TokenList.get(sec).getToken(index).objectCode.equals("")) { // object code�� ""�� �ƴ϶�� (������� �ʴٸ�)
							if (sLen == 0) { // sLen�� 0�̶�� (text record ���� ��ɾ�)
								sStart = TokenList.get(sec).getToken(index).location; // sStart�� ���� location�� �־��ֱ�
							}
							sLen += TokenList.get(sec).getToken(index).byteSize; // sLen�� byteSize ���ؼ� �־��ֱ� (���� ��ɾ ������ ���� ������ ��ɾ����� �Ǻ��ϱ� ���� �̸� ���̸� �÷���)
							if (sLen > 0x1E) { // sLen�� 1E���� ũ�ٸ� (���� ������ ��ɾ�, �� ���忡 �� �� �ִ� ������ ���̴� �ִ� 1E)
								sLen -= TokenList.get(sec).getToken(index).byteSize; // sLen���� ���ߴ� byteSize��ŭ ���ֱ� (���� ��ɾ�� ���� ������ ��ɾ�)
								code = String.format("T%06X%02X", sStart, sLen); // T^���� �����ּ�^���� ���� �������� code�� �־��ֱ�
								codeList.add(code); // code�� codeList�� �߰�
								for (int i = 0; i < textList.size(); i++) { // textList ó������ ������ �ݺ�
									code = textList.get(i); // textList�� i��°�� �ִ� ������ code�� �־��ֱ�
									codeList.add(code); // code�� codeList�� �߰�
								}
								code = "\n"; // text record �� ���� ���� �� �ٹٲٱ� ���� "\n"�� code�� �־��ֱ�
								codeList.add(code); // code�� codeList�� �߰�
								textList.clear(); // ���� ������ text�� �־��ֱ� ���� textList ����
								sStart = TokenList.get(sec).getToken(index).location; // ���� ��ɾ�� ���� ������ ���� ��ɾ� �̱� ������ ���� �ּҸ� sStart�� �־��ֱ�
								sLen = TokenList.get(sec).getToken(index).byteSize; // sLen�� ���� ��ɾ��� ũ��� �ʱ�ȭ
								textList.add(TokenList.get(sec).getToken(index).objectCode); // textList�� ���� ��ɾ��� Object code �߰�
							} else if (TokenList.get(sec).tokenList.size() == index + 1	|| TokenList.get(sec).getToken(index + 1).objectCode.equals("")) {
								// ���� �ε����� TokenTable�� ũ��� ���ų� (���� �ε����� TokenTable�� ������ �ε���) ���� ��ɾ��� object code�� ""��� (����ִٸ�)
								textList.add(TokenList.get(sec).getToken(index).objectCode); // textList�� ���� ��ɾ��� Object code �߰�
								code = String.format("T%06X%02X", sStart, sLen); // T^���� �����ּ�^���� ���� �������� code�� �־��ֱ�
								codeList.add(code);; // code�� codeList�� �߰�
								for (int i = 0; i < textList.size(); i++) { // textList ó������ ������ �ݺ�
									code = textList.get(i); // textList�� i��°�� �ִ� ������ code�� �־��ֱ�
									codeList.add(code); // code�� codeList�� �߰�
								}
								code = "\n"; // text record �� ���� ���� �� �ٹٲٱ� ���� "\n"�� code�� �־��ֱ�
								codeList.add(code); // code�� codeList�� �߰�
								sLen = 0; // sLen�� 0���� �ʱ�ȭ
								textList.clear(); // ���� ������ text�� �־��ֱ� ���� textList ����
							} else { // ���� ������ �ƴ϶��
								textList.add(TokenList.get(sec).getToken(index).objectCode); // textList�� ���� ��ɾ��� Object code �߰�
							}
						}
					}
					if (TokenList.get(sec).tokenList.size() == index + 1) { // ���� �ε����� TokenTable�� ũ��� ���ٸ� (���� �ε����� TokenTable�� ������ �ε���)
						
						/** Modification record */
						int ref = -1; // EXTREF�� �ִ� Token�� �ε����� ���� ������ -1�� �ʱ�ȭ
						for (int i = 0; i < TokenList.get(sec).tokenList.size(); i++) { // �ش� section�� TokenTable�� ũ�⸸ŭ �ݺ� (�ش� section�� line ������ŭ)
							if (TokenList.get(sec).getToken(i).operator != null) { // �ش� operator�� null�� �ƴ϶�� (�ִٸ�)
								if (TokenList.get(sec).getToken(i).operator.equals("EXTREF")) { // �ش� operator�� EXTREF���
									ref = i; // i�� ref�� �־��ֱ�
								}
							}
							char[] cntzero = TokenList.get(sec).getToken(i).objectCode.toCharArray(); // Object code�� �� ���ھ� �߶� �迭�� �ֱ� (���� ���ӵ� 0�� ������ ���� ����)
							int cnt = 0; // 0�� ������ ���� cnt ���� �ʱ�ȭ
							for (int cnti = TokenList.get(sec).getToken(i).objectCode.length() - 1; cnti > -1; cnti--) { // Object�� ���������� ù��° ���ڱ��� �ݺ�
								if (cntzero[cnti] == '0') { // �ش� ���ڰ� 0�̶��
									cnt++; //cnt�� 1 ����
								} else // 0�� �ƴ϶��
									break; // �ݺ��� ����
							}
							if (ref != -1 && i != ref && TokenList.get(sec).getToken(i).operand != null && !TokenList.get(sec).getToken(i).objectCode.equals("")) {
								// EXTREF�� ������ ���� Token�� �ƴϰ�(�ʱⰪ -1 ����) ���� ��ɾ EXTREF�� �ƴϰ� operand�� ������� ������ object code�� ������� ������
								
								/* COPY ���α׷����� "-"�� �־ ������ ���� */
								if (TokenList.get(sec).getToken(i).operand[0].contains("-")) { //operand�� "-"�� �ִٸ�
									String[] array = TokenList.get(sec).getToken(index).operand[0].split("-"); // "-"�� �������� operand�� �߶� array �迭�� �ֱ�
									for (int k = 0; k < array.length; k++) { // array �迭�� ���̸�ŭ �ݺ�
										for (int j = 0; j < TokenList.get(sec).getToken(ref).operand.length; j++) { // EXTREF�� operand ������ŭ �ݺ�
											if (array[k].equals(TokenList.get(sec).getToken(ref).operand[j])) { // array�� k��° �迭�� �ִ� ���ڿ��� EXTREF�� operand�� �ִ� ���ڿ��̶��
												if (cnt == 6) { // cnt�� 6�̶��
													code = String.format("M%06X%02X", TokenList.get(sec).getToken(i).location, cnt);
													// M^�ּ�^���� �ִ� 0�� ���� �������� code�� �־��ֱ�
													codeList.add(code); // code�� codeList�� �߰�
												} else if (cnt == 5) { // cnt�� 5���
													code = String.format("M%06X%02X", TokenList.get(sec).getToken(i).location + 1, cnt);
													// M^�ּ�^���� �ִ� 0�� ���� �������� code�� �־��ֱ�(0�� ������ 5�϶��� location�� 1 ����)
													codeList.add(code); // code�� codeList�� �߰�
												}
												if (k == 0) { // ���� �տ� �ִ� ���ڿ��̶�� (��ȣ�� ���°� + ����)
													code = "+"; // code�� "+" �־��ֱ�
													codeList.add(code); // code�� codeList�� �߰�
												} else { // ���� �տ� �ִ� ���ڿ��̾ƴ϶�� ("-"�� �������� �߶��� ������ ��ȣ�� -)
													code = "-"; // code�� "+" �־��ֱ�
													codeList.add(code); // code�� codeList�� �߰�
												}
												code = String.format("%s\n", array[k]); // array�� k��° �迭�� code�� �־��ֱ�
												codeList.add(code); // code�� codeList�� �߰�
											}
										}
									}
								} else { //operand�� "-"�� ���ٸ�
									for (int j = 0; j < TokenList.get(sec).getToken(ref).operand.length; j++) { // EXTREF�� operand ������ŭ �ݺ�
										if (TokenList.get(sec).getToken(i).operand[0].equals(TokenList.get(sec).getToken(ref).operand[j])) {
											 // ���� ��ɾ��� operand�� EXTREF�� operand�� �ִ� ���ڿ��̶��
											if (cnt == 6) { // cnt�� 6�̶��
												code = String.format("M%06X%02X+%s\n", TokenList.get(sec).getToken(i).location, cnt, TokenList.get(sec).getToken(i).operand[0]);
												// M^�ּ�^���� �ִ� 0�� ����^+���� ��ɾ��� operand �������� code�� �־��ֱ�
												codeList.add(code); // code�� codeList�� �߰�
											} else if (cnt == 5) { // cnt�� 5���
												code = String.format("M%06X%02X+%s\n", TokenList.get(sec).getToken(i).location + 1, cnt, TokenList.get(sec).getToken(i).operand[0]);
												// M^�ּ�^���� �ִ� 0�� ����^+���� ��ɾ��� operand �������� code�� �־��ֱ�(0�� ������ 5�϶��� location�� 1 ����)
												codeList.add(code); // code�� codeList�� �߰�
											}
										}
									}
								}
							}
						}
						
						/** End record */
						code = "E"; // code�� "E" �־��ֱ�
						codeList.add(code); // code�� codeList�� �߰�
						
						/** START�� �ִ� section�� End record�� �����ּҸ� ����ϱ� ���� �ݺ��� */
						for (int i = 0; i < TokenList.get(sec).tokenList.size(); i++) { // �ش� section�� TokenTable�� ũ�⸸ŭ �ݺ� (�ش� section�� line ������ŭ)
							if (TokenList.get(sec).getToken(i).operator != null) { // �ش� operator�� null�� �ƴ϶�� (�ִٸ�)
								if (TokenList.get(sec).getToken(i).operator.equals("START")) { // �ش� operator�� START ���
									code = String.format("%06X", TokenList.get(sec).getToken(i).location); // ���� �ּҸ� code�� �ֱ�
									codeList.add(code); // code�� codeList�� �߰�
									break; // �ݺ��� ����
								}
							}
						}
					}
				}
			}
			code = "\n\n"; // �� section �ۼ� ���� �� ���� ���� ���� "\n\n"�� code�� �־��ֱ�
			codeList.add(code); // code�� codeList�� �߰�
		}
	}

	/**
	 * �ۼ��� codeList�� ������¿� �°� ����Ѵ�.
	 * @param fileName : ����Ǵ� ���� �̸�
	 */
	private void printObjectCode(String fileName) {
		try {
			File file = new File(fileName); // ���� ��ü ����
			PrintWriter pw = new PrintWriter(file); // ��� ��Ʈ�� ����
			for (int i = 0; i < codeList.size(); i++) { // codeList�� ũ�⸸ŭ �ݺ�
				pw.print(codeList.get(i)); // ������� codeList ���� ���
			}
			pw.close(); // ��� ��Ʈ�� �ݱ�
		} catch (IOException e) { // ���� �߻���
			System.out.println("�ۼ��� ������ �����ϴ�."); // ������ ���ٰ� ���
		}
	}

}
