// 학생 사용자 정보

import java.io.Serializable; // 파일 저장용

public class Student implements Serializable {
    // 클래스 구조가 바뀌어도 기존 .dat 파일을 안전하게 읽을 수 있도록 직렬화 버전 고정값 명시적 선언
    private static final long serialVersionUID = 1L;

    String stuID; // 학생 식별 아이디
    String stuName; // 학생 이름
    int mileage; // 누적 마일리지
    String mGrade; // 마일리지 등급 (BRONZE, SILVER, GOLD)

    public Student(String stuID, String stuName) {
        this.stuID = stuID;
        this.stuName = stuName;
        this.mileage = 0; // 객체 생성 시 마일리지 자동으로 0으로 설정
        this.mGrade = "BRONZE"; // 기본 등급 설정
    }

    public void addMileage(int point) { // 평점 등록이나 잔반 제로 달성 시 마일리지 가산
        this.mileage += point; // 포인트 -> 마일리지 적립
        updateMGrade(); // 마일리지 적립됐으면 등급 갱신 (메서드 연동)
    }

    public boolean deductMileage(int point) { // 숍에서 기프티콘 구매 시 마일리지 차감
        // 결제 시 마일리지 부족 여부 확인하기 위해 boolean을 반환함.
        if(this.mileage >= point) { // 잔액이 충분하다면?
            this.mileage -= point;
            return true; // 차감 성공
        }
        return false; // 잔액 부족으로 차감 실패
    }

    public String getName() {
        return stuName;
    }

    public int getMileage() {
        return mileage;
    }

    private void updateMGrade() { // 등급 갱신 내부 private 메서드 (외부에서 임의로 등급 조작 못하도록 보호)
        if(this.mileage >= 1000) {
            this.mGrade = "GOLD";
        } else if(this.mileage >= 500) {
            this.mGrade = "SILVER";
        } else {
            this.mGrade = "BRONZE";
        }
    }
}