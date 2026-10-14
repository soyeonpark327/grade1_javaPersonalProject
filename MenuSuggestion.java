// 학생이 제안한 급식 메뉴 아이디어 (학생 의견함)
// -> 나중에 그 메뉴가 실제로 나와서 이달의 1위를 하면, 여기 저장된 누가 추천했는지를 보고 보상을 줌

import java.io.Serializable;

public class MenuSuggestion implements Serializable {
    private static final long serialVersionUID = 1L;

    private String menuName; // 추천한 메뉴 이름
    private String reason; // 추천 이유 (한줄)
    private Student suggestedBy; // 추천한 학생
    private String date; // 추천한 날짜
    private boolean rewarded; // 이미 보상을 지급했는지 (중복 지급 방지)

    public MenuSuggestion(String menuName, String reason, Student suggestedBy, String date) {
        this.menuName = menuName;
        this.reason = reason;
        this.suggestedBy = suggestedBy;
        this.date = date;
        this.rewarded = false; // 처음 추천할 땐 당연히 아직 보상 안 받은 상태
    }

    public String getMenuName() {
        return menuName;
    }

    public String getReason() {
        return reason;
    }

    public Student getSuggestedBy() {
        return suggestedBy;
    }

    public String getDate() {
        return date;
    }

    public boolean isRewarded() {
        return rewarded;
    }

    public void markRewarded() {
        this.rewarded = true;
    }
}