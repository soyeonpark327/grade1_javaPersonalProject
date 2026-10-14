// 누가, 어떤 메뉴에, 어떤 평가를 남겼는지

import java.io.Serializable; // 파일 저장용

public class Evaluation implements Serializable {
    // 클래스 구조가 바뀌어도 기존 .dat 파일을 안전하게 읽을 수 있도록 직렬화 버전 고정값 명시적 선언
    private static final long serialVersionUID = 1L;

    private String date; // 평가 작성 날짜 (학생 간이 제출 폼 전용, 기존 생성자로 만들면 null)
    private MenuItem menuItem; // 평가 대상
    private Student student; // 작성자
    private double score; // 기본 별점
    private String comment; // 한줄평
    private boolean hasLeftover; // 잔반 발생 여부 (true - 잔반 있음 / false - 잔반 없음)

    // 생성자 유효성 검증 (정상 범위 1.0 ~ 5.0으로 교정하여 저장)
    public Evaluation(String date, MenuItem menuItem, Student student, double score, String comment, boolean hasLeftover) {
        // 별점이 1미만이면 1로, 5초과면 5로 교정 (유효성 검증)
        if(score < 1.0) {
            score = 1.0;
        }
        if(score > 5.0) {
            score = 5.0;
        }

        // 별점 교정 후 생성자 내부에서 전달받은 값을 필드에 채움.
        this.date = date;
        this.menuItem = menuItem;
        this.student = student;
        this.score = score;
        this.comment = comment;
        this.hasLeftover = hasLeftover;
    }

    // 학생용 급식 평가 작성 탭의 간이 제출 폼 전용 생성자
    // (잔반 여부 입력 항목이 아직 UI에 없어 기본값 false로 저장)
    public Evaluation(String date, String menuName, int score, String feedback) {
        this(date, new MenuItem(menuName, 0, score), DataManager.getInstance().getCurrentStudent(), score, feedback, false);
    }

    // [핵심] 별점 -> 추정 잔반율(%) 변환 공식을 한 곳에 모음
    // -> 여러 클래스에서 각자 (5.0 - score) * 20을 따로 계산하면, 나중에 공식이 바뀔 때 전부 찾아 고쳐야 함
    public static double estimateWasteRate(double score) {
        return (5.0 - score) * 20;
    }

    // 학생이 고른 메뉴 종류(MainDish/Dessert)와 잔반 여부를 그대로 받아 저장하는 생성자
    // (MenuItem을 여기서 새로 안 만들고, StudentMainPanel에서 이미 만든 걸 그대로 받음)
    public Evaluation(String date, MenuItem menuItem, int score, String feedback, boolean hasLeftover) {
        this(date, menuItem, DataManager.getInstance().getCurrentStudent(), score, feedback, hasLeftover);
    }


    public String getDate() {
        return date;
    }

    public String getMenuName() {
        return menuItem.getName();
    }

    public String getFeedback() {
        return comment;
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