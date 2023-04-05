package SP20_simulator;

import SP20_simulator.ResourceManager.ObjectCode;

// instruction에 따라 동작을 수행하는 메소드를 정의하는 클래스

public class InstLuncher {
    ResourceManager rMgr;
    
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
    int index = 0;

    public InstLuncher(ResourceManager resourceManager) {
        this.rMgr = resourceManager;
    }
    
    public void run() {
    	/*nixbpe 플래그 선언 */
    	int nFlag = 0;
		int iFlag = 0;
		int xFlag = 0;
		int bFlag = 0;
		int pFlag = 0;
		int eFlag = 0;
		
		String Ob = ""; // 자른 오브젝트 코드를 넣을 변수와
		int Op = -1; // opcode 구별하여 넣을 변수
		ObjectCode object = null; // ObjectCode의 여러가지 정보를 담을 클래스도 선언 
		
		while(rMgr.memory[rMgr.getRegister(PC)] == null) { // PC레지스터가 비어있을 경우
			rMgr.setRegister(PC, rMgr.getRegister(PC) + 1); // 비어있지 않은 PC 레지스터가 나올 때까지 이동
		}
		
    	if (rMgr.memory[rMgr.getRegister(PC)] != null) { // PC 레지스터가 비어있지 않을 경우
    		int first = rMgr.StringToInt(rMgr.getMemory(rMgr.getRegister(PC), 2)[0]); // 메모리 한개를 읽어와서 first에 넣고 
    		if(rMgr.getMemory(rMgr.getRegister(PC), 2)[1] != null) { // 그 다음 메모리도 비어있지 않다면
    			int second = rMgr.StringToInt(rMgr.getMemory(rMgr.getRegister(PC), 2)[1]); // 읽어와서 second에 넣기
    			
    			/* nixbpe Flag 계산해서 넣어주기 */
    			iFlag = first % 2;
				nFlag = (first / 2) % 2;
				eFlag = ((second & 0xF0) >> 4) % 2;
				pFlag = (((second & 0xF0) >> 4) /2) % 2;
				bFlag = ((((second & 0xF0) >> 4) /2) /2) % 2;
				xFlag = (((((second & 0xF0) >> 4) /2) /2) /2) % 2;
				
				/* Flag를 이용하여 각 몇바이트 명령어인지 알아내고 해당 길이 만큼 Ob에 넣고 Op를 계산 */
				if(nFlag == 0 && iFlag == 0) { //2bytes
					Ob = rMgr.getMemory(rMgr.getRegister(PC), 2)[0] + rMgr.getMemory(rMgr.getRegister(PC), 2)[1];
					Op = rMgr.StringToInt(rMgr.getMemory(rMgr.getRegister(PC), 2)[0]);
					rMgr.setRegister(PC, rMgr.getRegister(PC) + 2);
				}
				else {
					if (eFlag == 1) { //4bytes
						Ob = rMgr.getMemory(rMgr.getRegister(PC), 4)[0] + rMgr.getMemory(rMgr.getRegister(PC), 4)[1] + rMgr.getMemory(rMgr.getRegister(PC), 4)[2] + rMgr.getMemory(rMgr.getRegister(PC), 4)[3];
						Op = rMgr.StringToInt(rMgr.getMemory(rMgr.getRegister(PC), 4)[0]) - (nFlag * 2) - (iFlag * 1);
						rMgr.setRegister(PC, rMgr.getRegister(PC) + 4);
					}
					else { //3bytes
						Ob = rMgr.getMemory(rMgr.getRegister(PC), 3)[0] + rMgr.getMemory(rMgr.getRegister(PC), 3)[1] + rMgr.getMemory(rMgr.getRegister(PC), 3)[2];
						Op = rMgr.StringToInt(rMgr.getMemory(rMgr.getRegister(PC), 4)[0]) - (nFlag * 2) - (iFlag * 1);
						rMgr.setRegister(PC, rMgr.getRegister(PC) + 3);
					}
				}
    		}
    		/* 두번째가 비어있다면 한바이트만 가져오고 Op는 -1로 두기 */
			else {
				Ob = rMgr.getMemory(rMgr.getRegister(PC), 2)[0];
				Op = -1;
			}
    		/* 계산한 내용을 ObjectCode 클래스의 리스트에 기존 내용에서 새로운 내용으로 바꾸기 */
    		rMgr.setObjectCode(index, Ob, Op, nFlag, iFlag, xFlag, bFlag, pFlag, eFlag);
    		object = rMgr.getObjectCode(index); // 함수에 파라미터로 쓰일 objectCode는 방금 넣은 그 objectCode
    	}
    		
    	/* 위에서 계산한 Op를 기준으로 함수 실행 */
    	switch(Op) { 
    	case 0x14:
    		STL(object);
    		break;
    	case 0x48:
    		JSUB(object);
    		break;	
    	case 0x00:
    		LDA(object);
    		break;
    	case 0x28:
    		COMP(object);
    		break;
    	case 0x30:
    		JEQ(object);
    		break;
    	case 0x3C:
    		J(object);
    		break;
    	case 0x0C:
    		STA(object);
    		break;
    	case 0xB4:
    		CLEAR(object);
    		break;
    	case 0x74:
    		LDT(object);
    		break;
    	case 0xE0:
    		TD(object);
    		break;
    	case 0xD8:
    		RD(object);
    		break;
    	case 0xA0:
    		COMPR(object);
    		break;
    	case 0x54:
    		STCH(object);
    		break;
    	case 0xB8:
    		TIXR(object);
    		break;
    	case 0x38:
    		JLT(object);
    		break;
    	case 0x10:
    		STX(object);
    		break;
    	case 0x4C:
    		RSUB(object);
    		break;
    	case 0x50:
    		LDCH(object);
    		break;
    	case 0xDC:
    		WD(object);
    		break;
    	default :
    		System.out.println("Object Code가 아닙니다.");
    	}
    	/* 인덱스가 0이 아니고, 8번(PC)레지스터가 끝나는 주소를 가리킬 때는 프로그램이 끝난 이후 이기때문에 해당 조건일 경우 함수 종료 */
    	if (index  != 0 && rMgr.getRegister(PC) == rMgr.StringToInt(rMgr.EndADDR)) {
			return;
		}
    	index++;
    }

