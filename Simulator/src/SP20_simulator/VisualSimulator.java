package SP20_simulator;

import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;

import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import javax.swing.SwingConstants;
import javax.swing.JTextArea;

/**
 * VisualSimulator는 사용자와의 상호작용을 담당한다.<br>
 * 즉, 버튼 클릭등의 이벤트를 전달하고 그에 따른 결과값을 화면에 업데이트 하는 역할을 수행한다.<br>
 * 실제적인 작업은 SicSimulator에서 수행하도록 구현한다.
 */
public class VisualSimulator {
	ResourceManager resourceManager = new ResourceManager();
	InstLuncher inst = new InstLuncher(resourceManager);
	SicLoader sicLoader = new SicLoader(resourceManager);
	SicSimulator sicSimulator = new SicSimulator(resourceManager, inst);
	
	/* 업데이트 할 텍스트 선언 */
	private JFrame frame;
	private JTextField PnameT;
	private JTextField SaddrT;
	private JTextField PlengthT;
	private JTextField EaddrT;
	private JTextField SmemT;
	private JTextField ADec;
	private JTextField XDec;
	private JTextField LDec;
	private JTextField BDec;
	private JTextField SDec;
	private JTextField TDec;
	private JTextField F;
	private JTextField PCDec;
	private JTextField SW;
	private JTextField AHex;
	private JTextField XHex;
	private JTextField LHex;
	private JTextField BHex;
	private JTextField SHex;
	private JTextField THex;
	private JTextField PCHex;
	private JTextField TAT;
	private JTextArea InstT;
	private JTextArea LogT;
	private JTextField DevT;
	
	int index = 0;
	
	/**
	 * 프로그램 로드 명령을 전달한다.
	 */
	public void load(File program){
		//...
		sicLoader.load(program);
		sicSimulator.load(program);
	};

	/**
	 * 하나의 명령어만 수행할 것을 SicSimulator에 요청한다.
	 */
	public void oneStep(){
		/* 인덱스가 0이 아니고, 8번(PC)레지스터가 끝나는 주소를 가리킬 때는 프로그램이 끝난 이후 이기때문에 부정하여 조건 지정 */
		if (!(index  != 0 && resourceManager.getRegister(8) == resourceManager.StringToInt(resourceManager.EndADDR))) {
			sicSimulator.oneStep(); // onestep 실행 
			try {
				/* 업데이트 시에 allstep의 경우 이중으로 나오는 일이 발생하여 업데이트 함수에서 실행하지 않음*/
				LogT.append(resourceManager.Log.get(index)); // 로그 출력
				if(index > 0) {
					InstT.append(resourceManager.getObjectCode(index).Object + "\n"); // 로더 시에 0번쨰 instruction은 이미 출력되었기 때문에 0 이상 조건 지정
				}
				index = sicSimulator.index; // 다음 인덱스로 이동, 인덱스는 시뮬레이터 클래스에서 증가
			} catch (IndexOutOfBoundsException e) {
				return;
			}
		}
	};

	/**
	 * 남아있는 모든 명령어를 수행할 것을 SicSimulator에 요청한다.
	 */
	public void allStep(){
		sicSimulator.allStep(); // allstep 실행
		int i = 0; // allstep이 끝난 후 로그와 instructuin을 출력하기 위한 인덱스
		while (!(index  != 0 && resourceManager.getRegister(8) == resourceManager.StringToInt(resourceManager.EndADDR))) { // 인덱스가 0이 아니고, 8번(PC)레지스터가 끝나는 주소를 가리킬 때는 프로그램이 끝난 이후 이기때문에 부정하여 조건 지정
			try {
			LogT.append(resourceManager.Log.get(i)); // 로그 출력
			if(i > 0) {
				InstT.append(resourceManager.getObjectCode(i).Object + "\n"); // 로더 시에 0번쨰 instruction은 이미 출력되었기 때문에 0 이상 조건 지정
			}
			i++; // 다음 인덱스로 이동
			} catch (IndexOutOfBoundsException e) {
				break;
			}
		}
		index = i; // allstep시 index가 계속 0에 머물러 있어 제대로 출력되지 않기 때문에 인덱스 지정
	};
	
