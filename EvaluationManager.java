// 평가 데이터 분석과 잔반 위험 식단 분류

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvaluationManager {
    // 잔반 위험 판정 기준(%) - NutritionistMainPanel도 이 상수를 그대로 가져다 씀
    public static final double RISK_WASTE_RATE = 30.0;

    // [핵심/신규] 특정 메뉴 하나에 대한 "전체 평가 건수"와 "잔반 발생 건수"를 계산
    // -> 예전엔 이 계산(for문)이 DataManager, NutritionistMainPanel 등 여러 군데서
    //    똑같이 반복되고 있었음. 한 곳으로 모아서 다 같이 가져다 쓰게 함
    public int[] getMenuWasteStats(MenuItem menu, List<Evaluation> allEvaluations) {
        int totalCount = 0; // 이 메뉴에 대한 총 평가 횟수
        int leftoverCount = 0; // 이 메뉴에서 잔반이 발생한 횟수 (hasLeftover == true)

        for (Evaluation eval : allEvaluations) { // 설문지 상자 한 장씩 읽으며 숫자 카운팅
            if (eval.getMenuItem().equals(menu)) { // 설문지에 적힌 메뉴와 지금 검사중인 메뉴가 동일하다면?
                totalCount++;
                if (eval.isHasLeftover()) { // 설문지에서 잔반이 나왔다고 하면? (true)
                    leftoverCount++;
                }
            }
        }
        return new int[]{totalCount, leftoverCount}; // [0]=총 건수, [1]=잔반 건수
    }

    // 전체 평가 데이터를 분석하여 잔반율 30% 이상 위험 메뉴 리스트 반환할 메서드
    public List<MenuItem> getRiskMenuList(List<MenuItem> allMenus, List<Evaluation> allEvaluations) {
        // allMenus: 검사할 전체 메뉴 목록 / allEvaluations: 학생들이 제출한 전체 평가 데이타 목록
        List<MenuItem> riskMenuList = new ArrayList<>(); // 위험 기준 넘은 메뉴들만 넣을 리스트

        for (MenuItem menu : allMenus) { // 식단표에 있던 메뉴 하나씩 가져와서 검사
            // [변경] 직접 세던 for문을 getMenuWasteStats() 호출로 교체
            int[] stats = getMenuWasteStats(menu, allEvaluations);
            int totalCount = stats[0];
            int leftoverCount = stats[1];

            if (totalCount > 0) { // 평가 횟수가 1회 이상이면 잔반율 계산
                double leftoverRate = ((double) leftoverCount / totalCount) * 100.0;

                if (leftoverRate > RISK_WASTE_RATE) { // 잔반율이 기준치를 넘는다면?
                    riskMenuList.add(menu);
                }
            }
        }
        return riskMenuList; // 모든 메뉴 확인한 뒤, 위험 메뉴 리스트 반환
    }


    // [핵심] 메뉴 이름별로 모든 평가를 묶어서 "진짜 평균 만족도"를 계산 (다형성 반영: MainDish/Dessert 보정 점수를 평균냄)
    // -> DataManager.getMenus()에 저장된 MenuItem은 "그 메뉴를 처음 평가했을 때의 점수"만 갖고 있어서
    //    평균이 아님. 그래서 Evaluation 전체를 직접 훑어서 새로 계산함
    public List<MenuItem> getRankedMenusBySatisfaction(List<Evaluation> allEvaluations) {
        Map<String, List<Double>> scoresByName = new HashMap<>();
        Map<String, MenuItem> representativeByName = new HashMap<>();

        for (Evaluation eval : allEvaluations) {
            String name = eval.getMenuName();
            if (!scoresByName.containsKey(name)) {
                scoresByName.put(name, new ArrayList<>());
                representativeByName.put(name, eval.getMenuItem()); // 열량 등 기본 정보 참고용으로만 기억
            }
            // eval.getMenuItem().getScore()를 써야 MainDish 잔반 감점/Dessert 보너스가 이 평가 건에 반영됨
            scoresByName.get(name).add(eval.getMenuItem().getScore());
        }

        List<MenuItem> result = new ArrayList<>();
        for (Map.Entry<String, List<Double>> entry : scoresByName.entrySet()) {
            double sum = 0;
            for (double score : entry.getValue()) {
                sum += score;
            }
            double average = sum / entry.getValue().size();

            MenuItem representative = representativeByName.get(entry.getKey());
            // 평균 점수를 담은 새 MenuItem을 만들어서 반환 (원본 데이터는 안 건드림)
            result.add(new MenuItem(entry.getKey(), representative.getKcal(), average));
        }

        Collections.sort(result, new SatisfactionComparator());
        return result;
    }
}