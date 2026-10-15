// 영양사용 화면 구성 (JTabbedPane 활용 및 예외 처리/유효성 검증 적용)

// Swing GUI 구성요소 및 테두리/표 관련 클래스
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

// AWT GUI 레이아웃 및 색상 관련 클래스
import java.awt.*;

// 입출력(I/O) 및 직렬화/역직렬화 파일 처리 관련 클래스
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

// 컬렉션 프레임워크 (자바 기본 List 인터페이스 사용)
import java.util.ArrayList;
import java.util.List;

// [영양사/관리자용 메인 화면 패널]
// JTabbedPane을 활용하여 대시보드, 잔반 통계, 식단 리포트 탭 제공
// 예외 처리(0으로 나누기 방지), instanceof 타입 검증(역직렬화 안전성), UI 레이아웃 깨짐 방지 적용
// -> 실제 통계/리포트 "계산"은 NutritionistStatsBuilder가 담당하고, 이 클래스는 화면 구성과 이벤트 처리만 맡음
public class NutritionistMainPanel extends JPanel {

    // 화면 전환 제어 및 메인 프레임 통신을 위한 참조 변수
    private MainFrame mainFrame;

    // [추가] 통계/리포트 계산 전담 클래스 (책임 분리)
    private NutritionistStatsBuilder statsBuilder = new NutritionistStatsBuilder();

    // 잔반 통계 JTable 데이터를 동적으로 관리하고 갱신하기 위한 데이터 모델
    private DefaultTableModel statsTableModel;

    // 현재 선택된 정렬 기준 (탭 이동/새 데이터 반영 시 같은 기준으로 다시 표시)
    private String currentSort = "DATE_DESC";

    // 날짜 필터 (둘 다 null이면 "전체 기간" 상태)
    private String filterStartDate = null;
    private String filterEndDate = null;

    // 식단 리포트 텍스트를 실시간으로 업데이트하기 위한 JTextArea 참조 변수
    private JTextArea txtReport;

    // 메뉴별 만족도 랭킹을 실시간으로 업데이트하기 위한 JTextArea 참조 변수
    private JTextArea txtRanking;

    // 대시보드 카드1(잔반율)을 실시간으로 업데이트하기 위한 JLabel 참조 변수
    private JLabel lblRoi;

    // 실시간 위험 경고 로그를 실시간으로 업데이트하기 위한 JTextArea 참조 변수
    private JTextArea txtWarning;

    // 월간 차트를 넣을 컨테이너 (새로고침할 때 내용물을 통째로 교체)
    private JPanel monthlyChartContainer;
    private DefaultTableModel studentActivityTableModel; // 학생 활동 현황 표 모델

    // NutritionistMainPanel 생성자
    public NutritionistMainPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        // 전체 패널 레이아웃 설정: 상단(NORTH)에 헤더, 중앙(CENTER)에 탭 영역 배치
        setLayout(new BorderLayout());

