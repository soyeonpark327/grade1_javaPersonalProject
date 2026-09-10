// 메인 프레임 및 CardLayout 골격 구축

import javax.swing.*;
import java.awt.*;

/*
    [Swing 핵심 요소 개념 정리]
        JPanel (종이 / 구역 패널): 화면을 여러 구역으로 나누거나 레이아웃을 따로 지정할 때 사용
        JLabel (글자 상자 / 라벨): 제목, 안내 문구, 이미지 등을 표시할 때 사용
        CardLayout (카드 겹침 레이아웃): 여러 개의 JPanel을 트럼프 카드처럼 한 위치에 겹쳐 두고, 키값을 통해 원하는 카드 하나만 겉으로 보여주는 화면 전환용 배치 규칙
        BorderLayout (5구역 배치 레이아웃): 컴포넌트를 CENTER(중앙), NORTH(북/상단), SOUTH(남/하단), EAST(동/우측), WEST(서/좌측) 5개 구역으로 나누어 배치하는 기본 정렬 방식

    [주요 단어 정리]
        Container: 다른 GUI 부품들을 담을 수 있는 틀 (예: JFrame, JPanel)
        Component: 화면을 구성하는 개별 시각적 부품 (예: JButton, JLabel, JTextField)
        JFrame: 닫기, 최소화 버튼과 상단 타이틀바가 포함된 프로그램의 가장 바깥쪽 창
        JButton: 마우스로 누를 수 있는 클릭 버
        JTextField: 아이디, 검색어 등 한 줄짜리 글자를 입력받는 칸
        JTextArea: 식단 개선 리포트, 한줄평 등 여러 줄의 긴 글을 입력하거나 보여주는 상자
        JTabbedPane: 인터넷 브라우저 탭처럼 상단/측면에 탭을 만들어 클릭할 때마다 내부 화면을 바꿔주는 컴포넌트
        FlowLayout: 가로 순차 배치, 왼쪽에서 오른쪽으로 늘어놓는 정렬 규칙
        GridLayout: 모든 칸의 크기를 동일하게 바둑판 모양으로 나눔. (예: 마일리지 숍 상품 카드 배치)
        GridBagLayout: 칸마다 크기와 비율, 여백을 세밀하게 제어할 수 있는 고급 격자 배치 방식
        Event: 사용자가 버튼을 클릭하거나 키보드를 치는 등의 행동
        Listener: 그 행동을 감시하고 있다가 이벤트가 발생했을 때 지정된 자바 코드를 실행해 주는 감지기
 */

// 최상위 창 역할을 할 MainFrame 클래스 정의
public class MainFrame extends JFrame {
    // 화면 전환을 관리할 CardLayout, 메인 패널 선언
    private CardLayout cardLayout;
    private JPanel mainContainer;

    public MainFrame() {
        // 창 기본 설정
        setTitle("MealBack - 급식 피드백 및 잔반 감축 시스템");
        setSize(900, 600); //창 가로, 세로 크기 설정
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // X 버튼 클릭 시 완전 종료
        setLocationRelativeTo(null); // 창 화면 중앙 배치

        // CardLayout 및 메인 컨테이너 패널 생성
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        RoleSelectPanel roleSelectPanel = new RoleSelectPanel(this);

        // 3개 핵심 화면 패널 생성 (골격용 임시 패널)
        StudentMainPanel studentPanel = new StudentMainPanel(this);
        NutritionistMainPanel nutritionistPanel = new NutritionistMainPanel(this);

        // CardLayout에 패널 등록 (고유 식별자 키값 부여)
        mainContainer.add(roleSelectPanel, "ROLE_SELECT");
        mainContainer.add(studentPanel, "STUDENT");
        mainContainer.add(nutritionistPanel, "NUTRITIONIST");

        // 창에 메인 컨테이너 패널 추가
        add(mainContainer);

        // 최초 실행 시 보여줄 화면 지정
        showScreen("ROLE_SELECT");
    }

    // 외부 및 내부 이벤트에서 화면 전환 시 호출하는 메소드
    public void showScreen(String screenName) { // screenName은 전환할 화면의 키값 ("ROLE_SELECT", "STUDENT", "NUTRITIONIST")
        cardLayout.show(mainContainer, screenName);
    }

    // 1단계 골격 확인용 임시 패널 생성 메소드 (테스트용)
    private JPanel createDummyPanel(String titleText, Color bgColor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bgColor); // bgColor로 패널 전체 배경색 설정

        // 중앙 안내 라벨
        JLabel label = new JLabel(titleText, SwingConstants.CENTER);
        label.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        label.setForeground(Color.WHITE);
        panel.add(label, BorderLayout.CENTER);

        // 하단 복귀 테스트용 버튼
        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton btnBack = new JButton("역할 선택 화면으로 돌아가기");

        // 버튼 클릭 시 역할 선택 화면으로 화면 전환
        btnBack.addActionListener(e -> showScreen("ROLE_SELECT"));
        btnPanel.add(btnBack);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // 메인 실행 메소드
    public static void main(String[] args) {
        // Swing GUI의 스레드 안전성(Thread-Safety)을 보장하기 위해 EDT 환경에서 실행
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true); // 창 시각화
        });
    }
}