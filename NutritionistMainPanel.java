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
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;

// 컬렉션 프레임워크 (자바 기본 List 인터페이스 사용)
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// [영양사/관리자용 메인 화면 패널]
// JTabbedPane을 활용하여 대시보드, 잔반 통계, 식단 리포트 탭 제공
// 예외 처리(0으로 나누기 방지), instanceof 타입 검증(역직렬화 안전성), UI 레이아웃 깨짐 방지 적용
public class NutritionistMainPanel extends JPanel {

    // 화면 전환 제어 및 메인 프레임 통신을 위한 참조 변수
    private MainFrame mainFrame;

    // 잔반 통계 JTable 데이터를 동적으로 관리하고 갱신하기 위한 데이터 모델
    private DefaultTableModel statsTableModel;

    // 잔반율 위험 기준(%) - 추정 잔반율 = (5 - 별점) * 20 이므로 30% 초과는 별점 3.5 미만
    private static final double RISK_WASTE_RATE = 30.0;

    // 현재 선택된 정렬 기준 (탭 이동/새 데이터 반영 시 같은 기준으로 다시 표시)
    private String currentSort = "DATE_DESC";

    // 식단 리포트 텍스트를 실시간으로 업데이트하기 위한 JTextArea 참조 변수
    private JTextArea txtReport;

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
        tabbedPane.addTab("식단 개선 리포트", createReportTab());

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
        refreshStatsTable(DataManager.getInstance().getSortedEvaluations(currentSort));
        if (txtReport != null) {
            txtReport.setText(buildRealtimeReportText());
        }
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
        btnBackup.setFocusPainted(false); // 버튼 선택 시 테두리 점선 제거
        btnBackup.addActionListener(e -> saveBackupData());

        // 2) [4주차 추가] 데이터 불러오기/복원 (.dat) 버튼 (instanceof 타입 검증 연결)
        JButton btnLoad = new JButton("📂 데이터 불러오기");
        btnLoad.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btnLoad.setBackground(new Color(0x0F, 0x76, 0x6E)); // 딥 틸 색상
        btnLoad.setForeground(Color.WHITE);
        btnLoad.setFocusPainted(false);
        btnLoad.addActionListener(e -> loadBackupData());

