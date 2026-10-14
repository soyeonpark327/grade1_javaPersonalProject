// 학생 사용자 정보

import java.io.Serializable; // 파일 저장용
import java.time.LocalDate;

public class Student implements Serializable {
    // 클래스 구조가 바뀌어도 기존 .dat 파일을 안전하게 읽을 수 있도록 직렬화 버전 고정값 명시적 선언
    private static final long serialVersionUID = 1L;

    String stuID; // 학생 식별 아이디
    String stuName; // 학생 이름
    int mileage; // 누적 마일리지
    String mGrade; // 등급 (BRONZE, SILVER, GOLD) - 리뷰 작성 횟수 기준
    int reviewCount; // 지금까지 작성한 급식 평가 횟수
    int lastResetYear; // [추가] 리뷰 횟수를 마지막으로 초기화한 연도 (매년 등급 리셋에 사용)

    public Student(String stuID, String stuName) {
        this.stuID = stuID;
        this.stuName = stuName;
        this.mileage = 0; // 객체 생성 시 마일리지 자동으로 0으로 설정
        this.reviewCount = 0; // 리뷰 횟수도 0으로 시작
        this.mGrade = "BRONZE"; // 기본 등급 설정
        this.lastResetYear = LocalDate.now().getYear(); // 생성된 연도를 기준점으로 기록
    }

    // 지금 연도가 마지막 리셋 연도와 다른지 확인 (= 해가 바뀌었는지)
    public boolean needsYearlyReset() {
        return LocalDate.now().getYear() != lastResetYear;
    }

    // 새해를 맞아 리뷰 횟수와 등급을 초기화
    public void resetForNewYear() {
        this.reviewCount = 0;
        this.lastResetYear = LocalDate.now().getYear();
        updateMGrade(); // 리뷰 횟수가 0이 됐으니 등급도 BRONZE로 다시 계산됨
    }

    public void addMileage(int point) { // 평점 등록이나 잔반 제로 달성 시 마일리지 가산
        this.mileage += point; // 포인트 -> 마일리지 적립
    }

    // 급식 평가를 작성할 때마다 호출 - 리뷰 횟수를 늘리고 그 결과로 등급을 갱신
    public void incrementReviewCount() {
        this.reviewCount++;
        updateMGrade();
    }

    public boolean deductMileage(int point) { // 숍에서 기프티콘 구매 시 마일리지 차감
        // 결제 시 마일리지 부족 여부 확인하기 위해 boolean을 반환함.
        if(this.mileage >= point) { // 잔액이 충분하다면?
            this.mileage -= point;
            return true; // 차감 성공
        }
        return false; // 잔액 부족으로 차감 실패
    }

    public String getId() {
        return stuID;
    }

    public String getName() {
        return stuName;
    }

    public String getGrade() {
        return mGrade;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setName(String name) {
        this.stuName = name;
    }

    public int getMileage() {
        return mileage;
    }

    private void updateMGrade() { // 등급 갱신 내부 private 메서드 (외부에서 임의로 등급 조작 못하도록 보호)
        if(this.reviewCount >= 100) {
            this.mGrade = "GOLD";
        } else if(this.reviewCount >= 30) {
            this.mGrade = "SILVER";
        } else {
            this.mGrade = "BRONZE";
        }
    }
}