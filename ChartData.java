// 차트에 쓸 데이터(제목, 라벨, 값)를 담는 간단한 데이터 묶음 클래스
// 필드만 담아서 전달하는 용도라 getter/setter 없이 public 필드로 바로 접근하게 함

import java.util.List;

public class ChartData {
    public String title;
    public List<String> labels;
    public List<Double> values;

    public ChartData(String title, List<String> labels, List<Double> values) {
        this.title = title;
        this.labels = labels;
        this.values = values;
    }
}