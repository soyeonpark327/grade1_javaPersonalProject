// 영양사 화면의 통계/리포트 계산 로직만 모아둔 클래스
// -> NutritionistMainPanel은 화면 구성 + 이벤트 처리만 담당하고,
//    실제 숫자/텍스트를 만드는 계산 전부 담당

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NutritionistStatsBuilder {
    private EvaluationManager evalManager = new EvaluationManager();

    // [핵심] 정렬 기준 + 날짜 필터를 적용해서 평가 리스트를 반환
    public List<Evaluation> getFilteredAndSortedEvaluations(String sortType, String filterStartDate, String filterEndDate) {
        List<Evaluation> sorted = DataManager.getInstance().getSortedEvaluations(sortType);

        if (filterStartDate == null && filterEndDate == null) {
            return sorted; // 필터가 없으면 그대로 반환
        }

        List<Evaluation> filtered = new ArrayList<>();
        for (Evaluation eval : sorted) {
            String date = eval.getDate();
            if (date == null) continue;
            if (filterStartDate != null && date.compareTo(filterStartDate) < 0) continue;
            if (filterEndDate != null && date.compareTo(filterEndDate) > 0) continue;
            filtered.add(eval);
        }
        return filtered;
    }

    // [핵심] 전체 평가의 평균 별점으로 추정 잔반율을 계산
    public String buildAverageWasteRateText() {
        List<Evaluation> evals = DataManager.getInstance().getEvaluationList();
        if (evals.isEmpty()) {
            return "데이터 없음";
        }

        double totalScoreSum = 0.0;
        for (Evaluation eval : evals) {
            totalScoreSum += eval.getScore();
        }
        double averageScore = totalScoreSum / evals.size();
        double averageWasteRate = Evaluation.estimateWasteRate(averageScore);

        return String.format("📉 %.1f %%", averageWasteRate);
    }

    // [핵심] EvaluationManager로 위험 메뉴를 구해서 실시간 경고 로그 형태로 만듦
    public String buildWarningLogText() {
        List<Evaluation> evals = DataManager.getInstance().getEvaluationList();
        List<MenuItem> riskMenus = evalManager.getRiskMenuList(DataManager.getInstance().getMenus(), evals);

        if (riskMenus.isEmpty()) {
            return "현재 잔반율 30%를 초과하는 위험 식단이 없습니다.";
        }

        StringBuilder sb = new StringBuilder();
        for (MenuItem menu : riskMenus) {
            int[] stats = evalManager.getMenuWasteStats(menu, evals);
            int menuTotal = stats[0];
            int menuLeftover = stats[1];
            double leftoverRate = ((double) menuLeftover / menuTotal) * 100.0;

            sb.append("[경고] '").append(menu.getName()).append("' 잔반 발생 비율 ")
                    .append(String.format("%.0f", leftoverRate)).append("% (")
                    .append(menuLeftover).append("/").append(menuTotal).append("건)\n");
        }
        return sb.toString();
    }

    // [핵심] SatisfactionComparator로 메뉴를 만족도 순으로 정렬해 베스트/워스트 TOP 3를 문자열로 반환
    public String buildTopMenuRankingText() {
        // DataManager.getMenus()의 "처음 평가 때 점수"가 아니라, evalManager로 "진짜 평균"을 계산
        List<Evaluation> evals = DataManager.getInstance().getEvaluationList();
        List<MenuItem> menus = evalManager.getRankedMenusBySatisfaction(evals);

        if (menus.isEmpty()) {
            return "아직 등록된 평가가 없습니다.";
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

    // [핵심] 0으로 나누기 예외 방지하며 전체 리포트 텍스트를 만듦
    public String buildRealtimeReportText() {
        List<Evaluation> evals = DataManager.getInstance().getEvaluationList();
        StringBuilder sb = new StringBuilder();

        sb.append("=====================================================\n");
        sb.append("       Meal-Back 월간 급식 잔반 및 식단 개선 종합 리포트\n");
        sb.append("=====================================================\n\n");

        int totalCount = evals.size();

        double totalScoreSum = 0.0;
        double averageScore = 0.0;
        double averageWasteRate = 0.0;

        if (totalCount > 0) {
            for (Evaluation eval : evals) {
                totalScoreSum += eval.getScore();
            }
            averageScore = totalScoreSum / totalCount;
            averageWasteRate = Evaluation.estimateWasteRate(averageScore);
        }

        sb.append("1. 데이터 수집 현황\n");
        sb.append("   - 총 작성된 학생 평가 건수: ").append(totalCount).append("건\n");

        if (totalCount > 0) {
            sb.append(String.format("   - 전체 평균 만족도 별점: %.2f / 5.0 점\n", averageScore));
            sb.append(String.format("   - 전체 추정 평균 잔반율: %.1f %%\n\n", averageWasteRate));
        } else {
            sb.append("   - 전체 평균 만족도 별점: N/A (등록된 데이터 없음)\n");
            sb.append("   - 전체 추정 평균 잔반율: N/A\n\n");
        }

        sb.append("2. 위험 식단 (실제 잔반 발생 비율 ").append((int) EvaluationManager.RISK_WASTE_RATE).append("% 초과)\n");
        int riskCount = 0;

        if (totalCount > 0) {
            List<MenuItem> riskMenus = evalManager.getRiskMenuList(DataManager.getInstance().getMenus(), evals);

            for (MenuItem menu : riskMenus) {
                riskCount++;

                int[] stats = evalManager.getMenuWasteStats(menu, evals);
                int menuTotal = stats[0];
                int menuLeftover = stats[1];
                double leftoverRate = ((double) menuLeftover / menuTotal) * 100.0;

                sb.append("   - [").append(menu.getName()).append("] 잔반율: ")
                        .append(String.format("%.1f", leftoverRate)).append("% (")
                        .append(menuLeftover).append("/").append(menuTotal).append("건 잔반 발생)\n");
            }
        }

        if (riskCount == 0) {
            sb.append("   - 현재 잔반율 30%를 초과하는 위험 식단이 없습니다. (양호)\n");
        }

        sb.append("\n3. 영양사 개선 조치 제안\n");
        sb.append("   - 저득점 메뉴 조리 방식 재검토 및 학생 기호도 양념 반영 예정.\n");
        sb.append("   - 잔반 감축 우수 학급 대상 마일리지 지급 이벤트 지속 추진.\n");
        sb.append("\n=====================================================\n");
        sb.append("   작성일: ").append(LocalDate.now()).append(" | 작성자: Meal-Back 시스템\n");

        return sb.toString();
    }

    // [핵심] 이번 달 평가만 모아서, 메뉴 이름별 평균 별점을 계산해 차트 데이터로 만듦
    public ChartData buildMonthlyChartData() {
        List<Evaluation> evals = DataManager.getInstance().getEvaluationList();
        String thisMonth = LocalDate.now().toString().substring(0, 7); // 예: "2026-10"

        Map<String, List<Double>> scoresByMenu = new HashMap<>();
        for (Evaluation eval : evals) {
            if (eval.getDate() != null && eval.getDate().startsWith(thisMonth)) {
                String name = eval.getMenuName();
                if (!scoresByMenu.containsKey(name)) {
                    scoresByMenu.put(name, new ArrayList<>());
                }
                scoresByMenu.get(name).add(eval.getScore());
            }
        }

        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        for (Map.Entry<String, List<Double>> entry : scoresByMenu.entrySet()) {
            double sum = 0;
            for (double score : entry.getValue()) {
                sum += score;
            }
            double average = sum / entry.getValue().size();
            labels.add(entry.getKey());
            values.add(average);
        }

        return new ChartData("이번 달(" + thisMonth + ") 메뉴별 평균 별점", labels, values);
    }

    
    // [핵심] 이번 달 1위 메뉴를 찾아서, 그 메뉴를 추천한 학생이 있고 아직 보상 안 받았으면 마일리지 지급
    public String rewardTopSuggestedMenu(int bonusMileage) {
        ChartData data = buildMonthlyChartData();

        if (data.labels.isEmpty()) {
            return "이번 달 평가 데이터가 없어서 보상 대상이 없습니다.";
        }

        // 값이 제일 큰(=평균 별점이 가장 높은) 메뉴 찾기
        String topMenuName = null;
        double topScore = -1;
        for (int i = 0; i < data.labels.size(); i++) {
            if (data.values.get(i) > topScore) {
                topScore = data.values.get(i);
                topMenuName = data.labels.get(i);
            }
        }

        // 그 메뉴를 추천한, 아직 보상 안 받은 의견을 찾아서 보상 지급
        for (MenuSuggestion suggestion : DataManager.getInstance().getSuggestions()) {
            if (!suggestion.isRewarded() && suggestion.getMenuName().equalsIgnoreCase(topMenuName)) {
                suggestion.getSuggestedBy().addMileage(bonusMileage);
                suggestion.markRewarded();
                return suggestion.getSuggestedBy().getName() + " 학생이 추천한 '" + topMenuName
                        + "'이(가) 이번 달 1위입니다! 보너스 " + bonusMileage + "P를 지급했습니다.";
            }
        }

        return "이번 달 1위 메뉴('" + topMenuName + "')를 추천한 학생이 없습니다.";
    }

}