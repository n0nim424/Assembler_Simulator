package SP20_simulator;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;



/**
 * ResourceManager는 컴퓨터의 가상 리소스들을 선언하고 관리하는 클래스이다.
 * 크게 네가지의 가상 자원 공간을 선언하고, 이를 관리할 수 있는 함수들을 제공한다.<br><br>
 * 
 * 1) 입출력을 위한 외부 장치 또는 device<br>
 * 2) 프로그램 로드 및 실행을 위한 메모리 공간. 여기서는 64KB를 최대값으로 잡는다.<br>
 * 3) 연산을 수행하는데 사용하는 레지스터 공간.<br>
 * 4) SYMTAB 등 simulator의 실행 과정에서 사용되는 데이터들을 위한 변수들. 
 * <br><br>
 * 2번은 simulator위에서 실행되는 프로그램을 위한 메모리공간인 반면,
 * 4번은 simulator의 실행을 위한 메모리 공간이라는 점에서 차이가 있다.
 */
public class ResourceManager{
	/**
	 * 디바이스는 원래 입출력 장치들을 의미 하지만 여기서는 파일로 디바이스를 대체한다.<br>
	 * 즉, 'F1'이라는 디바이스는 'F1'이라는 이름의 파일을 의미한다. <br>
	 * deviceManager는 디바이스의 이름을 입력받았을 때 해당 이름의 파일 입출력 관리 클래스를 리턴하는 역할을 한다.
	 * 예를 들어, 'A1'이라는 디바이스에서 파일을 read모드로 열었을 경우, hashMap에 <"A1", scanner(A1)> 등을 넣음으로서 이를 관리할 수 있다.
	 * <br><br>
	 * 변형된 형태로 사용하는 것 역시 허용한다.<br>
	 * 예를 들면 key값으로 String대신 Integer를 사용할 수 있다.
	 * 파일 입출력을 위해 사용하는 stream 역시 자유로이 선택, 구현한다.
	 * <br><br>
	 * 이것도 복잡하면 알아서 구현해서 사용해도 괜찮습니다.
	 */
	HashMap<String,Object> deviceManager = new HashMap<String,Object>();
	String[] memory = new String[65536]; // String으로 수정해서 사용하여도 무방함.
	int[] register = new int[10];
	double register_F;
	
	SymbolTable symtabList = new SymbolTable();

	/* 로더에 필요한 변수 선언 */
	ArrayList<String> Progname = new ArrayList<String>();
	ArrayList<String> ProgLength = new ArrayList<String>();
	ArrayList<String> StartADDR = new ArrayList<String>();
	String EndADDR = "";
	
	/* 파일 입출력에 필요한 변수 선언 */
	InputStream is = null;
	OutputStream os = null;
	int readindex = 0;
	String readD = "";
	String writeD = "";
	
	String Device = "";
	
	ArrayList<String> Log = new ArrayList<String>(); // 로그를 저장할 로그 리스트
	
	/* 레지스터를 편하게 다루기 위한 선언 */
	public static final int A = 0;
    public static final int X = 1;
    public static final int L = 2;
    public static final int B = 3;
    public static final int S = 4;
    public static final int T = 5;
    public static final int F = 6;
    public static final int PC = 8;
    public static final int SW = 9;
	
    /* ObjectCode를 다루기 위한 객체 선언 */
	class ObjectCode {
		String Object;
		String Operator;
		int Opcode;
		int TA = -1;
		int nFlag;
		int iFlag;
		int xFlag;
		int bFlag;
		int pFlag;
		int eFlag;
	}
	
	ArrayList<ObjectCode> ObjectList = new ArrayList<ObjectCode>(); // 리스트로 연결하여 사용

	/**
	 * 메모리, 레지스터등 가상 리소스들을 초기화한다.
	 */
	public void initializeResource(){
		// 기본값 0으로 초기화 되어 있어 따로 초기화 하지 않음
	}
	
