// (학생 / 영양사) 역할 선택 화면 구현

import javax.swing.*;
import java.awt.*;

public class RoleSelectPanel extends JPanel {
    // 화면 전환 명령을 전달 받아 처리할 MainFrame의 주소값을 저장하는 참조 변수
    private MainFrame mainFrame;

    public RoleSelectPanel(MainFrame mainFrame) {
        // 전달 받은 MainFrame 객체 주소를 멤버 변수에 저장 (이후 버튼 클릭 이벤트에서 사용)
        this.mainFrame = mainFrame;

        // 전체 패널의 배경색을 연한 회색으로 설정
        setBackground(new Color(0xF3, 0xF4, 0xF6));

        // 부품들을 화면 정중앙 격자 형태로 배치하기 위해 GridBagLayout 적용
        setLayout(new GridBagLayout());

        // GridBagLayout에서 각 부품의 좌표, 크기합치기, 여백 등을 지정하는 설정 객체 생성
        GridBagConstraints gbc = new GridBagConstraints();


        // [컴포넌트 1] 메인 타이틀 라벨 (JLabel)
        JLabel titleLabel = new JLabel("Meal-Back 급식 피드백 시스템", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0x11, 0x18, 0x27));

        // 타이틀 라벨 격자 배치 규칙 설정
        gbc.gridx = 0; // 가로 위치: 0번째 열(왼쪽)
        gbc.gridy = 0; // 세로 위치: 0번째 행(맨 위)
        gbc.gridwidth = 2; // 아래쪽에 버튼 2개가 들어갈 수 있도록 가로로 2개 칸(열)을 합침
        gbc.insets = new Insets(0, 0, 40, 0); // 외부 여백: (위, 왼쪽, 아래 40px, 오른쪽)

        // 설정한 gbc 규칙을 적용하여 패널에 타이틀 라벨 추가
        add(titleLabel, gbc);


        // [컴포넌트 2] 학생용 접속 버튼 (JButton)
        JButton btnStudent = new JButton("학생용 접속");
        btnStudent.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        btnStudent.setBackground(new Color(0x10, 0xB9, 0x81));
        btnStudent.setForeground(Color.WHITE);
        btnStudent.setPreferredSize(new Dimension(200, 90)); // 버튼의 고정 크기 지정: 가로 200px, 세로 90px
        btnStudent.setFocusPainted(false); // 클릭 시 버튼 글자 주변에 생기는 외곽 점선 표시 제거

        // 학생용 버튼 격자 배치 규칙 설정
        gbc.gridx = 0; // 가로 위치: 0번째 열 (왼쪽)
        gbc.gridy = 1; // 세로 위치: 1번째 행 (타이틀 아래)
        gbc.gridwidth = 1; // 1개 칸만 차지하도록 복구
        gbc.insets = new Insets(0, 15, 0, 15); // 버튼 좌우에 15px씩 여백 배치

        // 버튼 클릭 이벤트 리스너 (람다식)
        // 클릭 시 MainFrame의 showScreen을 호출하여 "STUDENT" 화면으로 전환
        btnStudent.addActionListener(e -> mainFrame.showScreen("STUDENT"));

        // 설정한 gbc 규칙을 적용하여 패널에 학생용 버튼 추가
        add(btnStudent, gbc);


        // [컴포넌트 3] 영양사용 접속 버튼 (JButton)
        JButton btnNutritionist = new JButton("영양사용 접속");
        btnNutritionist.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        btnNutritionist.setBackground(new Color(0x06, 0x4E, 0x3B));
        btnNutritionist.setForeground(Color.WHITE);
        btnNutritionist.setPreferredSize(new Dimension(200, 90)); // 버튼 고정 크기 지정: 가로 200px, 세로 90px
        btnNutritionist.setFocusPainted(false); // 클릭 시 외곽 점선 표시 제거

        // 영양사용 버튼 격자 배치 규칙 설정
        gbc.gridx = 1; // 가로 위치: 1번째 열 (오른쪽)
        gbc.gridy = 1; // 세로 위치: 1번째 행 (타이틀 아래)
        gbc.gridwidth = 1; // 1개 칸 차지
        gbc.insets = new Insets(0, 15, 0, 15); // 버튼 좌우 15px 여백

        // 버튼 클릭 이벤트 리스너 (람다식)
        // 클릭 시 MainFrame의 showScreen을 호출하여 "NUTRITIONIST" 화면으로 전환
        btnNutritionist.addActionListener(e -> mainFrame.showScreen("NUTRITIONIST"));

        // 설정한 gbc 규칙을 적용하여 패널에 영양사용 버튼 추가
        add(btnNutritionist, gbc);
    }
}