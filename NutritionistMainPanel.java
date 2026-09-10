// 영양사용 화면 구성 (JTabbedPane 활용)

import javax.swing.*;
import javax.swing.border.TitledBorder; // 테두리 제목 표시
import javax.swing.table.DefaultTableModel; // JTable 데이터 모델
import java.awt.*;
import java.io.File; // 파일 처리
import java.io.FileWriter; // 파일 쓰기
import java.io.IOException; // 예외 처리

// 영양사/관리자용 메인 화면 패널 (포레스트 그린 테마 헤더와 JTabbedPane 기반의 3개 관리자 전용 서브 탭 화면구성)
public class NutritionistMainPanel extends JPanel {
    // 화면 전환 및 메인 프레임 통신용 참조 변수
    private MainFrame mainFrame;

    public NutritionistMainPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        // 전체 패널 레이아웃: BorderLayout (NORTH: 관리자 헤더, CENTER: 탭 메뉴)
        setLayout(new BorderLayout());

        // 1. 상단 포레스트 그린 헤더 패널 추가
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. 중앙 서브메뉴용 JTabbedPane 생성
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        // 3. 3개 핵심 관리자 서브 탭 화면 생성 및 추가
        tabbedPane.addTab("관리자 대시보드", createDashboardTab());
        tabbedPane.addTab("잔반 통계 & 랭킹 분석", createStatsTab());
        tabbedPane.addTab("식단 개선 리포트", createReportTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // 헤더 영억
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());

