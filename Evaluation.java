// 누가, 어떤 메뉴에, 어떤 평가를 남겼는지

import java.io.Serializable; // 파일 저장용

public class Evaluation implements Serializable {
    // 클래스 구조가 바뀌어도 기존 .dat 파일을 안전하게 읽을 수 있도록 직렬화 버전 고정값 명시적 선언
    private static final long serialVersionUID = 1L;

    private MenuItem menuItem; // 평가 대상
    private Student student; // 작성자
    private double score; // 기본 별점
    private String comment; // 한줄평
    private boolean hasLeftover; // 잔반 발생 여부 (true - 잔반 있음 / false - 잔반 없음)

    // 생성자 유효성 검증 (정상 범위 1.0 ~ 5.0으로 교정하여 저장)
    public Evaluation(MenuItem menuItem, Student student, double score, String comment, boolean hasLeftover) {
        // 별점이 1미만이면 1로, 5초과면 5로 교정 (유효성 검증)
        if(score < 1.0) {
            score = 1.0;
        }
        if(score > 5.0) {
            score = 5.0;
        }

        // 별점 교정 후 생성자 내부에서 전달받은 값을 필드에 채움.
        this.menuItem = menuItem;
        this.student = student;
        this.score = score;
        this.comment = comment;
        this.hasLeftover = hasLeftover;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public Student getStudent() {
        return student;
    }

    public double getScore() {
        return score;
    }

    public String getComment() {
        return comment;
    }

    // boolean 타입은 get대신 is 붙여서 사용
    public boolean isHasLeftover() {
        return hasLeftover;
    }
}