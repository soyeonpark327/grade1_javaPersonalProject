// 학생용 화면 구성 (JTabbedPane 활용)

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

// 백엔드 로직 연동 및 이벤트 처리 학생용 메인 패널
public class StudentMainPanel extends JPanel {

    private MainFrame mainFrame;

    // 쿠폰의 실제 금액(원) - 화면에 "얼마짜리 쿠폰"인지 보여줄 때만 씀
    private static final int COUPON_VALUE = 1000;
    // 쿠폰 교환에 필요한 마일리지 포인트 - 실제 금액보다 비싸게 책정해서 쉽게 못 받게 함
    private static final int COUPON_PRICE = 10000;

    // UI 동적 바인딩 컴포넌트 참조 변수
    private JLabel lblHeaderMileage; // 상단 헤더 마일리지 표시 라벨
    private JLabel lblWelcome; // 메인 대시보드 추천 메뉴 표시 라벨
    private JTextArea txtBestWorst; // 메인 대시보드 베스트/워스트 TOP3 표시 영역
    private JLabel lblRankMileage; // 랭킹 탭 마일리지 표시 라벨
    private DefaultTableModel tableModel; // 평가 내역 표 모델
    private DefaultTableModel suggestionTableModel; // 메뉴 추천 내역 표 모델
    private JTextArea txtAnnouncements; // 공지사항 표시 영역
    private JTextField txtDate; // 평가 작성 탭의 날짜 입력칸 (급식 일정에서 자동 채우기용)
    private JComboBox<String> comboMenuSelect; // 평가 작성 탭의 메뉴 선택 콤보박스 (그날 등록된 메뉴 중에서만 고르게 함)
    private JTabbedPane tabbedPane; // 탭 전환용 (급식 일정에서 클릭 시 평가 작성 탭으로 이동)
    private JPanel mealScheduleContainer; // 급식 일정 목록을 담는 컨테이너 (갱신할 때 내용물을 통째로 교체)
    private JPanel calendarPanel; // 급식 일정 탭 상단의 달력 그리드 (날짜별 급식 등록 여부를 보여줌)

