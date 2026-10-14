// 디저트 메뉴 나올 시 가산점 부여

public class Dessert extends MenuItem {
    // 직렬화 버전 호환을 위해 선언
    private static final long serialVersionUID = 1L;

    public Dessert(String name, int kcal, double score) {
        super(name, kcal, score); // super() 사용하여 부모 메서드 호출
    }

    // score 값 재정의 (오버라이딩)
    @Override
    public double getScore() {
        // 힉생 선호도가 높은 디저트는 긍정적 가중치를 줌
        double boosted = super.getScore() * 1.2;
        return Math.min(boosted, 5.0); // 별점은 5점 만점이라, 보너스를 받아도 5.0을 넘지 않게 함
    }
    
    @Override
    public void printInfo() {
        super.printInfo(); // 기본 정보 출력
        System.out.println("디저트 보너스 적용 점수: " + getScore());
    }
}