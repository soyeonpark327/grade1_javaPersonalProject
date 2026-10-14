// 영양사가 올리는 공지사항 (학생 화면에 노출됨)

import java.io.Serializable;

public class Announcement implements Serializable {
    private static final long serialVersionUID = 1L;

    private String title; // 공지 제목
    private String content; // 공지 내용
    private String date; // 작성 날짜

    public Announcement(String title, String content, String date) {
        this.title = title;
        this.content = content;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getDate() {
        return date;
    }
}