	/**
	 * 화면을 최신값으로 갱신하는 역할을 수행한다.
	 */	
	public void update() {
		/* 각 세션의 프로그램 길이를 모두 더해 프로그램의 총길이를 구함 */
		int length = 0;
		for (int i = 0; i < resourceManager.ProgLength.size(); i++) {
			length += resourceManager.StringToInt(resourceManager.getProgLength(i));
		}
		
		for (int i = 0; i < resourceManager.memory.length; i++) {
			if (resourceManager.memory[i] != "") {
				SmemT.setText(Integer.toString(i));
				SmemT.setEnabled(false);
				break;
			}
		}
		
		/* 프로그램 이름 및 길이, 시작 주소, 레지스터와 같이 로더가 실행된 이후 텍스트를 출력해줌 */
		PnameT.setText(resourceManager.getProgname(0));
		PnameT.setEnabled(false);
		SaddrT.setText(resourceManager.getStartADDR(0));
		SaddrT.setEnabled(false);
		PlengthT.setText(resourceManager.intToString(length));
		PlengthT.setEnabled(false);
		EaddrT.setText(resourceManager.getEndADDR());
		EaddrT.setEnabled(false);
		
		ADec.setText(Integer.toString(resourceManager.getRegister(0)));
		ADec.setEnabled(false);
		XDec.setText(Integer.toString(resourceManager.getRegister(1)));
		XDec.setEnabled(false);
		LDec.setText(Integer.toString(resourceManager.getRegister(2)));
		LDec.setEnabled(false);
		BDec.setText(Integer.toString(resourceManager.getRegister(3)));
		BDec.setEnabled(false);
		SDec.setText(Integer.toString(resourceManager.getRegister(4)));
		SDec.setEnabled(false);
		TDec.setText(Integer.toString(resourceManager.getRegister(5)));
		TDec.setEnabled(false);
		F.setText(Integer.toString(resourceManager.getRegister(6)));
		F.setEnabled(false);
		PCDec.setText(Integer.toString(resourceManager.getRegister(8)));
		PCDec.setEnabled(false);
		SW.setText(Integer.toString(resourceManager.getRegister(9)));
		SW.setEnabled(false);
		
		AHex.setText(resourceManager.intToString(resourceManager.getRegister(0)));
		AHex.setEnabled(false);
		XHex.setText(resourceManager.intToString(resourceManager.getRegister(1)));
		XHex.setEnabled(false);
		LHex.setText(resourceManager.intToString(resourceManager.getRegister(2)));
		LHex.setEnabled(false);
		BHex.setText(resourceManager.intToString(resourceManager.getRegister(3)));
		BHex.setEnabled(false);
		SHex.setText(resourceManager.intToString(resourceManager.getRegister(4)));
		SHex.setEnabled(false);
		THex.setText(resourceManager.intToString(resourceManager.getRegister(5)));
		THex.setEnabled(false);
		PCHex.setText(resourceManager.intToString(resourceManager.getRegister(8)));
		PCHex.setEnabled(false);
		
		/* 실행중인 장치 */
		DevT.setText(resourceManager.Device);
		
		/* 로더 실행 후 가장 먼저 나오는 명령어 Instruction에 올려주기 */
		if (index == 0) {
			InstT.append(resourceManager.getObjectCode(index).Object + "\n");
		}
		
		/* 타겟 주소가 0보다 크거나 같으면 출력해주고 0보다 작으면 타겟 주소가 없는 명령어 */
		if (index > 0) {
			if (resourceManager.getObjectCode(index-1).TA >= 0) {
				TAT.setEnabled(true);
				TAT.setText(resourceManager.intToString(resourceManager.getObjectCode(index-1).TA));
			}
			else {
				TAT.setText("");
				TAT.setEnabled(false);
			}
		}
		
	}
	