    public StudentMainPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());

        // 헤더 및 탭 메뉴 배치
        add(createHeaderPanel(), BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        tabbedPane.addTab("메인 대시보드", createDashboardTab());
        tabbedPane.addTab("급식 평가 작성", createEvalTab());
        tabbedPane.addTab("급식 일정", createMealScheduleViewTab());
        tabbedPane.addTab("My 마일리지 & 랭킹", createRankTab());
        tabbedPane.addTab("마일리지 숍", createShopTab());
        tabbedPane.addTab("공지사항", createAnnouncementTab());
        tabbedPane.addTab("메뉴 의견함", createSuggestionTab());

        add(tabbedPane, BorderLayout.CENTER);

        // 초기 데이터 바인딩 로직 호출
        refreshUserData();

        // CardLayout으로 이 화면이 다시 보일 때마다 최신 데이터(복원/백업 결과 등) 반영
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                refreshUserData();
            }
        });
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

        // 도움말 버튼 - 사용 방법 안내 팝업
        JButton btnHelp = new JButton("❓ 도움말");
        btnHelp.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        btnHelp.addActionListener(e -> Notice.show(this, buildStudentHelpText(), "학생 모드 사용 방법", JOptionPane.INFORMATION_MESSAGE));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(lblHeaderMileage);
        rightPanel.add(btnHelp);
        rightPanel.add(btnBack);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    // 탭 1. 메인 대시보드
    private JPanel createDashboardTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        lblWelcome = new JLabel(buildRecommendedMenuText(), SwingConstants.CENTER);
        lblWelcome.setFont(new Font("맑은 고딕", Font.BOLD, 18));

        // [추가] 베스트/워스트 TOP3 표시 영역
        txtBestWorst = new JTextArea();
        txtBestWorst.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        txtBestWorst.setEditable(false);
        txtBestWorst.setText(buildBestWorstText());

        panel.add(lblWelcome, BorderLayout.NORTH);
        panel.add(txtBestWorst, BorderLayout.CENTER);
        return panel;
    }


    // [핵심] 학생 모드 사용법 안내 문구
    private String buildStudentHelpText() {
        return "📖 Meal-Back 학생용 사용 방법\n\n"
                + "1. 메인 대시보드: 지금까지 가장 인기 있는 메뉴를 보여줍니다.\n\n"
                + "2. 급식 평가 작성: 날짜/메뉴명/메뉴 종류(주메뉴·디저트)/별점/한줄평을 입력하고,\n"
                + "   잔반을 남겼으면 체크박스를 눌러주세요. 제출하면 50P가 적립됩니다.\n\n"
                + "3. My 마일리지 & 랭킹: 내 누적 마일리지와 지금까지 작성한 평가 내역을 볼 수 있습니다.\n\n"
                + "4. 마일리지 숍: 1,000P를 모으면 매점 쿠폰으로 교환할 수 있습니다.\n\n"
                + "※ 같은 메뉴라도 여러 번 평가할 수 있어요. 솔직하게 작성해 주세요!";
    }

    // [핵심] SatisfactionComparator로 만족도 1위 메뉴를 찾아 추천 문구를 만듦 (NutritionistMainPanel과 같은 방식)
    private String buildRecommendedMenuText() {
        // DataManager.getMenus()의 "처음 평가 때 점수"가 아니라, evalManager로 "진짜 평균"을 계산
        List<Evaluation> evals = DataManager.getInstance().getEvaluationList();
        List<MenuItem> menus = new EvaluationManager().getRankedMenusBySatisfaction(evals);
        if (menus.isEmpty()) {
            return "아직 등록된 평가가 없어요. 첫 급식 평가를 남겨보세요! 🍽️";
        }

        MenuItem topMenu = menus.get(0);
        return "오늘의 추천 잔반 제로 메뉴: " + topMenu.getName() + " 🍽️";
    }

    // [핵심] SatisfactionComparator로 메뉴를 만족도 순으로 정렬해 베스트/워스트 TOP 3를 문자열로 반환
    private String buildBestWorstText() {
        // [변경] DataManager.getMenus()의 "처음 평가 때 점수"가 아니라, evalManager로 "진짜 평균"을 계산
        List<Evaluation> evals = DataManager.getInstance().getEvaluationList();
        List<MenuItem> menus = new EvaluationManager().getRankedMenusBySatisfaction(evals);

        if (menus.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("🏆 베스트 TOP 3\n");
        int bestRank = 1;
        for (MenuItem menu : menus) {
            if (bestRank > 3) break;
            sb.append(bestRank).append("위. ").append(menu.getName())
                    .append(" (").append(String.format("%.1f", menu.getScore())).append("점)\n");
            bestRank++;
        }

        sb.append("\n💔 워스트 TOP 3\n");
        int worstRank = 1;
        for (int i = menus.size() - 1; i >= 0 && worstRank <= 3; i--) {
            MenuItem menu = menus.get(i);
            sb.append(worstRank).append("위. ").append(menu.getName())
                    .append(" (").append(String.format("%.1f", menu.getScore())).append("점)\n");
            worstRank++;
        }

        return sb.toString();
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
    txtDate = new JTextField(java.time.LocalDate.now().toString(), 15); // 현실 날짜 반영
    panel.add(txtDate, gbc);

    // 날짜를 바꾼 뒤 누르면, 그 날짜에 등록된 메뉴만 아래 콤보박스에 불러옴
    gbc.gridx = 2;
    JButton btnLoadMenu = new JButton("🔄 메뉴 불러오기");
    btnLoadMenu.addActionListener(e -> refreshMenuOptionsForDate());
    panel.add(btnLoadMenu, gbc);

gbc.gridx = 0; gbc.gridy = 1;
panel.add(new JLabel("메뉴명:"), gbc);
gbc.gridx = 1;
comboMenuSelect = new JComboBox<>(); // [변경] 자유 입력 대신, 그날 등록된 메뉴 중에서만 고르게 함
panel.add(comboMenuSelect, gbc);

refreshMenuOptionsForDate(); // 탭이 열릴 때 오늘 날짜 기준으로 1회 채워줌

    
        // 2. 메뉴 종류 선택 (주메뉴 / 디저트) - MainDish, Dessert 중 무엇을 만들지 결정
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("메뉴 종류:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> comboMenuType = new JComboBox<>(new String[]{"주메뉴", "디저트"});
        panel.add(comboMenuType, gbc);

        // 3. 별점 선택 ComboBox
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("별점:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> comboScore = new JComboBox<>(new String[]{"5점 (매우 만족)", "4점 (만족)", "3점 (보통)", "2점 (아쉬움)", "1점 (불만족)"});
        panel.add(comboScore, gbc);

        // 4. 한줄평 피드백 입력 영역 
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("한줄평:"), gbc);
        gbc.gridx = 1;
        JTextArea txtFeedback = new JTextArea(3, 20);
        txtFeedback.setLineWrap(true);
        panel.add(new JScrollPane(txtFeedback), gbc);

        // 5. 잔반 여부 체크박스 - MainDish의 getScore() 감점 로직에 쓰임
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        JCheckBox chkLeftover = new JCheckBox("잔반을 남겼어요");
        panel.add(chkLeftover, gbc);

        // 6. 제출 버튼 및 이벤트 리스너 연동
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        JButton btnSubmit = new JButton("급식 평가 제출하기 (+50P 적립)");
        btnSubmit.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        btnSubmit.setBackground(new Color(0x10, 0xB9, 0x81));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setOpaque(true); // macOS에서 버튼 배경색이 무시되는 문제 방지
        btnSubmit.setBorderPainted(false);

        // [핵심] 이벤트 리스너 및 백엔드 로직 연동
        btnSubmit.addActionListener(e -> {
            // 한글 조합 중인 글자를 먼저 확정 (macOS 입력기가 조합 중에 텍스트를 지우면 창이 먹통 되는 문제 방지)
            txtFeedback.getInputContext().endComposition();
            btnSubmit.requestFocusInWindow();

            String date = txtDate.getText().trim();
            String feedback = txtFeedback.getText().trim();
            int score = 5 - comboScore.getSelectedIndex(); // 콤보박스 인덱스로 점수 계산

            // 유효성 검사 - 메뉴명은 자유 입력이 아니라 그날 등록된 메뉴 중에서 고른 값만 허용
            if (!comboMenuSelect.isEnabled() || comboMenuSelect.getSelectedItem() == null) {
                Notice.show(this, "이 날짜에 등록된 급식 메뉴가 없어요. 날짜를 확인하고 메뉴를 불러와 주세요!", "입력 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String menu = (String) comboMenuSelect.getSelectedItem();

            if (feedback.isEmpty()) {
                Notice.show(this, "한줄평을 입력해 주세요!", "입력 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }


            // 날짜 형식 검증 (yyyy-MM-dd) - 문자열 정렬이 깨지지 않도록 정규화
            try {
                date = java.time.LocalDate.parse(date).toString();
            } catch (java.time.format.DateTimeParseException ex) {
                Notice.show(this, "날짜는 2026-09-10 형식으로 입력해 주세요!", "입력 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (DataManager.getInstance().hasEvaluationFor(date, menu)) {
                Notice.show(this, "이미 같은 날짜에 같은 메뉴로 평가를 남겼어요!", "중복 제출", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 메뉴 종류에 따라 실제 자식 클래스(MainDish/Dessert)를 만듦
            // -> 이렇게 만들어두면 menuItem.getScore()를 호출할 때 다형성으로 자동 가중치가 적용됨
            boolean hasLeftover = chkLeftover.isSelected();
            MenuItem menuItem;
            if (comboMenuType.getSelectedItem().equals("디저트")) {
                menuItem = new Dessert(menu, 0, score); // 디저트는 잔반 개념이 없어 보너스만 적용
            } else {
                menuItem = new MainDish(menu, 0, score, hasLeftover); // 주메뉴는 잔반 시 감점
            }

            // Evaluation 객체 생성 및 DataManager 연동 (새로 추가한 5-인자 생성자 사용)
            Evaluation eval = new Evaluation(date, menuItem, score, feedback, hasLeftover);
            DataManager.getInstance().addEvaluation(eval);

            // 데이터 바인딩 및 UI 실시간 갱신
            refreshUserData();
            SwingUtilities.invokeLater(() -> {
                txtFeedback.setText(""); // 입력창 초기화 (입력 확정 이후 실행)
            });
            chkLeftover.setSelected(false); // 체크박스도 다음 입력을 위해 초기화
            comboMenuType.setSelectedIndex(0); // 메뉴 종류도 기본값(주메뉴)으로 초기화
            refreshMenuOptionsForDate(); // 콤보박스도 그날 메뉴로 다시 채움 (선택값 초기화)



            // 팝업이 떠 있는 동안 버튼을 눌러도 중복 제출이 안 되도록 잠깐 비활성화
            // -> 팝업이 닫히면(onClose) 다시 눌러도 되는 상태로 복구
            btnSubmit.setEnabled(false);
            Notice.show(this, "평가가 제출되었습니다! 50 마일리지가 적립되었습니다.", "제출 완료",
                    JOptionPane.INFORMATION_MESSAGE, () -> btnSubmit.setEnabled(true));

        });

        panel.add(btnSubmit, gbc);
        return panel;
    }

    // 평가 작성 탭의 날짜 입력칸 값을 기준으로, 그날 영양사가 등록한 급식 메뉴만 콤보박스에 채워 넣음
    // (메뉴명을 자유롭게 적게 하면 실제로 나오지 않은 메뉴를 평가하는 일이 생길 수 있어서, 등록된 일정 안에서만 고르게 함)
    private void refreshMenuOptionsForDate() {
        if (comboMenuSelect == null || txtDate == null) return;

        String date = txtDate.getText().trim();
        comboMenuSelect.removeAllItems();

        java.util.Set<String> menuNames = new java.util.LinkedHashSet<>(); // 순서 유지 + 중복 메뉴명 제거
        for (MealSlot slot : DataManager.getInstance().getMealSlots()) {
            if (slot.getDate().equals(date)) {
                menuNames.addAll(slot.getMenuNames());
            }
        }

        if (menuNames.isEmpty()) {
            comboMenuSelect.addItem("(이 날짜엔 등록된 급식 일정이 없어요)");
            comboMenuSelect.setEnabled(false);
        } else {
            for (String name : menuNames) {
                comboMenuSelect.addItem(name);
            }
            comboMenuSelect.setEnabled(true);
        }
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

        JLabel lblItem = new JLabel("매점 " + COUPON_VALUE + "원 쿠폰", SwingConstants.CENTER);
        JButton btnBuy = new JButton(COUPON_PRICE + " P 교환");

        // 구매 버튼 클릭 이벤트 연동
        btnBuy.addActionListener(e -> {
            // 팝업이 떠 있는 동안 버튼을 눌러도 중복 차감이 안 되도록 잠깐 비활성화
            btnBuy.setEnabled(false);

            Student student = DataManager.getInstance().getCurrentStudent();

            // [추가] 마일리지가 부족하면 전화번호를 물어볼 필요 없이 바로 안내
            if (student.getMileage() < COUPON_PRICE) {
                Notice.show(this, "마일리지가 부족합니다.", "잔액 부족",
                        JOptionPane.ERROR_MESSAGE, () -> btnBuy.setEnabled(true));
                return;
            }

            // 쿠폰을 받을 전화번호 입력받기
            String phone = JOptionPane.showInputDialog(this, "쿠폰을 받으실 전화번호를 입력해 주세요:", "쿠폰 수령 정보", JOptionPane.PLAIN_MESSAGE);
            if (phone == null || phone.trim().isEmpty()) {
                // 취소했거나 빈 값이면 교환 자체를 취소 (마일리지 차감 안 됨)
                btnBuy.setEnabled(true);
                return;
            }

            if (student.deductMileage(COUPON_PRICE)) {
                refreshUserData(); // UI 갱신
                Notice.show(this, phone.trim() + "번으로 쿠폰을 보내드렸습니다!", "교환 성공",
                        JOptionPane.INFORMATION_MESSAGE, () -> btnBuy.setEnabled(true));
            } else {
                // 위에서 이미 잔액을 확인했지만, 혹시 모를 상황을 대비해 안전하게 남겨둠
                Notice.show(this, "마일리지가 부족합니다.", "잔액 부족",
                        JOptionPane.ERROR_MESSAGE, () -> btnBuy.setEnabled(true));
            }
        });


        itemCard.add(lblItem, BorderLayout.CENTER);
        itemCard.add(btnBuy, BorderLayout.SOUTH);
        panel.add(itemCard);

        return panel;
    }


    // 탭 5. 메뉴 의견함 (학생이 급식 메뉴를 제안하고, 그 메뉴가 이번 달 1위를 하면 보상을 받는 기능)
    private JPanel createSuggestionTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. 상단 추천 입력 폼
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("추천 메뉴명:"), gbc);
        gbc.gridx = 1;
        JTextField txtSuggestMenu = new JTextField(15);
        formPanel.add(txtSuggestMenu, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("추천 이유:"), gbc);
        gbc.gridx = 1;
        JTextField txtSuggestReason = new JTextField(15);
        formPanel.add(txtSuggestReason, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JButton btnSuggest = new JButton("메뉴 추천하기");
        btnSuggest.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        btnSuggest.setBackground(new Color(0x10, 0xB9, 0x81));
        btnSuggest.setForeground(Color.WHITE);
        btnSuggest.setOpaque(true); // macOS에서 버튼 배경색이 무시되는 문제 방지
        btnSuggest.setBorderPainted(false);

        btnSuggest.addActionListener(e -> {
            // 한글 조합 중인 글자를 먼저 확정 (macOS 입력기가 조합 중에 텍스트를 지우면 창이 먹통 되는 문제 방지)
            txtSuggestMenu.getInputContext().endComposition();
            txtSuggestReason.getInputContext().endComposition();
            btnSuggest.requestFocusInWindow();

            String menuName = txtSuggestMenu.getText().trim();
            String reason = txtSuggestReason.getText().trim();

            if (menuName.isEmpty()) {
                Notice.show(this, "추천할 메뉴명을 입력해 주세요!", "입력 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Student student = DataManager.getInstance().getCurrentStudent();
            String date = java.time.LocalDate.now().toString();
            MenuSuggestion suggestion = new MenuSuggestion(menuName, reason, student, date);
            DataManager.getInstance().addSuggestion(suggestion);

            refreshSuggestionTable();
            SwingUtilities.invokeLater(() -> {
                txtSuggestMenu.setText("");
                txtSuggestReason.setText("");
            });

            btnSuggest.setEnabled(false);
            Notice.show(this, "메뉴 추천이 등록되었습니다! 이 메뉴가 이번 달 1위를 하면 보너스를 받을 수 있어요.", "추천 완료",
                    JOptionPane.INFORMATION_MESSAGE, () -> btnSuggest.setEnabled(true));
        });

        formPanel.add(btnSuggest, gbc);

        // 2. 하단 내 추천 내역 표
        String[] columns = {"날짜", "추천 메뉴", "추천 이유", "보상 여부"};
        suggestionTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(suggestionTableModel);
        table.setRowHeight(25);

        refreshSuggestionTable(); // 초기 1회 채워줌

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    // [핵심] 메뉴 추천 내역 표를 최신 상태로 다시 그림 (보상 지급 여부 등 반영)
    private void refreshSuggestionTable() {
        if (suggestionTableModel == null) return;
        suggestionTableModel.setRowCount(0);
        for (MenuSuggestion s : DataManager.getInstance().getSuggestions()) {
            suggestionTableModel.addRow(new Object[]{
                    s.getDate(),
                    s.getMenuName(),
                    s.getReason(),
                    s.isRewarded() ? "🎁 보상 받음" : "대기 중"
            });
        }
    }


    // 탭. 공지사항 (영양사가 올린 글을 읽기 전용으로 보여줌)
    private JPanel createAnnouncementTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        txtAnnouncements = new JTextArea();
        txtAnnouncements.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        txtAnnouncements.setEditable(false);
        txtAnnouncements.setLineWrap(true);
        txtAnnouncements.setWrapStyleWord(true);

        refreshAnnouncements(); // 초기 1회 채워줌

        panel.add(new JScrollPane(txtAnnouncements), BorderLayout.CENTER);
        return panel;
    }

    // [핵심] 영양사가 등록한 공지사항을 최신순으로 다시 그림
    private void refreshAnnouncements() {
        if (txtAnnouncements == null) return;
        txtAnnouncements.setText("");
        List<Announcement> announcements = DataManager.getInstance().getAnnouncements();
        if (announcements.isEmpty()) {
            txtAnnouncements.setText("등록된 공지사항이 없습니다.");
            return;
        }
        for (int i = announcements.size() - 1; i >= 0; i--) { // 최신 글이 위에 오게 거꾸로 순회
            Announcement a = announcements.get(i);
            txtAnnouncements.append("📢 [" + a.getDate() + "] " + a.getTitle() + "\n" + a.getContent() + "\n\n");
        }
    }

    // 탭. 급식 일정 (영양사가 등록한 일정을 달력으로 보고, 날짜를 클릭하면 그날 메뉴가 아래에 뜸)
    private JPanel createMealScheduleViewTab() {
        JPanel outer = new JPanel(new BorderLayout(10, 10));
        outer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        calendarPanel = new JPanel(new GridLayout(0, 7, 5, 5)); // 7칸(요일) x 필요한 만큼 줄
        buildCalendarGrid();

        mealScheduleContainer = new JPanel();
        mealScheduleContainer.setLayout(new BoxLayout(mealScheduleContainer, BoxLayout.Y_AXIS));
        mealScheduleContainer.setBorder(BorderFactory.createTitledBorder("날짜를 클릭하면 그날 급식 메뉴가 여기 나와요"));

        outer.add(calendarPanel, BorderLayout.NORTH);
        outer.add(new JScrollPane(mealScheduleContainer), BorderLayout.CENTER);
        return outer;
    }

    // [핵심] 이번 달 달력 격자를 새로 그림 - 급식이 등록된 날짜는 색을 다르게 표시
    private void buildCalendarGrid() {
        if (calendarPanel == null) return;
        calendarPanel.removeAll();

        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate firstOfMonth = today.withDayOfMonth(1);
        int daysInMonth = firstOfMonth.lengthOfMonth();
        int startWeekday = firstOfMonth.getDayOfWeek().getValue() % 7; // 월=1...일=7 -> 일요일이 0번째 칸에 오도록 보정

        String[] dayNames = {"일", "월", "화", "수", "목", "금", "토"};
        for (String d : dayNames) {
            JLabel lbl = new JLabel(d, SwingConstants.CENTER);
            lbl.setFont(new Font("맑은 고딕", Font.BOLD, 13));
            calendarPanel.add(lbl);
        }

        for (int i = 0; i < startWeekday; i++) {
            calendarPanel.add(new JLabel("")); // 1일 이전의 빈 칸
        }

        List<MealSlot> slots = DataManager.getInstance().getMealSlots();

        for (int day = 1; day <= daysInMonth; day++) {
            java.time.LocalDate date = firstOfMonth.withDayOfMonth(day);
            String dateStr = date.toString();

            boolean hasSchedule = false;
            for (MealSlot slot : slots) {
                if (slot.getDate().equals(dateStr)) {
                    hasSchedule = true;
                    break;
                }
            }

            JButton btnDay = new JButton(hasSchedule ? (day + " 🍚") : String.valueOf(day));
            btnDay.setFocusPainted(false);
            btnDay.setOpaque(true);
            btnDay.setBorderPainted(false);

            if (date.equals(today)) {
                btnDay.setBackground(new Color(0x10, 0xB9, 0x81)); // 오늘 날짜는 초록색 강조
                btnDay.setForeground(Color.WHITE);
            } else if (hasSchedule) {
                btnDay.setBackground(new Color(0xEC, 0xFD, 0xF5)); // 급식 등록된 날은 연한 초록색
            } else {
                btnDay.setBackground(Color.WHITE);
            }

            btnDay.addActionListener(e -> showMealsForDate(dateStr));
            calendarPanel.add(btnDay);
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    // [핵심] 달력에서 클릭한 날짜의 급식 메뉴 목록을 아래 영역에 보여줌
    private void showMealsForDate(String dateStr) {
        if (mealScheduleContainer == null) return;
        mealScheduleContainer.removeAll();
        mealScheduleContainer.setBorder(BorderFactory.createTitledBorder(dateStr + " 급식"));

        boolean found = false;
        for (MealSlot slot : DataManager.getInstance().getMealSlots()) {
            if (!slot.getDate().equals(dateStr)) continue;
            found = true;

            JPanel slotPanel = new JPanel(new BorderLayout(10, 5));
            slotPanel.setBorder(BorderFactory.createTitledBorder(slot.getMealType()));
            slotPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            slotPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

            JPanel menuButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            for (String menuName : slot.getMenuNames()) {
                JButton btnEvaluate = new JButton(menuName + " 평가하기");
                btnEvaluate.addActionListener(e -> {
                    // [핵심] 날짜를 채우고 그 날짜의 메뉴 목록을 콤보박스에 불러온 뒤, 클릭한 메뉴를 선택 상태로 만듦
                    txtDate.setText(slot.getDate());
                    refreshMenuOptionsForDate();
                    comboMenuSelect.setSelectedItem(menuName);
                    tabbedPane.setSelectedIndex(1); // 급식 평가 작성 탭 (addTab한 순서상 두 번째, 인덱스 1)
                });
                menuButtonsPanel.add(btnEvaluate);
            }

            slotPanel.add(menuButtonsPanel, BorderLayout.CENTER);
            mealScheduleContainer.add(slotPanel);
            mealScheduleContainer.add(Box.createVerticalStrut(8));
        }

        if (!found) {
            JLabel lblEmpty = new JLabel("이 날짜엔 등록된 급식 일정이 없습니다.");
            lblEmpty.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
            mealScheduleContainer.add(lblEmpty);
        }

        mealScheduleContainer.revalidate();
        mealScheduleContainer.repaint();
    }

    // [핵심] 급식 일정 데이터가 바뀌었을 때(새 일정 등록 등) 달력을 최신 상태로 다시 그림
    private void refreshMealScheduleView() {
        buildCalendarGrid();
    }


    // 백엔드 데이터와 UI 컴포넌트 동기화
    private void refreshUserData() {
        Student student = DataManager.getInstance().getCurrentStudent();

        // 대시보드 추천 메뉴도 최신 데이터로 갱신
        if (lblWelcome != null) {
            lblWelcome.setText(buildRecommendedMenuText());
            if (txtBestWorst != null) {
                txtBestWorst.setText(buildBestWorstText());
            }

        }

        // 1. 헤더 및 마일리지 라벨 바인딩
        if (lblHeaderMileage != null) {
            lblHeaderMileage.setText(student.getName() + " | 🏅 " + student.getGrade() + " (" + student.getReviewCount() + "회) | 💰 " + student.getMileage() + " P");
        }
        if (lblRankMileage != null) {
            lblRankMileage.setText("현재 누적 보유 마일리지: " + student.getMileage() + " P");
        }

        refreshSuggestionTable(); // 메뉴 추천 내역도 최신 상태로 갱신
        refreshAnnouncements(); // 공지사항도 최신 상태로 갱신
        refreshMealScheduleView(); // [추가] 급식 일정도 최신 상태로 갱신

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