        // 3) 역할 선택 초기 화면으로 복귀하는 버튼
        JButton btnBack = new JButton("역할 선택으로");
        btnBack.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btnBack.setBackground(Color.WHITE);
        btnBack.setForeground(new Color(0x06, 0x4E, 0x3B));
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> mainFrame.showScreen("ROLE_SELECT"));

        // 우측 패널에 버튼들 추가
        rightBtnPanel.add(btnBackup);
        rightBtnPanel.add(btnLoad);
        rightBtnPanel.add(btnBack);

        // 헤더 패널 좌/우에 요소 배치
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(rightBtnPanel, BorderLayout.EAST);

        return headerPanel;
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
                JOptionPane.showMessageDialog(this,
                        "전체 데이터(학생/메뉴/평가) 백업이 완료되었습니다!\n경로: " + fileToSave.getAbsolutePath(),
                        "백업 완료", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
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

            // ObjectInputStream을 통한 역직렬화 파일 읽기 (saveData()가 쓴 순서: 학생 -> 메뉴 -> 평가)
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileToLoad))) {
                Object studentObj = ois.readObject();
                Object menuObj = ois.readObject();
                Object evalObj = ois.readObject();

                // [유효성 검증] 3개 객체가 각각 올바른 요소 타입의 List인지 instanceof로 확인
                List<Student> students = castList(studentObj, Student.class);
                List<MenuItem> menus = castList(menuObj, MenuItem.class);
                List<Evaluation> evaluations = castList(evalObj, Evaluation.class);

                if (students != null && menus != null && evaluations != null) {
                    DataManager.getInstance().replaceAllData(students, menus, evaluations);
                    refreshAllViews();

                    JOptionPane.showMessageDialog(this,
                            "데이터가 성공적으로 복원되었습니다! (학생 " + students.size()
                                    + "명, 메뉴 " + menus.size() + "개, 평가 " + evaluations.size() + "건)",
                            "복원 완료", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "올바른 Meal-Back 백업 파일(.dat) 형식이 아니거나 내부에 잘못된 데이터가 포함되어 있습니다.",
                            "검증 실패", JOptionPane.ERROR_MESSAGE);
                }

            } catch (ClassNotFoundException ex) {
                JOptionPane.showMessageDialog(this,
                        "클래스 정보를 찾을 수 없습니다: " + ex.getMessage(),
                        "복원 오류", JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
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

        // 위젯 카드 1: 잔반 감축률
        JPanel card1 = new JPanel(new BorderLayout());
        card1.setBackground(new Color(0xF0, 0xFD, 0xF4));
        card1.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0x06, 0x4E, 0x3B), 2),
                "금월 잔반 감축률 (전월 대비)", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("맑은 고딕", Font.BOLD, 14), new Color(0x06, 0x4E, 0x3B)));

        JLabel lblRoi = new JLabel("📉 -28.5 %", SwingConstants.CENTER);
        lblRoi.setFont(new Font("맑은 고딕", Font.BOLD, 32));
        lblRoi.setForeground(new Color(0x04, 0x78, 0x57));
        card1.add(lblRoi, BorderLayout.CENTER);

        // 위젯 카드 2: 예산 절감액
        JPanel card2 = new JPanel(new BorderLayout());
        card2.setBackground(new Color(0xEC, 0xFD, 0xF5));
        card2.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0x06, 0x4E, 0x3B), 2),
                "누적 잔반 처리 예산 절감액", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("맑은 고딕", Font.BOLD, 14), new Color(0x06, 0x4E, 0x3B)));

        JLabel lblSavings = new JLabel("💰 1,420,000 원", SwingConstants.CENTER);
        lblSavings.setFont(new Font("맑은 고딕", Font.BOLD, 32));
        lblSavings.setForeground(new Color(0x04, 0x78, 0x57));
        card2.add(lblSavings, BorderLayout.CENTER);

        widgetPanel.add(card1);
        widgetPanel.add(card2);

        // 2. 하단 실시간 위험 경고 영역 (JTextArea)
        JPanel warningPanel = new JPanel(new BorderLayout());
        warningPanel.setBackground(Color.WHITE);
        warningPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.RED, 1),
                "🚨 실시간 잔반 위험 경고 & 이상 징후 알림", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("맑은 고딕", Font.BOLD, 14), Color.RED));

        JTextArea txtWarning = new JTextArea();
        txtWarning.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        txtWarning.setEditable(false); // 읽기 전용

        // [UI 깨짐 방지] 긴 문장 출력 시 창이 옆으로 늘어나 레이아웃이 깨지지 않도록 자동 줄바꿈 설정
        txtWarning.setLineWrap(true);       // 화면 끝에 도달하면 다음 줄로 자동 넘김
        txtWarning.setWrapStyleWord(true);   // 단어 단위로 깔끔하게 줄바꿈

        // 알림 로그 샘플 텍스트 추가
        txtWarning.append("[2026-09-10 13:10] [경고] '팽이버섯 미역국' 잔반 발생 비율 38% 초과 (평균 대비 +15%)\n");
        txtWarning.append("[2026-09-08 13:05] [주의] '나물무침' 항목 학생 피드백 불만족도 증가 (간이 세다는 의견 12건)\n");
        txtWarning.append("[2026-09-05 13:20] [알림] '수제 등심 돈까스' 잔반율 3.2%로 최저치 기록 (인기 식단 지정)\n");

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

        btnSortWaste.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        btnSortRating.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        btnSortDate.setFont(new Font("맑은 고딕", Font.PLAIN, 12));

        filterPanel.add(lblFilter);
        filterPanel.add(btnSortWaste);
        filterPanel.add(btnSortRating);
        filterPanel.add(btnSortDate);

        // 2. 표(JTable) 데이터 모델 생성 (수정 불가능하도록 익명 클래스 오버라이딩)
        String[] columnNames = {"날짜", "주 메뉴", "예상 잔반율 (%)", "평균 별점", "학생 한줄평 피드백"};
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
            refreshStatsTable(DataManager.getInstance().getSortedEvaluations(currentSort));
        });

        btnSortRating.addActionListener(e -> {
            currentSort = "RATING_DESC";
            refreshStatsTable(DataManager.getInstance().getSortedEvaluations(currentSort));
        });

        btnSortDate.addActionListener(e -> {
            currentSort = "DATE_DESC";
            refreshStatsTable(DataManager.getInstance().getSortedEvaluations(currentSort));
        });

        // 초기 화면 로딩 시 최신 날짜 순으로 데이터 표기
        refreshStatsTable(DataManager.getInstance().getSortedEvaluations(currentSort));

        JScrollPane scrollPane = new JScrollPane(table);

        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
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
        for (Evaluation eval : evaluationList) {
            int estimatedWasteRate = (int) ((5.0 - eval.getScore()) * 20);

            statsTableModel.addRow(new Object[]{
                    eval.getDate(),
                    eval.getMenuName(),
                    estimatedWasteRate + "%",
                    eval.getScore() + " / 5.0",
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
        txtReport.setText(buildRealtimeReportText());

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
        btnSave.setPreferredSize(new Dimension(250, 45));
        btnSave.setFocusPainted(false);

        // 새로고침 버튼 이벤트
        btnRefresh.addActionListener(e -> {
            txtReport.setText(buildRealtimeReportText());
            JOptionPane.showMessageDialog(this, "최신 데이터로 리포트가 갱신되었습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
        });

        // .txt 저장 버튼 이벤트 (FileWriter 활용)
        btnSave.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("리포트 저장 경로 선택");
            fileChooser.setSelectedFile(new File("MealBack_Report_202609.txt"));

            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                try (FileWriter writer = new FileWriter(fileToSave)) {
                    writer.write(txtReport.getText());
                    JOptionPane.showMessageDialog(this,
                            "리포트가 성공적으로 저장되었습니다:\n" + fileToSave.getAbsolutePath(),
                            "저장 성공", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this,
                            "파일 저장 중 오류가 발생했습니다: " + ex.getMessage(),
                            "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnPanel.add(btnRefresh);
        btnPanel.add(btnSave);

        panel.add(scrollReport, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // [핵심] 0으로 나누기 예외 방지 (ArithmeticException 처리)
    // 평가 데이터 건수가 0건일 때 나누기 0 연산을 방지하고 안전한 대체 문구 생성
    private String buildRealtimeReportText() {
        List<Evaluation> evals = DataManager.getInstance().getEvaluationList();
        StringBuilder sb = new StringBuilder();

        sb.append("=====================================================\n");
        sb.append("       Meal-Back 월간 급식 잔반 및 식단 개선 종합 리포트\n");
        sb.append("=====================================================\n\n");

        int totalCount = evals.size();

        // [예외 방지] 0으로 나누기(Division by Zero / ArithmeticException / NaN) 사전 차단
        double totalScoreSum = 0.0;
        double averageScore = 0.0;
        double averageWasteRate = 0.0;

        // totalCount > 0 조건일 때만 나누기(/) 연산 실행
        if (totalCount > 0) {
            for (Evaluation eval : evals) {
                totalScoreSum += eval.getScore();
            }
            averageScore = totalScoreSum / totalCount; // 0건 분기문으로 나누기 0 완벽 차단
            averageWasteRate = (5.0 - averageScore) * 20; // 평균 잔반율 계산
        }

        sb.append("1. 데이터 수집 현황\n");
        sb.append("   - 총 작성된 학생 평가 건수: ").append(totalCount).append("건\n");

        // [UI 방어] 데이터 유무에 따른 수치/N/A 표기 분기 처리
        if (totalCount > 0) {
            sb.append(String.format("   - 전체 평균 만족도 별점: %.2f / 5.0 점\n", averageScore));
            sb.append(String.format("   - 전체 추정 평균 잔반율: %.1f %%\n\n", averageWasteRate));
        } else {
            // 데이터가 0건일 때 ArithmeticException 대신 안전한 안내 문자열 출력
            sb.append("   - 전체 평균 만족도 별점: N/A (등록된 데이터 없음)\n");
            sb.append("   - 전체 추정 평균 잔반율: N/A\n\n");
        }

        sb.append("2. 위험 식단 (추정 잔반율 ").append((int) RISK_WASTE_RATE).append("% 초과)\n");
        int riskCount = 0;

        // 데이터가 존재하는 경우에만 저득점 식단 분석 실행
        if (totalCount > 0) {
            for (Evaluation eval : evals) {
                double wasteRate = (5.0 - eval.getScore()) * 20;
                if (wasteRate > RISK_WASTE_RATE) {
                    riskCount++;
                    sb.append("   - [").append(eval.getMenuName()).append("] 추정 잔반율: ")
                            .append(String.format("%.0f", wasteRate)).append("% (별점 ")
                            .append(eval.getScore()).append("점) | 의견: ").append(eval.getFeedback()).append("\n");
                }
            }
        }

        if (riskCount == 0) {
            sb.append("   - 현재 추정 잔반율 30%를 초과하는 위험 식단이 없습니다. (양호)\n");
        }

        sb.append("\n3. 영양사 개선 조치 제안\n");
        sb.append("   - 저득점 메뉴 조리 방식 재검토 및 학생 기호도 양념 반영 예정.\n");
        sb.append("   - 잔반 감축 우수 학급 대상 마일리지 지급 이벤트 지속 추진.\n");
        sb.append("\n=====================================================\n");
        sb.append("   작성일: " + LocalDate.now() + " | 작성자: Meal-Back 시스템\n");

        return sb.toString();
    }
}