        // 영양사용 시그니처 테마 색상: 딥 포레스트 그린 (#064E3B)
        headerPanel.setBackground(new Color(0x06, 0x4E, 0x3B));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // 좌측 타이틀
        JLabel titleLabel = new JLabel("Meal-Back - 영양사/관리자 모드");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);

        // 우측 역할 선택 화면 복귀 버튼
        JButton btnBack = new JButton("역할 선택으로");
        btnBack.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btnBack.setBackground(Color.WHITE);
        btnBack.setForeground(new Color(0x06, 0x4E, 0x3B));
        btnBack.setFocusPainted(false);

        // 클릭 시 역할 선택 초기 화면으로 전환
        btnBack.addActionListener(e -> mainFrame.showScreen("ROLE_SELECT"));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(btnBack, BorderLayout.EAST);

        return headerPanel;
    }

    // 탭 1. 관리자 대시보드 탭 (ROI 감축률/예산 절감액 위젯 카드 + 실시간 잔반 위험 경고 로그 창)
    private JPanel createDashboardTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        // 1. 상단 ROI 및 예산 절감 요약 위젯 영역 (1행 2열)
        JPanel widgetPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        widgetPanel.setOpaque(false);

        // 위젯 1: 잔반 감축률 카드
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

        // 위젯 2: 예산 절감액 카드
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

        // 2. 하단 실시간 잔반 위험 경고창 (JTextArea)
        JPanel warningPanel = new JPanel(new BorderLayout());
        warningPanel.setBackground(Color.WHITE);
        warningPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.RED, 1),
                "🚨 실시간 잔반 위험 경고 & 이상 징후 알림", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("맑은 고딕", Font.BOLD, 14), Color.RED));

        JTextArea txtWarning = new JTextArea();
        txtWarning.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        txtWarning.setEditable(false);
        txtWarning.append("[2026-09-10 13:10] [경고] '팽이버섯 미역국' 잔반 발생 비율 38% 초과 (평균 대비 +15%)\n");
        txtWarning.append("[2026-09-08 13:05] [주의] '나물무침' 항목 학생 피드백 불만족도 증가 (간이 세다는 의견 12건)\n");
        txtWarning.append("[2026-09-05 13:20] [알림] '수제 등심 돈까스' 잔반율 3.2%로 최저치 기록 (인기 식단 지정)\n");

        JScrollPane scrollWarning = new JScrollPane(txtWarning);
        warningPanel.add(scrollWarning, BorderLayout.CENTER);

        panel.add(widgetPanel, BorderLayout.NORTH);
        panel.add(warningPanel, BorderLayout.CENTER);

        return panel;
    }

    // 탭 2. 잔반 통계 & 랭킹 분석 탭 (정렬 필터 버튼 + JTable(DefaultTableModel) + JScrollPane 감싸기)
    private JPanel createStatsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        // 1. 상단 정렬 및 필터 버튼 영역
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

        // 2. 표 데이터 구성 (DefaultTableModel & JTable)
        String[] columnNames = {"날짜", "주 메뉴", "잔반율 (%)", "평균 별점", "학생 피드백 수"};
        Object[][] rowData = {
                {"2026-09-10", "수제 등심 돈까스", "5.2%", "4.8 / 5.0", "142건"},
                {"2026-09-09", "치킨마요덮밥", "3.1%", "4.9 / 5.0", "185건"},
                {"2026-09-08", "오징어 볶음", "34.5%", "2.3 / 5.0", "98건"},
                {"2026-09-05", "크림 스파게티", "8.7%", "4.7 / 5.0", "120건"},
                {"2026-09-04", "시래기 국밥", "41.2%", "1.9 / 5.0", "110건"}
        };

        DefaultTableModel tableModel = new DefaultTableModel(rowData, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 표 내용을 직접 수정하지 못하도록 읽기 전용 설정
            }
        };

        JTable table = new JTable(tableModel);
        table.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        table.setRowHeight(28); // 행 높이 조절
        table.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(0xF3, 0xF4, 0xF6));

        // JTable을 반드시 JScrollPane으로 감싸서 배치 (헤더 및 스크롤바 바인딩)
        JScrollPane scrollPane = new JScrollPane(table);

        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // 탭 3. 식단 개선 리포트 탭 (분석 리포트 출력 JTextArea + JFileChooser 기반 파일 저장 버튼)
    private JPanel createReportTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        // 1. 리포트 본문 텍스트 영역
        JTextArea txtReport = new JTextArea();
        txtReport.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        txtReport.setMargin(new Insets(15, 15, 15, 15));

        // 자동 생성된 리포트 내용 예시 템플릿
        txtReport.setText("=====================================================\n" +
                "       Meal-Back 월간 급식 잔반 및 식단 개선 종합 리포트\n" +
                "=====================================================\n\n" +
                "1. 총평\n" +
                "   - 이번 달 평균 잔반율은 전월 대비 28.5% 감소하는 성과를 도출함.\n" +
                "   - 육류/일식 선호 메뉴 제공 시 잔반량이 급격히 감소함.\n\n" +
                "2. 주요 개선 필요 식단\n" +
                "   - [시래기 국밥] 잔반율 41.2% (사유: 학생 선호도 저하, 레시피 개선 요망)\n" +
                "   - [오징어 볶음] 잔반율 34.5% (사유: 매운맛 강도 조절 필요 피드백 다수)\n\n" +
                "3. 영양사 개선 조치 제안\n" +
                "   - 나물 및 국류 조리 방식 변경 및 학생 선호 양념 반영 예정.\n" +
                "   - 잔반 감축 우수 학급 대상 마일리지 추가 지급 이벤트 추진.\n");

        JScrollPane scrollReport = new JScrollPane(txtReport);

        // 2. 하단 리포트 .txt 저장 버튼
        JButton btnSave = new JButton("📄 리포트 .txt 파일로 저장하기");
        btnSave.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        btnSave.setBackground(new Color(0x06, 0x4E, 0x3B));
        btnSave.setForeground(Color.WHITE);
        btnSave.setPreferredSize(new Dimension(250, 45));
        btnSave.setFocusPainted(false);

        // JFileChooser를 활용한 파일 저장 이벤트 구현
        btnSave.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("리포트 저장 경로 선택");
            fileChooser.setSelectedFile(new File("MealBack_Report_202609.txt"));

            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                try (FileWriter writer = new FileWriter(fileToSave)) {
                    writer.write(txtReport.getText());
                    JOptionPane.showMessageDialog(this, "리포트가 성공적으로 저장되었습니다:\n" + fileToSave.getAbsolutePath(), "저장 성공", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "파일 저장 중 오류가 발생했습니다: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(btnSave);

        panel.add(scrollReport, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }
}