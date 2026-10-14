// 영양사가 등록하는 날짜별 급식 일정 (하루 중 한 끼 - 조식/중식/석식 - 에 나오는 메뉴들)

import java.io.Serializable;
import java.util.List;

public class MealSlot implements Serializable {
    private static final long serialVersionUID = 1L;

    private String date; // "2026-10-15"
    private String mealType; // "조식", "중식", "석식"
    private List<String> menuNames; // 그 끼니에 나오는 메뉴 이름들

    public MealSlot(String date, String mealType, List<String> menuNames) {
        this.date = date;
        this.mealType = mealType;
        this.menuNames = menuNames;
    }

    public String getDate() {
        return date;
    }

    public String getMealType() {
        return mealType;
    }

    public List<String> getMenuNames() {
        return menuNames;
    }

    // 화면에 "2026-10-15 중식"처럼 표시할 때 쓰는 문구
    public String getLabel() {
        return date + " " + mealType;
    }
}