	/**
	 * deviceManager가 관리하고 있는 파일 입출력 stream들을 전부 종료시키는 역할.
	 * 프로그램을 종료하거나 연결을 끊을 때 호출한다.
	 */
	public void closeDevice() {
		try {
			/* 입출력 스트림 닫기 */
			is.close();
			os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	/**
	 * 디바이스를 사용할 수 있는 상황인지 체크. TD명령어를 사용했을 때 호출되는 함수.
	 * 입출력 stream을 열고 deviceManager를 통해 관리시킨다.
	 * @param devName 확인하고자 하는 디바이스의 번호,또는 이름
	 */
	public void testDevice(String devName) {
		try {
			File file = new File(devName + ".txt"); // 파일 객체 생성, 편의를 위항 txt 파일
			if (is == null || devName == readD) { // 입력 스트림이 비어있거나 장치 이름이 읽기용이라면
				is = new FileInputStream(file); // 입력 스트림 생성
				readD = devName; // 해당 장치를 읽기용으로 저장
				Device = devName; // 사용중인 장치에 해당 장치 넣기 
				setRegister(SW, -1); // CC에 같지 않음 지정
			}
			else {
				if (os == null) { // 출력 스트림이 비어있다면
					os = new FileOutputStream(file); // 출력 스트림 생성
				}
				writeD = devName; // 해당 장치를 쓰기용으로 저장
				Device = devName; // 사용중인 장치에 해당 장치 넣기 
				setRegister(SW, 1); // CC에 같지 않음 지정
			}
		} catch (FileNotFoundException e) { // 읽을 파일을 찾을 수 없다면
			setRegister(SW, 0); // CC에 같음 지정
		}
	}
	
	public String getDevice(String devName) { // 사용중인 장치를 가져올 때 쓰임
		return devName;
	}

	/**
	 * 디바이스로부터 원하는 개수만큼의 글자를 읽어들인다. RD명령어를 사용했을 때 호출되는 함수.
	 * @param devName 디바이스의 이름
	 * @param num 가져오는 글자의 개수
	 * @return 가져온 데이터
	 */
	public char[] readDevice(String devName, int num){
		char [] result = new char[num];
		try {
			byte [] b = new byte [is.available()]; //임시로 읽는데 쓰는 공간
			while( is.read(b) != -1) {} // 읽어서 b에 넣기
			for (int i = 0; i < num; i++) {
				if (readindex >= b.length) { // 넣을 문자가 없다면 null 입력
					result[i] = '\u0000';
				}
				else {
					result[i] = (char) b[readindex]; // 결과 배열에 읽은 b배열 값 넣기
					readindex++;
				}
				//System.out.println(result[i]);
			}
        } catch (Exception e) {
            e.printStackTrace();
        }
		return result;
	}

	/**
	 * 디바이스로 원하는 개수 만큼의 글자를 출력한다. WD명령어를 사용했을 때 호출되는 함수.
	 * @param devName 디바이스의 이름
	 * @param data 보내는 데이터
	 * @param num 보내는 글자의 개수
	 */
	public void writeDevice(String devName, char[] data, int num){
		try {
			/* char 배열을 byte 배열로 변경하여 쓰기 */
			byte[] b = new byte[1];
			for (int i = 0; i < num; i++) {
				b[i] = (byte) data[i];
			}
			os.write(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	/**
	 * 메모리의 특정 위치에서 원하는 개수만큼의 글자를 가져온다.
	 * @param location 메모리 접근 위치 인덱스
	 * @param num 데이터 개수
	 * @return 가져오는 데이터
	 */
	public String[] getMemory(int location, int num){
		String[] mem = new String[65536];
		for (int i=0; i<num; i++) {
			mem[i] = memory[location+i];
		}
		return mem;
	}

	/**
	 * 메모리의 특정 위치에 원하는 개수만큼의 데이터를 저장한다. 
	 * @param locate 접근 위치 인덱스
	 * @param data 저장하려는 데이터
	 * @param num 저장하는 데이터의 개수
	 */
	public void setMemory(int locate, char[] data, int num){
		/* 2byte를 1byte로 패킹하여 저장 */
		for (int i = 0; i < num; i++) {
			String Sdata = "";
			for (int j = i*2; j < (i+1)*2; j++) {
			    Sdata += Character.toString(data[j]);
			}
			memory[locate + i] = Sdata;
		}
	}

	/**
	 * 번호에 해당하는 레지스터가 현재 들고 있는 값을 리턴한다. 레지스터가 들고 있는 값은 문자열이 아님에 주의한다.
	 * @param regNum 레지스터 분류번호
	 * @return 레지스터가 소지한 값
	 */
	public int getRegister(int regNum){
		return register[regNum];
		
	}

	/**
	 * 번호에 해당하는 레지스터에 새로운 값을 입력한다. 레지스터가 들고 있는 값은 문자열이 아님에 주의한다.
	 * @param regNum 레지스터의 분류번호
	 * @param value 레지스터에 집어넣는 값
	 */
	public void setRegister(int regNum, int value){
		register[regNum] = value;
	}

	/**
	 * 주로 레지스터와 메모리간의 데이터 교환에서 사용된다. int값을 char[]형태로 변경한다.
	 * @param data
	 * @return
	 */
	public String intToString(int data){
		return Integer.toHexString(data).toUpperCase(); // 16진수 문자열로 변경
	}

	/**
	 * 주로 레지스터와 메모리간의 데이터 교환에서 사용된다. char[]값을 int형태로 변경한다.
	 * @param data
	 * @return
	 */
	public int StringToInt(String data){
		return Integer.parseInt(data, 16); // 16진수
	}

	/* 프로그램 이름, 길이, 시작주소, 끝주소 등을 저장하고 불러오는 함수 */
	public void setProgname (String name, int currentSection) {
		Progname.add(currentSection, name);
	}
	
	public String getProgname (int currentSection) {
		return Progname.get(currentSection);
	}
	
	public void setProgLength (String length, int currentSection) {
		ProgLength.add(currentSection, length);
	}
	
	public String getProgLength (int currentSection) {
		return ProgLength.get(currentSection);
	}
	
	public void setStartADDR (String ADDR, int currentSection) {
		/* 앞에 섹션의 길이만큼 더해서 새로운 시작 주소를 지정 */
		if (currentSection == 0) {
			StartADDR.add(currentSection, ADDR);
		}
		else {
			int SADDR = 0;
			SADDR = StringToInt(getStartADDR(currentSection - 1)) + StringToInt(getProgLength(currentSection - 1));
			StartADDR.add(currentSection, intToString(SADDR));
		}
	}
	
	public String getStartADDR (int currentSection) {
		return StartADDR.get(currentSection);
	}
	
	public void setEndADDR (String ADDR) {
		EndADDR = ADDR;
	}
	
	public String getEndADDR () {
		return EndADDR;
	}
	
	/* ObjectCode 리스트가 이미 존재할 경우는 set, 없다면 add 함수 사용*/
	
	public void addObjectCode(int index, String Ob, int Op, int nFlag, int iFlag, int xFlag, int bFlag, int pFlag, int eFlag) {
		ObjectCode Object = new ObjectCode();
		Object.Object = Ob;
		Object.Opcode = Op;
		Object.nFlag = nFlag;
		Object.iFlag = iFlag;
		Object.xFlag = xFlag;
		Object.bFlag = bFlag;
		Object.pFlag = pFlag;
		Object.eFlag = eFlag;
		ObjectList.add(index, Object);
	}
	
	public void setObjectCode(int index, String Ob, int Op, int nFlag, int iFlag, int xFlag, int bFlag, int pFlag, int eFlag) {
		ObjectCode Object = new ObjectCode();
		Object.Object = Ob;
		Object.Opcode = Op;
		Object.nFlag = nFlag;
		Object.iFlag = iFlag;
		Object.xFlag = xFlag;
		Object.bFlag = bFlag;
		Object.pFlag = pFlag;
		Object.eFlag = eFlag;
		if (ObjectList.size() <= index) {
			ObjectList.add(index, Object);
		}
		else {
			ObjectList.set(index, Object);
		}
	}
	
	/* 결과창에 뿌려줄 operator 불러오는 함수 */
	public void setOperator(int index, String Operator, int TA) {
		ObjectCode Object = getObjectCode(index);
		Object.Operator = Operator;
		Object.TA = TA;
		ObjectList.set(index, Object);
	}
	
	public ObjectCode getObjectCode(int index) {
		return ObjectList.get(index);
	}
}