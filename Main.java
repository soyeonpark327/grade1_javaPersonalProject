import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        MainDish rice = new MainDish("돼지갈비찜", 420, 4.5, true);
        Dessert cake = new Dessert("망고 에이드", 150, 4.0);

        rice.printInfo();
        cake.printInfo();

        // MenuItem 객체들만 담을 수 있는 ArrayList (칸 정해지지 않은 데이터 묶음)
        List<MenuItem> menuList = new ArrayList<>();
        // memuList에 자식 객체인 메인 메뉴(잔반 있음)와 디저트를 넣는다.
        menuList.add(new MainDish("돼지갈비찜", 420, 4.5, true));
        menuList.add(new Dessert("망고 에이드", 150, 4.0));

        // 리스트 안의 메뉴 하나씩 확인하기
        for(MenuItem item : menuList) {
            item.printInfo();
            System.out.println("------------------------------");
        }
    }
}