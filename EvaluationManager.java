// 평가 데이터 분석과 잔반 위험 식단 분류

import java.util.ArrayList;
import java.util.List;

public class EvaluationManager {
    // 전체 평가 데이터를 분석하여 잔반율 30% 이상 위험 메뉴 리스트 반환할 메서드
    public List<MenuItem> getRiskMenuList(List<MenuItem> allMenus, List<Evaluation> allEvaluations) {
        // allMenus: 검사할 전체 메뉴 목록 / allEvaluations: 학생들이 제출한 전체 평가 데이타 목록
        List<MenuItem> riskMenuList = new ArrayList<>(); // 위험 기준 넘은 메뉴들만 넣을 리스트

        for(MenuItem menu : allMenus) { // 식단표에 있던 메뉴 하나씩 가져와서 검사
            // 검사 중인 메뉴의 카운트를 0으로 초기화
            int totalCount = 0; // 이 메뉴에 대한 총 평가 횟수
            int leftoverCount = 0; // 이 메뉴에서 잔반이 발생한 횟수 (hasLeftover == true)

            for(Evaluation eval : allEvaluations) { // 설문지 상자 한 장씩 읽으며 숫자 카운팅
                if(eval.getMenuItem().equals(menu)) { // 설문지에 적힌 메뉴와 지금 검사중인 메뉴가 동일하다면?
                    totalCount++;

                    if(eval.isHasLeftover()) { // 설문지에서 잔반이 나왔다고 하면? (true)
                        leftoverCount++;
                    }
                }
            }
            if(totalCount > 0) { // 평가 횟수가 1회 이상이면 잔반율 계산
                // 정수인 leftoverCount 앞에 (double)을 붙여 실수 형변환하여 계산 (소수점 뒤 값이 사라지지 X)
                double leftoverRate = ((double) leftoverCount / totalCount) * 100.0;

                if(leftoverRate > 30.0) { // 잔반율이 30% 이상이라면?
                    riskMenuList.add(menu);
                }
            }
        }
        return riskMenuList; // 모든 메뉴 확인한 뒤, 위험 메뉴 리스트 반환
    }
}