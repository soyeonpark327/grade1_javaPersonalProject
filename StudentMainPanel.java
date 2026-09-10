// 학생용 화면 구성 (JTabbedPane 활용)

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

// 백엔드 로직 연동 및 이벤트 처리 학생용 메인 패널
public class StudentMainPanel extends JPanel {

    private MainFrame mainFrame;

    // UI 동적 바인딩 컴포넌트 참조 변수
    private JLabel lblHeaderMileage; // 상단 헤더 마일리지 표시 라벨
    private JLabel lblRankMileage; // 랭킹 탭 마일리지 표시 라벨
    private DefaultTableModel tableModel; // 평가 내역 표 모델

    public StudentMainPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());

        // 헤더 및 탭 메뉴 배치
        add(createHeaderPanel(), BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        tabbedPane.addTab("메인 대시보드", createDashboardTab());
        tabbedPane.addTab("급식 평가 작성", createEvalTab());
        tabbedPane.addTab("My 마일리지 & 랭킹", createRankTab());
        tabbedPane.addTab("마일리지 숍", createShopTab());

        add(tabbedPane, BorderLayout.CENTER);

        // 초기 데이터 바인딩 로직 호출
        refreshUserData();
    }

    // 상단 헤더 패널 (실시간 마일리지 바인딩)
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0x10, 0xB9, 0x81));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Meal-Back - 학생 모드");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);

        // 우측 정보 및 마일리지 표시 라벨
        lblHeaderMileage = new JLabel();
        lblHeaderMileage.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        lblHeaderMileage.setForeground(Color.WHITE);

        JButton btnBack = new JButton("역할 선택으로");
        btnBack.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        btnBack.addActionListener(e -> mainFrame.showScreen("ROLE_SELECT"));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(lblHeaderMileage);
        rightPanel.add(btnBack);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    // 탭 1. 메인 대시보드
    private JPanel createDashboardTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblWelcome = new JLabel("오늘의 추천 잔반 제로 메뉴: 수제 등심 돈까스 🍽️", SwingConstants.CENTER);
        lblWelcome.setFont(new Font("맑은 고딕", Font.BOLD, 18));

        panel.add(lblWelcome, BorderLayout.CENTER);
        return panel;
    }

    // 탭 2. 급식 평가 작성 탭 (이벤트 리스너 및 입력값 추출/바인딩)
    private JPanel createEvalTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. 날짜 및 메뉴 입력
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("날짜:"), gbc);
        gbc.gridx = 1;
        JTextField txtDate = new JTextField("2026-09-10", 15);
        panel.add(txtDate, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("메뉴명:"), gbc);
        gbc.gridx = 1;
        JTextField txtMenu = new JTextField("수제 등심 돈까스", 15);
        panel.add(txtMenu, gbc);

        // 2. 별점 선택 ComboBox
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("별점:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> comboScore = new JComboBox<>(new String[]{"5점 (매우 만족)", "4점 (만족)", "3점 (보통)", "2점 (아쉬움)", "1점 (불만족)"});
        panel.add(comboScore, gbc);

        // 3. 한줄평 피드백 입력 영역
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("한줄평:"), gbc);
        gbc.gridx = 1;
        JTextArea txtFeedback = new JTextArea(3, 20);
        txtFeedback.setLineWrap(true);
        panel.add(new JScrollPane(txtFeedback), gbc);

        // 4. 제출 버튼 및 이벤트 리스너 연동
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        JButton btnSubmit = new JButton("급식 평가 제출하기 (+50P 적립)");
        btnSubmit.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        btnSubmit.setBackground(new Color(0x10, 0xB9, 0x81));
        btnSubmit.setForeground(Color.WHITE);

        // [핵심] 이벤트 리스너 및 백엔드 로직 연동
        btnSubmit.addActionListener(e -> {
            String date = txtDate.getText().trim();
            String menu = txtMenu.getText().trim();
            String feedback = txtFeedback.getText().trim();
            int score = 5 - comboScore.getSelectedIndex(); // 콤보박스 인덱스로 점수 계산

            // 유효성 검사
            if (menu.isEmpty() || feedback.isEmpty()) {
                JOptionPane.showMessageDialog(this, "메뉴명과 한줄평을 입력해 주세요!", "입력 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Evaluation 객체 생성 및 DataManager 연동
            Evaluation eval = new Evaluation(date, menu, score, feedback);
            DataManager.getInstance().addEvaluation(eval);

            // 데이터 바인딩 및 UI 실시간 갱신
            refreshUserData();
            txtFeedback.setText(""); // 입력창 초기화

            JOptionPane.showMessageDialog(this, "평가가 제출되었습니다! 50 마일리지가 적립되었습니다.", "제출 완료", JOptionPane.INFORMATION_MESSAGE);
        });

        panel.add(btnSubmit, gbc);
        return panel;
    }

    // 탭 3. 마일리지 & 평가 내역 탭 (JTable 데이터 동적 바인딩)
    private JPanel createRankTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 보유 마일리지 요약 라벨
        lblRankMileage = new JLabel("", SwingConstants.LEFT);
        lblRankMileage.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        panel.add(lblRankMileage, BorderLayout.NORTH);

        // 작성 내역 JTable 모델 설정
        String[] columns = {"날짜", "메뉴명", "별점", "한줄평"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(25);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    // 탭 4. 마일리지 숍 탭 (구매 이벤트 및 마일리지 차감 로직 연동)
    private JPanel createShopTab() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 상품 카드리스트 예시
        JPanel itemCard = new JPanel(new BorderLayout(5, 5));
        itemCard.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        itemCard.setPreferredSize(new Dimension(180, 120));

        JLabel lblItem = new JLabel("매점 1,000원 쿠폰", SwingConstants.CENTER);
        JButton btnBuy = new JButton("1,000 P 교환");

        // 구매 버튼 클릭 이벤트 연동
        btnBuy.addActionListener(e -> {
            Student student = DataManager.getInstance().getCurrentStudent();
            if (student.deductMileage(1000)) {
                refreshUserData(); // UI 갱신
                JOptionPane.showMessageDialog(this, "쿠폰 교환이 완료되었습니다!", "교환 성공", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "마일리지가 부족합니다.", "잔액 부족", JOptionPane.ERROR_MESSAGE);
            }
        });

        itemCard.add(lblItem, BorderLayout.CENTER);
        itemCard.add(btnBuy, BorderLayout.SOUTH);
        panel.add(itemCard);

        return panel;
    }

    // 백엔드 데이터와 UI 컴포넌트 동기화
    private void refreshUserData() {
        Student student = DataManager.getInstance().getCurrentStudent();

        // 1. 헤더 및 마일리지 라벨 바인딩
        if (lblHeaderMileage != null) {
            lblHeaderMileage.setText(student.getName() + " | 💰 " + student.getMileage() + " P");
        }
        if (lblRankMileage != null) {
            lblRankMileage.setText("현재 누적 보유 마일리지: " + student.getMileage() + " P");
        }

        // 2. 급식 평가 JTable 데이터 새로고침
        if (tableModel != null) {
            tableModel.setRowCount(0); // 기존 표 내용 초기화
            for (Evaluation eval : DataManager.getInstance().getEvaluationList()) {
                tableModel.addRow(new Object[]{
                        eval.getDate(),
                        eval.getMenuName(),
                        eval.getScore() + " / 5",
                        eval.getFeedback()
                });
            }
        }
    }
}