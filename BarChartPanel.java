// 막대그래프를 직접 그리는 커스텀 패널 (Swing엔 차트 기능이 기본으로 없어서 직접 그림)

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class BarChartPanel extends JPanel {
    private List<String> labels; // 막대 아래 표시할 이름들 (메뉴명 등)
    private List<Double> values; // 각 막대의 높이에 해당하는 값 (점수 등)
    private double maxValue; // 그래프의 최댓값 기준 (막대 높이 비율 계산용)
    private String title; // 그래프 제목

    public BarChartPanel(String title, List<String> labels, List<Double> values, double maxValue) {
        this.title = title;
        this.labels = labels;
        this.values = values;
        this.maxValue = maxValue;
        setPreferredSize(new Dimension(500, 300));
        setBackground(Color.WHITE);
    }

    // [핵심] Swing이 이 패널을 화면에 그릴 때마다 자동으로 호출하는 메서드
    // -> 버튼 리스너처럼 "이벤트가 생기면 실행"이 아니라, "화면을 그려야 할 때마다 실행"되는 메서드
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // 배경색 등 JPanel 기본 그리기를 먼저 처리
        Graphics2D g2 = (Graphics2D) g; // 더 정밀한 그리기 기능을 쓰려고 형변환

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        // 제목 그리기
        g2.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        g2.setColor(Color.BLACK);
        g2.drawString(title, 20, 25);

        if (labels == null || labels.isEmpty()) {
            g2.drawString("표시할 데이터가 없습니다.", 20, 60);
            return;
        }

        int chartTop = 50; // 그래프 영역 시작 y좌표
        int chartBottom = panelHeight - 50; // 그래프 영역 끝 y좌표 (x축 이름 쓸 공간 확보)
        int chartHeight = chartBottom - chartTop;

        int barCount = labels.size();
        int barAreaWidth = (panelWidth - 40) / barCount; // 막대 하나가 차지할 가로 폭
        int barWidth = (int) (barAreaWidth * 0.6); // 막대 자체 두께 (양옆 여백 남기려고 60%만 사용)

        for (int i = 0; i < barCount; i++) {
            double value = values.get(i);
            int barHeight = (int) ((value / maxValue) * chartHeight); // 값 비율만큼 막대 높이 계산
            int x = 20 + i * barAreaWidth + (barAreaWidth - barWidth) / 2;
            int y = chartBottom - barHeight;

            g2.setColor(new Color(0x10, 0xB9, 0x81)); // 초록색 막대
            g2.fillRect(x, y, barWidth, barHeight);

            // 막대 위에 값 표시
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
            g2.drawString(String.format("%.1f", value), x, y - 5);

            // 막대 아래에 이름(라벨) 표시
            g2.drawString(labels.get(i), x, chartBottom + 15);
        }

        // x축 기준선
        g2.setColor(Color.GRAY);
        g2.drawLine(20, chartBottom, panelWidth - 20, chartBottom);
    }
}