        // 1. 상단 포레스트 그린 헤더 패널 생성 및 배치 (백업/복원 버튼 포함)
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. 중앙에 배치할 서브메뉴용 JTabbedPane(탭 패널) 생성
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("맑은 고딕", Font.BOLD, 14)); // 탭 글꼴 설정

        // 3. 각 관리자 전용 서브 탭 화면 생성 및 추가
        tabbedPane.addTab("관리자 대시보드", createDashboardTab());
        tabbedPane.addTab("잔반 통계 & 랭킹 분석", createStatsTab());
        tabbedPane.addTab("월간 통계 (도표)", createMonthlyChartTab());
        tabbedPane.addTab("식단 개선 리포트", createReportTab());
        tabbedPane.addTab("공지사항 작성", createAnnouncementTab());
        tabbedPane.addTab("급식 일정 등록", createMealScheduleTab());
        tabbedPane.addTab("학생 활동 현황", createStudentActivityTab());

        // 메인 패널 중앙에 탭 패널 배치
        add(tabbedPane, BorderLayout.CENTER);

        // 탭을 이동할 때마다 학생 화면에서 새로 들어온 평가를 표/리포트에 반영
        tabbedPane.addChangeListener(e -> refreshAllViews());

        // CardLayout으로 이 화면이 다시 보일 때도 최신 데이터 반영
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                refreshAllViews();
            }
        });
    }

    // 통계 표와 리포트를 최신 DataManager 데이터로 다시 그림
    private void refreshAllViews() {
        refreshStatsTable(statsBuilder.getFilteredAndSortedEvaluations(currentSort, filterStartDate, filterEndDate));
        if (txtReport != null) {
            txtReport.setText(statsBuilder.buildRealtimeReportText());
        }
        if (txtRanking != null) {
            txtRanking.setText(statsBuilder.buildTopMenuRankingText());
        }
        if (lblRoi != null) {
            lblRoi.setText(statsBuilder.buildAverageWasteRateText());
        }
        if (txtWarning != null) {
            txtWarning.setText(statsBuilder.buildWarningLogText());
        }
        refreshStudentActivityTable(); // 학생 활동 현황도 최신 데이터로 갱신
        refreshMonthlyChart(); // 월간 차트도 최신 데이터로 갱신
    }

    // 상단 헤더 패널 생성 메서드 (영양사 모드 타이틀, 데이터 백업/복원 버튼, 역할 선택 화면 복귀 버튼 포함)
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());

        // 영양사 시그니처 테마 색상 (딥 포레스트 그린: #064E3B)
        headerPanel.setBackground(new Color(0x06, 0x4E, 0x3B));

        // 내부 여백 설정 (상, 좌, 하, 우)
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // 좌측 앱 타이틀 라벨 설정
        JLabel titleLabel = new JLabel("Meal-Back - 영양사/관리자 모드");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);

        // 우측 버튼 모음 패널 (백업 버튼 + 복원 버튼 + 화면 전환 버튼)
        JPanel rightBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBtnPanel.setOpaque(false); // 배경을 투명하게 만들어 헤더 색상이 비치도록 설정

        // 1) 전체 데이터 직렬화 백업 (.dat) 버튼
        JButton btnBackup = new JButton("💾 데이터 백업 (.dat)");
        btnBackup.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btnBackup.setBackground(new Color(0x04, 0x78, 0x57)); // 밝은 그린
        btnBackup.setForeground(Color.WHITE);
        btnBackup.setOpaque(true); // macOS에서 버튼 배경색이 무시되는 문제 방지
        btnBackup.setBorderPainted(false);
        btnBackup.setFocusPainted(false); // 버튼 선택 시 테두리 점선 제거
        btnBackup.addActionListener(e -> saveBackupData());

        // 2) [4주차 추가] 데이터 불러오기/복원 (.dat) 버튼 (instanceof 타입 검증 연결)
        JButton btnLoad = new JButton("📂 데이터 불러오기");
        btnLoad.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btnLoad.setBackground(new Color(0x0F, 0x76, 0x6E)); // 딥 틸 색상
        btnLoad.setForeground(Color.WHITE);
        btnLoad.setOpaque(true); // macOS에서 버튼 배경색이 무시되는 문제 방지
        btnLoad.setBorderPainted(false);
        btnLoad.setFocusPainted(false);
        btnLoad.addActionListener(e -> loadBackupData());

        // 도움말 버튼 - 사용 방법 안내 팝업
        JButton btnHelp = new JButton("❓ 도움말");
        btnHelp.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btnHelp.setBackground(Color.WHITE);
        btnHelp.setForeground(new Color(0x06, 0x4E, 0x3B));
        btnHelp.setOpaque(true);
        btnHelp.setBorderPainted(false);
        btnHelp.setFocusPainted(false);
        btnHelp.addActionListener(e -> Notice.show(this, buildNutritionistHelpText(), "영양사 모드 사용 방법", JOptionPane.INFORMATION_MESSAGE));

        // 3) 역할 선택 초기 화면으로 복귀하는 버튼
        JButton btnBack = new JButton("역할 선택으로");
        btnBack.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btnBack.setBackground(Color.WHITE);
        btnBack.setForeground(new Color(0x06, 0x4E, 0x3B));
        btnBack.setOpaque(true); // macOS에서 버튼 배경색이 무시되는 문제 방지
        btnBack.setBorderPainted(false);
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> mainFrame.showScreen("ROLE_SELECT"));

        // 우측 패널에 버튼들 추가
        rightBtnPanel.add(btnBackup);
        rightBtnPanel.add(btnLoad);
        rightBtnPanel.add(btnHelp);
        rightBtnPanel.add(btnBack);

        // 헤더 패널 좌/우에 요소 배치
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(rightBtnPanel, BorderLayout.EAST);

        return headerPanel;
    }

    // [핵심] 영양사 모드 사용법 안내 문구
    private String buildNutritionistHelpText() {
        return "📖 Meal-Back 영양사용 사용 방법\n\n"
                + "1. 관리자 대시보드: 전체 평균 잔반율, 메뉴별 만족도 TOP3, 위험 메뉴 경고를 한눈에 봐요.\n\n"
                + "2. 잔반 통계 & 랭킹 분석: 버튼으로 잔반율/만족도/날짜 기준 정렬할 수 있어요.\n\n"
                + "3. 식단 개선 리포트: 위험 메뉴(잔반율 30% 초과)를 자동으로 분석해요. .txt로 저장도 가능해요.\n\n"
                + "4. 데이터 백업 (.dat): 학생/메뉴/평가 전체를 파일로 저장해요.\n"
                + "   데이터 불러오기: 저장했던 .dat 파일로 복원해요.";
    }

    // DataManager의 평가 데이터를 .dat 파일로 직렬화(Serialization)하여 저장
    private void saveBackupData() {
        // 파일 저장 경로를 선택할 수 있는 Swing 탐색기 창 생성
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("백업 파일 저장 경로 선택");
        fileChooser.setSelectedFile(new File("MealBack_Data_Backup.dat")); // 기본 파일명 지정

        // 저장 다이얼로그 출력 후 사용자의 선택 결과 저장
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            // DataManager.saveData()로 학생, 메뉴, 평가 목록을 순서대로 직렬화하여 저장
            boolean success = DataManager.getInstance().saveData(fileToSave.getAbsolutePath());
            if (success) {
                Notice.show(this,
                        "전체 데이터(학생/메뉴/평가/추천/공지/일정) 백업이 완료되었습니다!\n경로: " + fileToSave.getAbsolutePath(),
                        "백업 완료", JOptionPane.INFORMATION_MESSAGE);
            } else {
                Notice.show(this,
                        "백업 중 오류가 발생했습니다. 저장 경로와 권한을 확인해 주세요.",
                        "백업 실패", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // [핵심] .dat 파일 역직렬화(Deserialization) 및 다운캐스팅 타입 검증(instanceof)
    // 올바르지 않은 타입의 객체를 읽어왔을 때 ClassCastException 예외가 발생하는 것 방지
    private void loadBackupData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("복원할 백업 파일 선택 (.dat)");

        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToLoad = fileChooser.getSelectedFile();

            // ObjectInputStream을 통한 역직렬화 파일 읽기 (saveData()가 쓴 순서대로 읽어야 함)
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileToLoad))) {
                Object studentObj = ois.readObject();
                Object menuObj = ois.readObject();
                Object evalObj = ois.readObject();
                Object suggestionObj = ois.readObject();
                Object announcementObj = ois.readObject();
                Object mealSlotObj = ois.readObject();

                // [유효성 검증] 6개 객체가 각각 올바른 요소 타입의 List인지 instanceof로 확인
                List<Student> students = castList(studentObj, Student.class);
                List<MenuItem> menus = castList(menuObj, MenuItem.class);
                List<Evaluation> evaluations = castList(evalObj, Evaluation.class);
                List<MenuSuggestion> suggestions = castList(suggestionObj, MenuSuggestion.class);
                List<Announcement> announcements = castList(announcementObj, Announcement.class);
                List<MealSlot> mealSlots = castList(mealSlotObj, MealSlot.class);

                if (students != null && menus != null && evaluations != null
                        && suggestions != null && announcements != null && mealSlots != null) {
                    DataManager.getInstance().replaceAllData(students, menus, evaluations, suggestions, announcements, mealSlots);
                    refreshAllViews();

                    Notice.show(this,
                            "데이터가 성공적으로 복원되었습니다! (학생 " + students.size()
                                    + "명, 메뉴 " + menus.size() + "개, 평가 " + evaluations.size() + "건)",
                            "복원 완료", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    Notice.show(this,
                            "올바른 Meal-Back 백업 파일(.dat) 형식이 아니거나 내부에 잘못된 데이터가 포함되어 있습니다.",
                            "검증 실패", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (ClassNotFoundException ex) {
                Notice.show(this,
                        "클래스 정보를 찾을 수 없습니다: " + ex.getMessage(),
                        "복원 오류", JOptionPane.ERROR_MESSAGE);
                } catch (IOException ex) {
                Notice.show(this,
                        "파일을 읽는 중 오류가 발생했습니다: " + ex.getMessage(),
                        "입출력 오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // obj가 List이고 모든 요소가 type 인스턴스일 때만 안전하게 형변환 (아니면 null)
    private <T> List<T> castList(Object obj, Class<T> type) {
        if (!(obj instanceof List<?>)) {
            return null;
        }
        List<T> result = new ArrayList<>();
        for (Object item : (List<?>) obj) {
            if (!type.isInstance(item)) {
                return null;
            }
            result.add(type.cast(item));
        }
        return result;
    }

    // 탭 1. 관리자 대시보드 탭 생성 (ROI/예산 절감 요약 카드 위젯 및 실시간 위험 경고 로그 창으로 구성)
    private JPanel createDashboardTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        // 1. 상단 위젯 영역 (1행 2열 격자 레이아웃)
        JPanel widgetPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        widgetPanel.setOpaque(false);

        // 위젯 카드 1: 전체 평균 추정 잔반율 (실데이터 기반)
        JPanel card1 = new JPanel(new BorderLayout());
        card1.setBackground(new Color(0xF0, 0xFD, 0xF4));
        card1.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0x06, 0x4E, 0x3B), 2),
                "전체 평균 추정 잔반율", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("맑은 고딕", Font.BOLD, 14), new Color(0x06, 0x4E, 0x3B)));

        lblRoi = new JLabel(statsBuilder.buildAverageWasteRateText(), SwingConstants.CENTER);
        lblRoi.setFont(new Font("맑은 고딕", Font.BOLD, 32));
        lblRoi.setForeground(new Color(0x04, 0x78, 0x57));
        card1.add(lblRoi, BorderLayout.CENTER);

        // 위젯 카드 2: 메뉴별 만족도 랭킹 (SatisfactionComparator로 실제 데이터 정렬)
        JPanel card2 = new JPanel(new BorderLayout());
        card2.setBackground(new Color(0xEC, 0xFD, 0xF5));
        card2.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0x06, 0x4E, 0x3B), 2),
                "메뉴별 만족도 베스트&워스트 TOP 3", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("맑은 고딕", Font.BOLD, 14), new Color(0x06, 0x4E, 0x3B)));

        txtRanking = new JTextArea();
        txtRanking.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        txtRanking.setEditable(false);
        txtRanking.setOpaque(false); // 카드 배경색이 그대로 비치도록 설정
        txtRanking.setText(statsBuilder.buildTopMenuRankingText());
        card2.add(txtRanking, BorderLayout.CENTER);

        widgetPanel.add(card1);
        widgetPanel.add(card2);

        // 2. 하단 실시간 위험 경고 영역 (JTextArea)
        JPanel warningPanel = new JPanel(new BorderLayout());
        warningPanel.setBackground(Color.WHITE);
        warningPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.RED, 1),
                "🚨 실시간 잔반 위험 경고 & 이상 징후 알림", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("맑은 고딕", Font.BOLD, 14), Color.RED));

        txtWarning = new JTextArea();
        txtWarning.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        txtWarning.setEditable(false); // 읽기 전용

        // [UI 깨짐 방지] 긴 문장 출력 시 창이 옆으로 늘어나 레이아웃이 깨지지 않도록 자동 줄바꿈 설정
        txtWarning.setLineWrap(true); // 화면 끝에 도달하면 다음 줄로 자동 넘김
        txtWarning.setWrapStyleWord(true); // 단어 단위로 깔끔하게 줄바꿈

        // 실제 위험 메뉴 데이터로 경고 로그 바인딩
        txtWarning.setText(statsBuilder.buildWarningLogText());

        JScrollPane scrollWarning = new JScrollPane(txtWarning);
        warningPanel.add(scrollWarning, BorderLayout.CENTER);

        panel.add(widgetPanel, BorderLayout.NORTH);
        panel.add(warningPanel, BorderLayout.CENTER);

        return panel;
    }

    // 탭 2. 잔반 통계 & 랭킹 분석 탭 생성 (정렬 기준 버튼 3개 및 DataManager 연동 JTable 표로 구성)
    private JPanel createStatsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        // 1. 상단 정렬 조작 패널
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(Color.WHITE);

        JLabel lblFilter = new JLabel("정렬 기준:");
        lblFilter.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        JButton btnSortWaste = new JButton("잔반율 높은 순");
        JButton btnSortRating = new JButton("만족도 높은 순");
        JButton btnSortDate = new JButton("최신 날짜 순");
        JButton btnSortSingleDay = new JButton("특정 날짜만 보기"); // 하루치 평가만 모아보는 정렬

        btnSortWaste.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        btnSortRating.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        btnSortDate.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        btnSortSingleDay.setFont(new Font("맑은 고딕", Font.PLAIN, 12));

        filterPanel.add(lblFilter);
        filterPanel.add(btnSortWaste);
        filterPanel.add(btnSortRating);
        filterPanel.add(btnSortDate);
        filterPanel.add(btnSortSingleDay);

        // 날짜 범위 필터 UI (두 번째 줄)
        JPanel dateFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        dateFilterPanel.setBackground(Color.WHITE);

        JLabel lblDateRange = new JLabel("기간 조회:");
        lblDateRange.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        JTextField txtStartDate = new JTextField(10);
        JTextField txtEndDate = new JTextField(10);
        JButton btnApplyDateFilter = new JButton("조회");
        JButton btnClearDateFilter = new JButton("전체보기");

        btnApplyDateFilter.addActionListener(e -> {
            String start = txtStartDate.getText().trim();
            String end = txtEndDate.getText().trim();

            // 입력값 검증 (yyyy-MM-dd 형식인지)
            try {
                if (!start.isEmpty()) java.time.LocalDate.parse(start);
                if (!end.isEmpty()) java.time.LocalDate.parse(end);
            } catch (java.time.format.DateTimeParseException ex) {
                Notice.show(this, "날짜는 2026-10-01 형식으로 입력해 주세요!", "입력 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }

            filterStartDate = start.isEmpty() ? null : start;
            filterEndDate = end.isEmpty() ? null : end;
            refreshStatsTable(statsBuilder.getFilteredAndSortedEvaluations(currentSort, filterStartDate, filterEndDate));
        });

        btnClearDateFilter.addActionListener(e -> {
            filterStartDate = null;
            filterEndDate = null;
            txtStartDate.setText("");
            txtEndDate.setText("");
            refreshStatsTable(statsBuilder.getFilteredAndSortedEvaluations(currentSort, filterStartDate, filterEndDate));
        });

        dateFilterPanel.add(lblDateRange);
        dateFilterPanel.add(new JLabel("시작일"));
        dateFilterPanel.add(txtStartDate);
        dateFilterPanel.add(new JLabel("종료일"));
        dateFilterPanel.add(txtEndDate);
        dateFilterPanel.add(btnApplyDateFilter);
        dateFilterPanel.add(btnClearDateFilter);

        // 2. 표(JTable) 데이터 모델 생성 (수정 불가능하도록 익명 클래스 오버라이딩)
        String[] columnNames = {"날짜", "주 메뉴", "예상 잔반율 (%)", "평균 별점", "이 메뉴 총 후기", "이 메뉴 잔반 후기", "학생 한줄평 피드백"};
        statsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 셀 수정 불가능 설정
            }
        };

        JTable table = new JTable(statsTableModel);
        table.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(0xF3, 0xF4, 0xF6));

        // 각 정렬 버튼 클릭 이벤트 연동
        btnSortWaste.addActionListener(e -> {
            currentSort = "WASTE_DESC";
            refreshStatsTable(statsBuilder.getFilteredAndSortedEvaluations(currentSort, filterStartDate, filterEndDate));
            markActiveSortButton(btnSortWaste, btnSortWaste, btnSortRating, btnSortDate, btnSortSingleDay);
        });

        btnSortRating.addActionListener(e -> {
            currentSort = "RATING_DESC";
            refreshStatsTable(statsBuilder.getFilteredAndSortedEvaluations(currentSort, filterStartDate, filterEndDate));
            markActiveSortButton(btnSortRating, btnSortWaste, btnSortRating, btnSortDate, btnSortSingleDay);
        });

        btnSortDate.addActionListener(e -> {
            currentSort = "DATE_DESC";
            refreshStatsTable(statsBuilder.getFilteredAndSortedEvaluations(currentSort, filterStartDate, filterEndDate));
            markActiveSortButton(btnSortDate, btnSortWaste, btnSortRating, btnSortDate, btnSortSingleDay);
        });

        // [추가] 특정 날짜 하루치 평가만 모아보기 - 날짜 하나를 입력받아 기간 조회(시작일=종료일)를 재사용
        btnSortSingleDay.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "조회할 날짜를 입력해 주세요 (예: 2026-10-14):",
                    "특정 날짜만 보기", JOptionPane.PLAIN_MESSAGE);
            if (input == null || input.trim().isEmpty()) {
                return; // 취소하거나 빈 값이면 아무것도 안 함
            }

            String date = input.trim();
            try {
                date = java.time.LocalDate.parse(date).toString(); // 형식 검증 및 정규화
            } catch (java.time.format.DateTimeParseException ex) {
                Notice.show(this, "날짜는 2026-10-14 형식으로 입력해 주세요!", "입력 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }

            filterStartDate = date;
            filterEndDate = date;
            txtStartDate.setText(date);
            txtEndDate.setText(date);

            refreshStatsTable(statsBuilder.getFilteredAndSortedEvaluations(currentSort, filterStartDate, filterEndDate));
            markActiveSortButton(btnSortSingleDay, btnSortWaste, btnSortRating, btnSortDate, btnSortSingleDay);
        });

        // 초기 화면 로딩 시 최신 날짜 순으로 데이터 표기
        refreshStatsTable(statsBuilder.getFilteredAndSortedEvaluations(currentSort, filterStartDate, filterEndDate));
        markActiveSortButton(btnSortDate, btnSortWaste, btnSortRating, btnSortDate, btnSortSingleDay); // 기본값(최신순) 강조 표시

        JScrollPane scrollPane = new JScrollPane(table);

        // 정렬 버튼 줄 + 날짜 필터 줄을 세로로 묶어서 NORTH에 배치
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        topPanel.add(filterPanel);
        topPanel.add(dateFilterPanel);
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // 탭. 월간 메뉴별 선호도 통계 (막대그래프)
        private JPanel createMonthlyChartTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("이번 달 메뉴별 평균 만족도", SwingConstants.CENTER);
        lblTitle.setFont(new Font("맑은 고딕", Font.BOLD, 16));

        monthlyChartContainer = new JPanel(new BorderLayout());
        monthlyChartContainer.setBackground(Color.WHITE);
        refreshMonthlyChart(); // 초기 1회 그려줌

        // 이달의 추천 메뉴 보상 지급 버튼
        JButton btnReward = new JButton("🎁 이달의 추천 메뉴 보상 지급 (+500P)");
        btnReward.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btnReward.setBackground(new Color(0x06, 0x4E, 0x3B));
        btnReward.setForeground(Color.WHITE);
        btnReward.setOpaque(true);
        btnReward.setBorderPainted(false);
        btnReward.addActionListener(e -> {
            btnReward.setEnabled(false);
            String resultMessage = statsBuilder.rewardTopSuggestedMenu(500);
            Notice.show(this, resultMessage, "이달의 추천 메뉴 보상",
                    JOptionPane.INFORMATION_MESSAGE, () -> btnReward.setEnabled(true));
        });

        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(monthlyChartContainer, BorderLayout.CENTER);
        panel.add(btnReward, BorderLayout.SOUTH);

        return panel;
    }


    // [핵심] 월간 차트를 최신 데이터로 다시 그림 (컨테이너 안을 통째로 교체)
    private void refreshMonthlyChart() {
        if (monthlyChartContainer == null) return;
        monthlyChartContainer.removeAll();

        ChartData data = statsBuilder.buildMonthlyChartData();
        monthlyChartContainer.add(new BarChartPanel(data.title, data.labels, data.values, 5.0), BorderLayout.CENTER);

        monthlyChartContainer.revalidate();
        monthlyChartContainer.repaint();
    }

    // [핵심] 정렬 버튼 3개 중 현재 선택된 것만 굵게+초록색으로 강조 (나머지는 기본 스타일로 복구)
    private void markActiveSortButton(JButton active, JButton... allButtons) {
        for (JButton b : allButtons) {
            b.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
            b.setForeground(Color.BLACK);
        }
        active.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        active.setForeground(new Color(0x06, 0x4E, 0x3B));
    }

    // [예외 처리 및 UI 보호] JTable 표 행을 동적으로 재구성하는 메서드
    // NullPointerException 방어 및 데이터 0건 시 UI가 정상 유지되도록 예외 처리
    private void refreshStatsTable(List<Evaluation> evaluationList) {
        if (statsTableModel == null) return;

        // 기존 표의 모든 데이터 초기화
        statsTableModel.setRowCount(0);

        // [예외 방어] 데이터가 null이거나 0건(Empty)일 때 표 출력 예외 방지
        if (evaluationList == null || evaluationList.isEmpty()) {
            return; // 렌더링 중단하여 NullPointerException 및 불필요한 연산 방지
        }

        // 데이터 리스트 순회하며 테이블 데이터 채우기
        EvaluationManager evalManager = new EvaluationManager();
        List<Evaluation> allEvals = DataManager.getInstance().getEvaluationList(); // 메뉴별 집계는 항상 전체 데이터 기준으로 계산

        for (Evaluation eval : evaluationList) {
            // [수정] 잔반 감점(MainDish)/가중치(Dessert)가 반영된 보정 점수 기준으로 통일
            // -> 대시보드 TOP3와 다른 값이 나오던 문제 해결 (전에는 보정 전 원점수를 썼었음)
            double adjustedScore = eval.getMenuItem().getScore();
            int estimatedWasteRate = (int) Evaluation.estimateWasteRate(adjustedScore);

            // 이 평가가 속한 메뉴의 "총 후기 수"와 "잔반 후기 수"를 계산
            int[] menuStats = evalManager.getMenuWasteStats(eval.getMenuItem(), allEvals);

            statsTableModel.addRow(new Object[]{
                    eval.getDate(),
                    eval.getMenuName(),
                    estimatedWasteRate + "%",
                    String.format("%.1f", adjustedScore) + " / 5.0",
                    menuStats[0] + "건",
                    menuStats[1] + "건",
                    eval.getFeedback()
            });
        }
    }

    // 탭 3. 식단 개선 리포트 탭 생성 (실시간 데이터 분석 결과 텍스트 출력 및 .txt 문서 저장 기능 포함)
    private JPanel createReportTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        // 리포트 본문 출력 영역
        txtReport = new JTextArea();
        txtReport.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        txtReport.setMargin(new Insets(15, 15, 15, 15));
        txtReport.setEditable(false);

        // [UI 깨짐 방지] 긴 피드백 문장이 들어가더라도 패널 영역이 깨지지 않게 줄바꿈 적용
        txtReport.setLineWrap(true);
        txtReport.setWrapStyleWord(true);

        // 실시간 분석 리포트 텍스트 바인딩
        txtReport.setText(statsBuilder.buildRealtimeReportText());

        JScrollPane scrollReport = new JScrollPane(txtReport);

        // 하단 버튼 제어 영역 (새로고침 & .txt 저장)
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnPanel.setBackground(Color.WHITE);

        JButton btnRefresh = new JButton("🔄 리포트 새로고침");
        btnRefresh.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        btnRefresh.setPreferredSize(new Dimension(160, 45));

        JButton btnSave = new JButton("📄 리포트 .txt 파일로 저장하기");
        btnSave.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        btnSave.setBackground(new Color(0x06, 0x4E, 0x3B));
        btnSave.setForeground(Color.WHITE);
        btnSave.setOpaque(true); // macOS에서 버튼 배경색이 무시되는 문제 방지
        btnSave.setBorderPainted(false);
        btnSave.setPreferredSize(new Dimension(250, 45));
        btnSave.setFocusPainted(false);

        // 새로고침 버튼 이벤트
        btnRefresh.addActionListener(e -> {
            txtReport.setText(statsBuilder.buildRealtimeReportText());
            Notice.show(this, "최신 데이터로 리포트가 갱신되었습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
        });

        // .txt 저장 버튼 이벤트 - DataManager.generateReport()를 그대로 재사용
        btnSave.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("리포트 저장 경로 선택");
            fileChooser.setSelectedFile(new File("MealBack_Report_202609.txt"));

            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();

                // 화면 리포트와 같은 기준(EvaluationManager)으로 위험 메뉴를 다시 구해서 넘김
                List<Evaluation> evals = DataManager.getInstance().getEvaluationList();
                EvaluationManager evalManager = new EvaluationManager();
                List<MenuItem> riskMenus = evalManager.getRiskMenuList(DataManager.getInstance().getMenus(), evals);

                boolean success = DataManager.getInstance().generateReport(riskMenus, evals, fileToSave.getAbsolutePath());
                if (success) {
                    Notice.show(this,
                            "리포트가 성공적으로 저장되었습니다:\n" + fileToSave.getAbsolutePath(),
                            "저장 성공", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    Notice.show(this,
                            "리포트 저장 중 오류가 발생했습니다. 저장 경로와 권한을 확인해 주세요.",
                            "저장 실패", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnPanel.add(btnRefresh);
        btnPanel.add(btnSave);

        panel.add(scrollReport, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    
    // 탭. 공지사항 작성 (영양사가 글을 올리면 학생 화면에 노출됨)
    private JPanel createAnnouncementTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        // 1. 상단 작성 폼
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("제목:"), gbc);
        gbc.gridx = 1;
        JTextField txtTitle = new JTextField(20);
        formPanel.add(txtTitle, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("내용:"), gbc);
        gbc.gridx = 1;
        JTextArea txtContent = new JTextArea(3, 20);
        txtContent.setLineWrap(true);
        formPanel.add(new JScrollPane(txtContent), gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JButton btnPost = new JButton("공지 등록");
        btnPost.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        btnPost.setBackground(new Color(0x06, 0x4E, 0x3B));
        btnPost.setForeground(Color.WHITE);
        btnPost.setOpaque(true);
        btnPost.setBorderPainted(false);

        // 2. 하단 등록된 공지 목록 표시 영역
        JTextArea txtAnnouncementList = new JTextArea();
        txtAnnouncementList.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        txtAnnouncementList.setEditable(false);

        btnPost.addActionListener(e -> {
            // 한글 조합 중인 글자를 먼저 확정 (macOS 입력기가 조합 중에 텍스트를 지우면 창이 먹통 되는 문제 방지)
            txtTitle.getInputContext().endComposition();
            txtContent.getInputContext().endComposition();
            btnPost.requestFocusInWindow();

            String title = txtTitle.getText().trim();
            String content = txtContent.getText().trim();

            if (title.isEmpty() || content.isEmpty()) {
                Notice.show(this, "제목과 내용을 모두 입력해 주세요!", "입력 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String date = java.time.LocalDate.now().toString();
            DataManager.getInstance().addAnnouncement(new Announcement(title, content, date));

            SwingUtilities.invokeLater(() -> {
                txtTitle.setText("");
                txtContent.setText("");
            });
            refreshAnnouncementList(txtAnnouncementList);

            btnPost.setEnabled(false);
            Notice.show(this, "공지사항이 등록되었습니다!", "등록 완료",
                    JOptionPane.INFORMATION_MESSAGE, () -> btnPost.setEnabled(true));
        });

        formPanel.add(btnPost, gbc);

        refreshAnnouncementList(txtAnnouncementList); // 초기 1회 채워줌

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(txtAnnouncementList), BorderLayout.CENTER);

        return panel;
    }

    // [핵심] 등록된 공지사항을 최신순으로 텍스트 영역에 다시 그림
    private void refreshAnnouncementList(JTextArea target) {
        target.setText("");
        List<Announcement> announcements = DataManager.getInstance().getAnnouncements();
        for (int i = announcements.size() - 1; i >= 0; i--) { // 최신 글이 위에 오게 거꾸로 순회
            Announcement a = announcements.get(i);
            target.append("[" + a.getDate() + "] " + a.getTitle() + "\n" + a.getContent() + "\n\n");
        }
    }


    // 탭. 급식 일정 등록 (날짜 + 끼니 + 메뉴 목록을 미리 등록해두는 기능)
    private JPanel createMealScheduleTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        // 1. 상단 등록 폼
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("날짜:"), gbc);
        gbc.gridx = 1;
        JTextField txtScheduleDate = new JTextField(java.time.LocalDate.now().toString(), 15);
        formPanel.add(txtScheduleDate, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("끼니:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> comboMealType = new JComboBox<>(new String[]{"조식", "중식", "석식"});
        comboMealType.setSelectedItem("중식"); // 급식은 보통 중식이 메인이라 기본값으로 지정
        formPanel.add(comboMealType, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("메뉴명 (쉼표로 구분):"), gbc);
        gbc.gridx = 1;
        JTextField txtMenuNames = new JTextField("수제 등심 돈까스, 김치찌개, 요구르트", 20);
        formPanel.add(txtMenuNames, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JButton btnRegister = new JButton("급식 일정 등록");
        btnRegister.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        btnRegister.setBackground(new Color(0x06, 0x4E, 0x3B));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setOpaque(true);
        btnRegister.setBorderPainted(false);

        // 2. 하단 등록된 일정 목록 표시 영역
        JTextArea txtScheduleList = new JTextArea();
        txtScheduleList.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        txtScheduleList.setEditable(false);

        btnRegister.addActionListener(e -> {
            String date = txtScheduleDate.getText().trim();
            String mealType = (String) comboMealType.getSelectedItem();
            String rawMenus = txtMenuNames.getText().trim();

            // 날짜 형식 검증
            try {
                date = java.time.LocalDate.parse(date).toString();
            } catch (java.time.format.DateTimeParseException ex) {
                Notice.show(this, "날짜는 2026-10-15 형식으로 입력해 주세요!", "입력 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (rawMenus.isEmpty()) {
                Notice.show(this, "메뉴명을 한 개 이상 입력해 주세요!", "입력 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // [핵심] 쉼표로 구분된 문자열을 메뉴 이름 리스트로 쪼개기
            List<String> menuNames = new ArrayList<>();
            for (String name : rawMenus.split(",")) {
                String trimmed = name.trim();
                if (!trimmed.isEmpty()) {
                    menuNames.add(trimmed);
                }
            }

            DataManager.getInstance().addMealSlot(new MealSlot(date, mealType, menuNames));
            refreshScheduleList(txtScheduleList);

            btnRegister.setEnabled(false);
            Notice.show(this, "급식 일정이 등록되었습니다!", "등록 완료",
                    JOptionPane.INFORMATION_MESSAGE, () -> btnRegister.setEnabled(true));
        });

        formPanel.add(btnRegister, gbc);

        refreshScheduleList(txtScheduleList); // 초기 1회 채워줌

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(txtScheduleList), BorderLayout.CENTER);

        return panel;
    }

    // [핵심] 등록된 급식 일정을 날짜순으로 텍스트 영역에 다시 그림
    private void refreshScheduleList(JTextArea target) {
        target.setText("");
        for (MealSlot slot : DataManager.getInstance().getMealSlots()) {
            target.append(slot.getLabel() + " : " + String.join(", ", slot.getMenuNames()) + "\n");
        }
    }

    
    // 탭. 학생 활동 현황 (로그인한 학생들의 리뷰 횟수/등급/마일리지를 한눈에 봄)
    private JPanel createStudentActivityTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        String[] columns = {"학번", "이름", "등급", "리뷰 횟수", "마일리지"};
        studentActivityTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(studentActivityTableModel);
        table.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(0xF3, 0xF4, 0xF6));

        refreshStudentActivityTable(); // 초기 1회 채워줌

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // [핵심] 등록된 전체 학생의 활동 현황을 표에 다시 그림
    private void refreshStudentActivityTable() {
        if (studentActivityTableModel == null) return;
        studentActivityTableModel.setRowCount(0);

        for (Student student : DataManager.getInstance().getStudents()) {
            studentActivityTableModel.addRow(new Object[]{
                    student.getId(),
                    student.getName(),
                    student.getGrade(),
                    student.getReviewCount() + "회",
                    student.getMileage() + "P"
            });
        }
    }
}