    /* L레지스터의 값을 타겟 주소의 메모리에 넣기 */
    public void STL(ObjectCode Ob) {
    	/* pFlag 값으로 PC 주소지정인지 판단하여 타겟 주소에 PC 더하기 */
    	int TA = - 1;
    	if (rMgr.getObjectCode(index).pFlag == 1) {
    		TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3)) + rMgr.getRegister(PC);
    	}
    	else {
    		TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3));
    	}
    	/* L레지스터 값을 메모리에 넣기 위해 String으로 바꾸고 char 배열로 바꾼 후 메모리에 넣기 */
    	String L_Reg = String.format("%06X", rMgr.getRegister(L));
		char[] data = new char[60];
		int num = 3;
		for(int i=0;i<L_Reg.length();i++){
			data[i] = (L_Reg.charAt(i));//스트링을 한글자씩 끊어 배열에 저장
		}
    	rMgr.setMemory(TA, data, num);
    	rMgr.setOperator(index, "STL", TA);
    	/*System.out.println(rMgr.getRegister(L));
    	System.out.println(L_Reg);
    	System.out.println(rMgr.getMemory(TA, num));*/
    }
    
    /* PC 레지스터의 값을 L레지스터에 넣고, 타겟 주소를 PC 레지스터에 넣기 */
    public void JSUB(ObjectCode Ob) {
    	/* pFlag 값으로 PC 주소지정인지 판단하여 타겟 주소에 PC 더하기 */
    	int TA = - 1;
    	if (rMgr.getObjectCode(index).pFlag == 1) {
    		TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3)) + rMgr.getRegister(PC);
    	}
    	else {
    		TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3));
    	}
    	rMgr.setRegister(L, rMgr.getRegister(PC)); // PC레지스터에 있는 값을 L레지스터에 넣기
    	rMgr.setRegister(PC, TA); // TA를 PC레지스터에 넣기
    	rMgr.setOperator(index, "JSUB", TA);
    }
    
    /* 타겟 주소의 메모리에 있는 값을 3개 읽어와서 A 레지스터에 넣기 */
    public void LDA(ObjectCode Ob) {
    	/* pFlag 값으로 PC 주소지정인지 판단하여 타겟 주소에 PC 더하기 */
    	int TA = - 1;
    	if (rMgr.getObjectCode(index).pFlag == 1) {
    		TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3)) + rMgr.getRegister(PC);

    		/* TA를 메모리 안에 있는 값을 3개 가져와서 String으로 이어붙인 후 다시 정수형으로 변환*/
        	String strTA = "";
        	for (int i=0; i<3; i++) {
        		if(rMgr.getMemory(TA, 3)[i] == null) {
        			strTA += "00";
        		}
        		else {
        			strTA += rMgr.getMemory(TA, 3)[i];
        		}
    		}
        	TA = rMgr.StringToInt(strTA);
    	}
    	else {
    		TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3));
    	}
    	rMgr.setRegister(A, TA);  // TA를 A레지스터에 넣기
    	rMgr.setOperator(index, "LDA", TA);
    }
    
    /* A 레지스터의 값과 타겟 주소의 메모리에 있는 값을 3개 읽어와서 비교 */
    public void COMP(ObjectCode Ob) {
    	/* pFlag 값으로 PC 주소지정인지 판단하여 타겟 주소에 PC 더하기 */
    	int TA = - 1;
    	if (rMgr.getObjectCode(index).pFlag == 1) {
    		TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3)) + rMgr.getRegister(PC);
    	}
    	else {
    		TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3));
    	}
    	/* A레지스터와 TA를 비교하여  CC 지정*/
    	if ((rMgr.getRegister(A)-TA) > 0) {
    		rMgr.setRegister(SW, 1);
    	}
    	else if ((rMgr.getRegister(A)-TA) < 0) {
    		rMgr.setRegister(SW, -1);
    	}
    	else {
    		rMgr.setRegister(SW, 0);
    	}
    	rMgr.setOperator(index, "COMP", TA);
    }
    
    /* CC가 같으면 (SW 레지스터의 값이 0이라면) 타겟 주소를 PC 레지스터에 넣기 */
    public void JEQ(ObjectCode Ob) {
    	/* pFlag 값으로 PC 주소지정인지 판단하여 타겟 주소에 PC 더하기 */
    	int TA = - 1;
    	if (rMgr.getObjectCode(index).pFlag == 1) {
    		if (rMgr.getObjectCode(index).Object.substring(3).charAt(0) == 'F') { // 음수일경우 계산
    			TA = rMgr.getRegister(PC) - (0x1000 - rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3)));
    		}
    		else {
    			TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3)) + rMgr.getRegister(PC);
    		}
    	}
    	else {
    		TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3));
    	}
    	
    	if (rMgr.getRegister(SW) == 0) { // CC가 같으면
    		rMgr.setRegister(PC, TA); // TA를 PC레지스터에 넣기
    	}
    	rMgr.setOperator(index, "JEQ", TA);
    }
    
    /* 타겟 주소를 PC 레지스터에 넣기 */
    public void J(ObjectCode Ob) {
    	/* pFlag 값으로 PC 주소지정인지 판단하여 타겟 주소에 PC 더하기 */
    	int TA = - 1;
    	if (rMgr.getObjectCode(index).pFlag == 1) {
    		if (rMgr.getObjectCode(index).Object.substring(3).charAt(0) == 'F') { // 음수일경우 계산
    			TA = rMgr.getRegister(PC) - (0x1000 - rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3)));
    		}
    		else {
    			TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3)) + rMgr.getRegister(PC);
    		}
    	}
    	else {
    		TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3));
    	}
    	
    	/* 간접 주소일경우  TA 계산 없이 주소 그대로 */
    	if (rMgr.getObjectCode(index).nFlag == 1 && rMgr.getObjectCode(index).iFlag == 0) {
    		TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3));
    	}
    	rMgr.setRegister(PC, TA); // TA를 PC레지스터에 넣기
    	rMgr.setOperator(index, "J", TA);
    }
    
    /* A레지스터의 값을 타겟 주소의 메모리에 넣기 */
    public void STA(ObjectCode Ob) {
    	/* pFlag 값으로 PC 주소지정인지 판단하여 타겟 주소에 PC 더하기 */
    	int TA = - 1;
    	if (rMgr.getObjectCode(index).pFlag == 1) {
    		TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3)) + rMgr.getRegister(PC);
    	}
    	else {
    		TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3));
    	}
    	
    	/* A레지스터 값을 메모리에 넣기 위해 String으로 바꾸고 char 배열로 바꾼 후 메모리에 넣기 */

    	String A_Reg = String.format("%06X", rMgr.getRegister(A));
		char[] data = new char[60];
		int num = 3;
		for(int i=0;i<A_Reg.length();i++){
			data[i] = (A_Reg.charAt(i));//스트링을 한글자씩 끊어 배열에 저장
		}
    	rMgr.setMemory(TA, data, num);
    	rMgr.setOperator(index, "STA", TA);
    }
    
    /* 해당 레지스터를 0을 넣기 */
    public void CLEAR(ObjectCode Ob) {
    	int TA = -1;
    	/* 해당 레지스터 구하기 */
    	int r1 = rMgr.StringToInt(Character.toString(rMgr.getObjectCode(index).Object.charAt(2)));
    	rMgr.setRegister(r1, 0); // 그 레지스터를 0으로
    	rMgr.setOperator(index, "CLEAR", TA);
    }
    
    /* 타겟 주소의 메모리에 있는 값을 3개 읽어와서 ㅆ 레지스터에 넣기 */
    public void LDT(ObjectCode Ob) {
    	/* pFlag 값으로 PC 주소지정인지 판단하여 타겟 주소에 PC 더하기 */
    	int TA = - 1;
    	if (rMgr.getObjectCode(index).pFlag == 1) {
    		TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3)) + rMgr.getRegister(PC);
    	}
    	else {
    		TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3));
    	}
    	
    	/* TA를 메모리 안에 있는 값을 3개 가져와서 String으로 이어붙인 후 다시 정수형으로 변환*/
    	String strTA = "";
    	for (int i=0; i<3; i++) {
    		if(rMgr.getMemory(TA, 3)[i] == null) {
    			strTA += "00";
    		}
    		else {
    			strTA += rMgr.getMemory(TA, 3)[i];
    		}
		}
    	rMgr.setRegister(T, rMgr.StringToInt(strTA));
    	rMgr.setOperator(index, "LDT", TA);
    }
    
    /* 디바이스가 사용할 수 있는 상태인지 판단하여 CC 지정 */
    public void TD(ObjectCode Ob) {
    	/* pFlag 값으로 PC 주소지정인지 판단하여 타겟 주소에 PC 더하기 */
    	int TA = - 1;
    	if (rMgr.getObjectCode(index).pFlag == 1) {
    		TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3)) + rMgr.getRegister(PC);
    	}
    	else {
    		TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3));
    	}
    	rMgr.testDevice(rMgr.getMemory(TA, 1)[0]); // 리소스 매니저에서 testDevice 호출
    	rMgr.setOperator(index, "TD", TA);
    }
    
    /* 디바이스의 내용을 읽어서 A 레지스터의 가장 오른쪽에 넣기 */
    public void RD(ObjectCode Ob) {
    	/* pFlag 값으로 PC 주소지정인지 판단하여 타겟 주소에 PC 더하기 */
    	int TA = - 1;
    	if (rMgr.getObjectCode(index).pFlag == 1) {
    		TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3)) + rMgr.getRegister(PC);
    	}
    	else {
    		TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3));
    	}
    	
    	/*A레지스터의 가장 오른쪽 메모리 비우고 읽어오기*/
    	rMgr.setRegister(A, (rMgr.getRegister(A) << 4) & 0xF);
    	rMgr.setRegister(A, rMgr.getRegister(A) + (int) rMgr.readDevice(rMgr.getMemory(TA, 1)[0], 1)[0]);  // 리소스 매니저에서 readDevice 호출
    	rMgr.setOperator(index, "RD", TA);
    }
    
    /* 해당 레지스터 2개를 비교하여 CC 지정 */
    public void COMPR(ObjectCode Ob) {
    	int TA = -1;
    	/* 해당 레지스터 2개를 위치로 가져오고 비교하여 CC 지정 */
    	int r1 = rMgr.getRegister(rMgr.StringToInt(Character.toString(rMgr.getObjectCode(index).Object.charAt(2))));
    	int r2 = rMgr.getRegister(rMgr.StringToInt(Character.toString(rMgr.getObjectCode(index).Object.charAt(3))));
    	if ((r1-r2) > 0) {
    		rMgr.setRegister(SW, 1);
    	}
    	else if ((r1-r2) < 0) {
    		rMgr.setRegister(SW, -1);
    	}
    	else {
    		rMgr.setRegister(SW, 0);
    	}
    	rMgr.setOperator(index, "COMPR", TA);
    }
    
    /* A 레지스터의 가장 오른쪽에 있는 값을 타겟 주소의 메모리에 넣기 */
    public void STCH(ObjectCode Ob) {
    	/* pFlag 값으로 PC 주소지정인지 판단하여 타겟 주소에 PC 더하기, 이 때 X레지스터도 고려해야함 */
    	int TA = - 1;
    	if (rMgr.getObjectCode(index).pFlag == 1) {
    		if (rMgr.getObjectCode(index).xFlag == 1) {
    			TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3)) + rMgr.getRegister(PC) + rMgr.getRegister(X);
    		}
    		else {
    			TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3)) + rMgr.getRegister(PC);
    		}
    	}
    	else {
    		if (rMgr.getObjectCode(index).xFlag == 1) {
    			TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3)) + rMgr.getRegister(X);
    		}
    		else {
    			TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3));
    		}
    	}
    	/* A레지스터의 가장 오른쪽 값을 메모리에 넣기 위해 char 배열로 바꾼 후 메모리에 넣기 */
    	char [] data = new char[60];
    	data[0] = (char) (rMgr.getRegister(A) & 0xFF);
    	rMgr.setMemory(TA, data, 1);
    	rMgr.setOperator(index, "STCH", TA);
    }
    
    /* X레지스터의 값을 1 증가해주고, 해당 레지스터의 값과 X레지스터의 값과 비교하여 CC 지정 */
    public void TIXR(ObjectCode Ob) {
    	int TA = -1;
    	/* 해당 레지스터의 값을 구하고 */
    	int r1 = rMgr.getRegister(rMgr.StringToInt(Character.toString(rMgr.getObjectCode(index).Object.charAt(2))));
    	rMgr.setRegister(X, rMgr.getRegister(X) + 1); // X레지스터의 값을 1 증가해준 다음에
    	
    	/* 비교하여 CC 지정 */
    	if ((rMgr.getRegister(X)-r1) > 0) {
    		rMgr.setRegister(SW, 1);
    	}
    	else if ((rMgr.getRegister(X)-r1) < 0) {
    		rMgr.setRegister(SW, -1);
    	}
    	else {
    		rMgr.setRegister(SW, 0);
    	}
    	rMgr.setOperator(index, "TIXR", TA);
    }
    
    /* CC가 작으면 (SW 레지스터의 값이 음수라면) 타겟 주소를 PC 레지스터에 넣기 */
    public void JLT(ObjectCode Ob) {
    	/* pFlag 값으로 PC 주소지정인지 판단하여 타겟 주소에 PC 더하기 */
    	int TA = - 1;
    	if (rMgr.getObjectCode(index).pFlag == 1) {
    		if (rMgr.getObjectCode(index).Object.substring(3).charAt(0) == 'F') { // 음수일경우 계산
    			TA = rMgr.getRegister(PC) - (0x1000 - rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3)));
    		}
    		else {
    			TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3)) + rMgr.getRegister(PC);
    		}
    	}
    	else {
    		TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3));
    	}
    	
    	if (rMgr.getRegister(SW) < 0) { // CC가 작다면
    		rMgr.setRegister(PC, TA); // TA를 PC레지스터에 넣기
    	}
    	rMgr.setOperator(index, "JLT", TA);
    }
    
    /* X레지스터의 값을 타겟 주소의 메모리에 넣기 */
    public void STX(ObjectCode Ob) {
    	/* pFlag 값으로 PC 주소지정인지 판단하여 타겟 주소에 PC 더하기 */
    	int TA = - 1;
    	if (rMgr.getObjectCode(index).pFlag == 1) {
    		TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3)) + rMgr.getRegister(PC);
    	}
    	else {
    		TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3));
    	}
    	/* X레지스터 값을 메모리에 넣기 위해 String으로 바꾸고 char 배열로 바꾼 후 메모리에 넣기 */
    	String X_Reg = String.format("%06X", rMgr.getRegister(X));
		char[] data = new char[60];
		int num = 3;
		for(int i=0;i<X_Reg.length();i++){
			data[i] = (X_Reg.charAt(i));//스트링을 한글자씩 끊어 배열에 저장
		}
    	rMgr.setMemory(TA, data, num);
    	rMgr.setOperator(index, "STX", TA);
    }
    
    /* L 레지스터에 있는 값을 PC 레지스터에 넣기 */
    public void RSUB(ObjectCode Ob) {
    	int TA = -1;
    	rMgr.Device = ""; // 해당 서브루틴이 끝났으므로 디바이스 해제
    	rMgr.setRegister(PC, rMgr.getRegister(L));  // L 레지스터의 값을  PC레지스터에 넣기
    	rMgr.setOperator(index, "RSUB", TA);
    }
    
    /* 타겟 주소의 메모리에 있는 값을 읽어와서 A 레지스터에 가장 오른쪽에 넣기 */
    public void LDCH(ObjectCode Ob) {
    	/* pFlag 값으로 PC 주소지정인지 판단하여 타겟 주소에 PC 더하기, 이 때 X레지스터도 고려해야함 */
    	int TA = - 1;
    	if (rMgr.getObjectCode(index).pFlag == 1) {
    		if (rMgr.getObjectCode(index).xFlag == 1) {
    			TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3)) + rMgr.getRegister(PC) + rMgr.getRegister(X);
    		}
    		else {
    			TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3)) + rMgr.getRegister(PC);
    		}
    	}
    	else {
    		if (rMgr.getObjectCode(index).xFlag == 1) {
    			TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3)) + rMgr.getRegister(X);
    		}
    		else {
    			TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3));
    		}
    	}
    	
    	/* A 레지스터의 가장 오른 쪽 값을 비우고 */
    	rMgr.setRegister(A, (rMgr.getRegister(A) << 4) & 0xF);
    	try {
    		rMgr.setRegister(A, rMgr.StringToInt(rMgr.getMemory(TA, 1)[0].toString())); // 아스키 코드라면 16진수 문자를 숫자로 바꾸어 넣고
    	} catch (NumberFormatException e) {
    		rMgr.setRegister(A, rMgr.getMemory(TA, 1)[0].charAt(0)); // 아니라면 char형으로 변환하여 넣기
    	}
    	rMgr.setOperator(index, "LDCH", TA);
    }
    
    /* A 레지스터의 가장 오른쪽에 있는 내용을 읽어서 해당 디바이스에 넣기 */
    public void WD(ObjectCode Ob) {
    	/* pFlag 값으로 PC 주소지정인지 판단하여 타겟 주소에 PC 더하기 */
    	int TA = - 1;
    	if (rMgr.getObjectCode(index).pFlag == 1) {
    		TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3)) + rMgr.getRegister(PC);
    	}
    	else {
    		TA = rMgr.StringToInt(rMgr.getObjectCode(index).Object.substring(3));
    	}
    	/* A레지스터의 가장 오른쪽 값을 디바이스에 넣기 위해 char 배열로 바꾼 후 메모리에 넣기 */
    	char[] data = new char[60];
    	data[0] = (char) rMgr.getRegister(A);
    	rMgr.writeDevice(rMgr.getMemory(TA, 1)[0], data, 1);  // 리소스 매니저에서 writeDevice 호출
    	rMgr.setRegister(A, (rMgr.getRegister(A) << 4) & 0xF); // A레지스터 비우기
    	rMgr.setOperator(index, "WD", TA);
    }
    
}