	/* main에서 생성자를 만들고 프레임 실행 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					VisualSimulator window = new VisualSimulator();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/* 생성자에서 GUI 디자인 및 액션을 하는 함수인 initialize 실행 */
	public VisualSimulator() {
		initialize();
	}
	
	/* 프레임 안에 라벨, 텍스트 박스, 버튼과 같이 필요한 요소를 만들어줌 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 515, 650);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JLabel lblNewLabel = new JLabel("FileName : ");
		lblNewLabel.setBounds(12, 10, 75, 21);
		
		JTextField txtfilename = new JTextField();
		txtfilename.setBounds(80, 10, 130, 21);
		txtfilename.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		txtfilename.setColumns(10);
		
		JButton openbtn = new JButton("open");
		openbtn.setBounds(222, 10, 62, 21);
		
		frame.getContentPane().setLayout(null);
		frame.getContentPane().add(lblNewLabel);
		frame.getContentPane().add(txtfilename);
		frame.getContentPane().add(openbtn);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "H (Header Record)", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setBounds(12, 41, 236, 114);
		frame.getContentPane().add(panel);
		panel.setLayout(null);
		
		JLabel label = new JLabel("Program name : ");
		label.setBounds(12, 17, 120, 21);
		panel.add(label);
		
		JLabel lblNewLabel_1 = new JLabel("Start Address of");
		lblNewLabel_1.setBounds(12, 45, 120, 15);
		panel.add(lblNewLabel_1);
		
		JLabel lblNewLabel_1_2 = new JLabel("Object Program : ");
		lblNewLabel_1_2.setBounds(12, 59, 120, 15);
		panel.add(lblNewLabel_1_2);
		
		JLabel lblNewLabel_2 = new JLabel("Length of Program : ");
		lblNewLabel_2.setBounds(12, 81, 120, 21);
		panel.add(lblNewLabel_2);
		
		PnameT = new JTextField();
		PnameT.setBounds(128, 17, 96, 21);
		panel.add(PnameT);
		PnameT.setColumns(10);
		
		SaddrT = new JTextField();
		SaddrT.setBounds(128, 52, 96, 21);
		panel.add(SaddrT);
		SaddrT.setColumns(10);
		
		PlengthT = new JTextField();
		PlengthT.setBounds(128, 81, 96, 21);
		panel.add(PlengthT);
		PlengthT.setColumns(10);
		
		JPanel panel_1 = new JPanel();
		panel_1.setLayout(null);
		panel_1.setBorder(new TitledBorder(null, "Register", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.setBounds(12, 157, 236, 262);
		frame.getContentPane().add(panel_1);
		
		JLabel label_1 = new JLabel("A (#0)");
		label_1.setHorizontalAlignment(SwingConstants.RIGHT);
		label_1.setBounds(12, 32, 50, 21);
		panel_1.add(label_1);
		
		JLabel label_2 = new JLabel("X (#1)");
		label_2.setHorizontalAlignment(SwingConstants.RIGHT);
		label_2.setBounds(12, 56, 50, 21);
		panel_1.add(label_2);
		
		JLabel label_3 = new JLabel("L (#2)");
		label_3.setHorizontalAlignment(SwingConstants.RIGHT);
		label_3.setBounds(12, 81, 50, 21);
		panel_1.add(label_3);
		
		JLabel label_4 = new JLabel("B (#3)");
		label_4.setHorizontalAlignment(SwingConstants.RIGHT);
		label_4.setBounds(12, 106, 50, 21);
		panel_1.add(label_4);
		
		JLabel label_5 = new JLabel("S (#4)");
		label_5.setHorizontalAlignment(SwingConstants.RIGHT);
		label_5.setBounds(12, 131, 50, 21);
		panel_1.add(label_5);
		
		JLabel label_6 = new JLabel("T (#5)");
		label_6.setHorizontalAlignment(SwingConstants.RIGHT);
		label_6.setBounds(12, 156, 50, 21);
		panel_1.add(label_6);
		
		JLabel label_7 = new JLabel("F (#6)");
		label_7.setHorizontalAlignment(SwingConstants.RIGHT);
		label_7.setBounds(12, 181, 50, 21);
		panel_1.add(label_7);
		
		JLabel label_8 = new JLabel("PC (#8)");
		label_8.setHorizontalAlignment(SwingConstants.RIGHT);
		label_8.setBounds(12, 206, 50, 21);
		panel_1.add(label_8);
		
		JLabel label_9 = new JLabel("SW (#9)");
		label_9.setHorizontalAlignment(SwingConstants.RIGHT);
		label_9.setBounds(12, 231, 50, 21);
		panel_1.add(label_9);
		
		JLabel Rlabel_1 = new JLabel("Dec");
		Rlabel_1.setBounds(73, 17, 50, 15);
		panel_1.add(Rlabel_1);
		
		JLabel Rlabel_2 = new JLabel("Hex");
		Rlabel_2.setBounds(152, 17, 50, 15);
		panel_1.add(Rlabel_2);
		
		ADec = new JTextField();
		ADec.setBounds(73, 32, 72, 21);
		panel_1.add(ADec);
		ADec.setColumns(10);
		
		AHex = new JTextField();
		AHex.setColumns(10);
		AHex.setBounds(152, 32, 72, 21);
		panel_1.add(AHex);
		
		XDec = new JTextField();
		XDec.setColumns(10);
		XDec.setBounds(73, 56, 72, 21);
		panel_1.add(XDec);
		
		LDec = new JTextField();
		LDec.setColumns(10);
		LDec.setBounds(73, 81, 72, 21);
		panel_1.add(LDec);
		
		BDec = new JTextField();
		BDec.setColumns(10);
		BDec.setBounds(74, 106, 72, 21);
		panel_1.add(BDec);
		
		SDec = new JTextField();
		SDec.setColumns(10);
		SDec.setBounds(73, 131, 72, 21);
		panel_1.add(SDec);
		
		TDec = new JTextField();
		TDec.setColumns(10);
		TDec.setBounds(73, 156, 72, 21);
		panel_1.add(TDec);
		
		F = new JTextField();
		F.setColumns(10);
		F.setBounds(73, 181, 151, 21);
		panel_1.add(F);
		
		PCDec = new JTextField();
		PCDec.setColumns(10);
		PCDec.setBounds(73, 206, 72, 21);
		panel_1.add(PCDec);
		
		SW = new JTextField();
		SW.setColumns(10);
		SW.setBounds(73, 231, 151, 21);
		panel_1.add(SW);
		
		XHex = new JTextField();
		XHex.setColumns(10);
		XHex.setBounds(152, 56, 72, 21);
		panel_1.add(XHex);
		
		LHex = new JTextField();
		LHex.setColumns(10);
		LHex.setBounds(152, 81, 72, 21);
		panel_1.add(LHex);
		
		BHex = new JTextField();
		BHex.setColumns(10);
		BHex.setBounds(152, 106, 72, 21);
		panel_1.add(BHex);
		
		SHex = new JTextField();
		SHex.setColumns(10);
		SHex.setBounds(152, 131, 72, 21);
		panel_1.add(SHex);
		
		THex = new JTextField();
		THex.setColumns(10);
		THex.setBounds(152, 156, 72, 21);
		panel_1.add(THex);
		
		PCHex = new JTextField();
		PCHex.setColumns(10);
		PCHex.setBounds(152, 206, 72, 21);
		panel_1.add(PCHex);
		
		JPanel panel_1_1 = new JPanel();
		panel_1_1.setLayout(null);
		panel_1_1.setBorder(new TitledBorder(null, "E (End Record)", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1_1.setBounds(253, 41, 236, 65);
		frame.getContentPane().add(panel_1_1);
		
		JLabel label_1_1 = new JLabel("Address of First instruction");
		label_1_1.setBounds(12, 17, 212, 21);
		panel_1_1.add(label_1_1);
		
		JLabel lblNewLabel_2_1_1 = new JLabel("in Object Program : ");
		lblNewLabel_2_1_1.setBounds(12, 35, 120, 21);
		panel_1_1.add(lblNewLabel_2_1_1);
		
		EaddrT = new JTextField();
		EaddrT.setBounds(128, 35, 96, 21);
		panel_1_1.add(EaddrT);
		EaddrT.setColumns(10);
		
		JLabel lblNewLabel_1_1_1 = new JLabel("Start Address in Memory");
		lblNewLabel_1_1_1.setBounds(253, 116, 236, 21);
		frame.getContentPane().add(lblNewLabel_1_1_1);
		
		SmemT = new JTextField();
		SmemT.setBounds(359, 147, 130, 21);
		frame.getContentPane().add(SmemT);
		SmemT.setColumns(10);
		
		JLabel lblNewLabel_3 = new JLabel("Target Address : ");
		lblNewLabel_3.setBounds(253, 185, 120, 21);
		frame.getContentPane().add(lblNewLabel_3);
		
		TAT = new JTextField();
		TAT.setBounds(359, 185, 130, 21);
		frame.getContentPane().add(TAT);
		TAT.setColumns(10);
		
		JLabel lblNewLabel_4 = new JLabel("Instructions : ");
		lblNewLabel_4.setBounds(253, 216, 130, 21);
		frame.getContentPane().add(lblNewLabel_4);

		InstT = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(InstT);
		scrollPane.setBounds(253, 238, 120, 178);
		frame.getContentPane().add(scrollPane);
		
		JLabel lblNewLabel_5 = new JLabel("DEVICE ");
		lblNewLabel_5.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_5.setBounds(385, 240, 100, 21);
		frame.getContentPane().add(lblNewLabel_5);
		
		DevT = new JTextField();
		DevT.setBounds(385, 268, 100, 21);
		frame.getContentPane().add(DevT);
		DevT.setColumns(10);
		
		JButton run_1 = new JButton("RUN (1step)");
		run_1.setBounds(385, 317, 102, 23);
		frame.getContentPane().add(run_1);
		
		JButton run_all = new JButton("RUN (all)");
		run_all.setBounds(385, 350, 102, 23);
		frame.getContentPane().add(run_all);
		
		JButton exit = new JButton("EXIT");
		exit.setBounds(385, 383, 102, 23);
		frame.getContentPane().add(exit);
		
		JLabel lblNewLabel_6 = new JLabel("Log : ");
		lblNewLabel_6.setBounds(12, 429, 200, 21);
		frame.getContentPane().add(lblNewLabel_6);
		
		LogT = new JTextArea();
		JScrollPane scrollPane2 = new JScrollPane(LogT);
		scrollPane2.setBounds(12, 460, 477, 143);
		frame.getContentPane().add(scrollPane2);
		
		/* 버튼 클릭시 실행되는 액션 */
		
		/*오픈 버튼 클릭 시 */
		openbtn.addActionListener( new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent argo) {
				FileDialog f = new FileDialog(frame); // 파일 다이얼로그를 가져와서 보여주기
		        f.setVisible(true);
		        try {
		        	String FileName = f.getFile(); //file이름 가져오기
		        	txtfilename.setText(FileName); // file이름 텍스트에 출력
		        	txtfilename.setEnabled(false);
		        	File file = new File(FileName); // 파일 객체 생성
					load(file); // 로더 실행 및 시뮬레이터 로딩
					update(); // 값을 최신값으로 만들어주는 함수
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		/* onestep 버튼 클릭시, onestep과 업데이트 실행 */
		run_1.addActionListener( new ActionListener(){ 
			@Override
			public void actionPerformed(ActionEvent argo) {
		        try {
		        	oneStep();
					update();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		/* runall 버튼 클릭시, onestep과 업데이트 실행 */
		run_all.addActionListener( new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent argo) {
		        try {
		        	allStep();
					update();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		/* 종료 버튼 클릭시 사용중이던 디바이스가 종료되고 프로그램이 종료된다. */
		exit.addActionListener( new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent argo) {
				resourceManager.closeDevice();
		        System.exit(0);
			}
		});
	}
}
