// 메인 메뉴 정보 및 잔반 여부

public class MainDish extends MenuItem {
    // 직렬화 버전 호환을 위해 선언
    // 자식 클래스도 각자 별도로 선언해줘야 함 (부모 것만 있다고 상속되지 않음)
    private static final long serialVersionUID = 1L;

    private boolean hasLeftover; // 잔반 여부

    public MainDish(String name, int kcal, double score, boolean hasLeftover) {
        super(name, kcal, score); // 부모 생성자 호출 (메뉴명, 열량, 점수 값 전달)
        this.hasLeftover = hasLeftover;
    }

    // score 값 재정의 (오버라이딩)
    @Override
    public double getScore() {
        if(hasLeftover) { // 잔반을 남겼다면? (true라면)
            return (super.getScore() - 1.5); // 부모 점수에서 1.5 값을 뺀 값 리턴
        } else {
            return super.getScore(); // 잔반 없을 시 원래 점수 출력
        }
    }

    @Override
    public void printInfo() {
        super.printInfo(); // 부모 클래스에서의 기본 정보 출력
        System.out.println("잔반 여부: " + hasLeftover);
    }
}