// 두 개의 메뉴 만족도 점수 비교 및 순위 매기기

import java.util.Comparator; // Comparator: 자바 표준 비교 도구

// MenuItem 객체 2개의 점수를 비교하여 BEST순으로 정렬하는 클래스
// implements: 자바의 비교 규격을 실제로 작동하는 코드로 만드는 기능 구현
// Comparator<MenuItem>: 급식 메뉴 객체만 받아오겠다고 지정하는 제네릭 문법 (타입을 임시 기호로 비워두는 것)
public class SatisfactionComparator implements Comparator<MenuItem> {
    @Override // Comparator에 정의 된 compare 메서드를 재정의
    public int compare(MenuItem m1, MenuItem m2) {
        double score1 = m1.getScore();
        double score2 = m2.getScore();

        // Double.compare(a, b)는 소수점 연산 오차 없이 double 점수를 안전하게 비교,
        // 첫번쨰 값인 a가 더 작으면 음수(-1) 반환, a가 더 크면 (1) 반환하며 정렬함.
        return Double.compare(score2, score1); // 내림차순 (BEST순) 정렬 위해 score2를 앞에 배치
    }
}