// 급식 메뉴 정보

import java.io.Serializable; // 파일 저장용
import java.util.Objects;

public class MenuItem implements Serializable {
    // 클래스 구조(필드 추가/삭제 등)가 나중에 바뀌어도 기존에 저장된 .dat 파일을 
    // InvalidClassException 없이 읽을 수 있도록 버전 고정값 명시적 선언
    private static final long serialVersionUID = 1L;

    protected String name; // 메뉴명
    protected int kcal; // 열량
    protected double score; // 메뉴 점수

    public MenuItem(String name, int kcal, double score) {
        this.name = name;
        this.kcal = kcal;
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    public String getName() {
        return name;
    }

    public int getKcal() {
        return kcal;
    }

    public void printInfo() {
        System.out.println("메뉴 이름: " + name + ", 열량(kcal): " + kcal + "kcal, 메뉴 점수: " + score);
    }

    // 이름 + 열량이 같으면 같은 메뉴로 인식하도록 equals()를 재정의
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MenuItem)) return false;
        MenuItem other = (MenuItem) obj;
        return kcal == other.kcal && Objects.equals(name, other.name);
    }

    // equals()를 재정의하면 hashCode()도 반드시 함께 재정의해야 함.
    @Override
    public int hashCode() {
        return Objects.hash(name, kcal);
    }
}