// 객체 데이터를 파일로 저장 및 복원 (.dat 객체 백업 - ObjectOutputStream)
// .dat: 자바 프로그램이 읽기 위한 이진 데이터
// .txt: 사람이 메모장으로 열어서 바로 읽을 수 있는 텍스트 문서
// FileWriter: 한 개씩 나르는 것. 파일에 글자 하나 쓸 때마다 하드디스크를 건드리므로 속도 느림.
// BufferedWriter: 버퍼에 글자들을 모아두었다가 가득 차거나 작업이 끝나면 파일에 싹 쓰므로 파일 작성 속도 빠름.

// 객체 데이터를 파일로 저장 및 복원 (.dat 객체 백업 - ObjectOutputStream)
// .dat: 자바 프로그램이 읽기 위한 이진 데이터
// .txt: 사람이 메모장으로 열어서 바로 읽을 수 있는 텍스트 문서
// FileWriter: 한 개씩 나르는 것. 파일에 글자 하나 쓸 때마다 하드디스크를 건드리므로 속도 느림.
// BufferedWriter: 버퍼에 글자들을 모아두었다가 가득 차거나 작업이 끝나면 파일에 싹 쓰므로 파일 작성 속도 빠름.

import java.io.*; // java.io 안에 있는 모든 클래스 불러옴.
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    // 앱 전체에서 하나의 데이터 저장소만 쓰도록 하는 싱글턴 인스턴스
    private static DataManager instance;

    private List<Student> students = new ArrayList<>();
    private List<MenuItem> menus = new ArrayList<>();
    private List<Evaluation> evaluations = new ArrayList<>();
    private Student currentStudent;

    // 외부에서 new DataManager()로 직접 생성하지 못하도록 생성자를 감춤
    private DataManager() {
        // 로그인 기능이 아직 없어 임시로 현재 학생 한 명을 기본 등록
        currentStudent = new Student("S001", "김민준");
        students.add(currentStudent);
    }

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public Student getCurrentStudent() {
        return currentStudent;
    }

    public void addEvaluation(Evaluation eval) {
        evaluations.add(eval);
        currentStudent.addMileage(50); // 급식 평가 제출 시 50P 적립 (StudentMainPanel 안내 문구와 일치)
    }

    public List<Evaluation> getEvaluationList() {
        return evaluations;
    }

    // 1. 학생 목록, 메뉴 목록, 평가 목록을 backup.dat 파일에 바이트 형태로 통째로 저장함.
    public void saveData(List<Student> students, List<MenuItem> menus, List<Evaluation> evaluations, String filePath) {
        // students: 저장할 학생 리스트 / menus: 저장할 메뉴 리스트 / evaluations: 저장할 평가 리스트 / filePath: 저장할 파일 경로

        // try-with-resources 구문: try() 괄호 안에 생성된 스트림은 작업 종료 후 자동으로 close() 됨.
        try (
                FileOutputStream fos = new FileOutputStream(filePath); // 파일에 바이트를 쓸 기본 통로 생성
                ObjectOutputStream oos = new ObjectOutputStream(fos) // 객체를 바이트 데이터로 변환해주는 보조 통로 연결
        ) { // out.writeObject()를 통해 리스트 객체 통째로 파일 기록
            oos.writeObject(students); // 학생 목록 전체 백업
            oos.writeObject(menus); // 메뉴 목록 전체 백업
            oos.writeObject(evaluations); // 평가 목록 전체 백업

            System.out.println("데이터 백업이 성공적으로 완료되었습니다: " + filePath);
        } catch(IOException e) {
            // 파일 경로 오류, 용량 부족, 권한 문제 등이 생겼을 때의 예외 처리
            System.out.println("데이터 백업 중 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 2. 위험 식단 목록과 분석 데이터를 사람용 report.txt 파일로 저장함.
    public void generateReport(List<MenuItem> riskMenus, List<Evaluation> allEvaluations, String filePath) {
        // riskMenus: 위험 메뉴 리스트 / allEvaluations: 잔반율 세부 계산을 위한 평가 리스트 / filePath: 저장할 리포트 파일 경로

        // try-with-resources 구문: 작업 종료 후 BufferedWriter와 FileWriter를 자동으로 close() 함.
        try (
                FileWriter fw = new FileWriter(filePath); // 텍스트 파일 생성 연결 통로
                BufferedWriter bw = new BufferedWriter(fw) // 모아서 한 번에 적는 버퍼 보조 통로
        ) { // 리포트 헤더 작성
            bw.write("=========================================");
            bw.newLine(); // os에 맞는 줄바꿈 처리
            bw.write("★ [식단 개선 피드백 리포트] ★");
            bw.newLine();
            bw.write("=========================================");
            bw.newLine();
            bw.newLine();

            if(riskMenus.isEmpty()) { // 위험 메뉴가 하나도 없다면?
                bw.write("현재 잔반율 30%를 초과하는 위험 메뉴가 없습니다.");
                bw.newLine();
                bw.write("모든 메뉴가 양호하게 관리되고 있습니다.");
                bw.newLine();
            } else {
                bw.write("[경고] 잔반율 30% 초과 위험 메뉴 목록 및 영양사 피드백");
                bw.newLine();
                bw.write("-----------------------------------------");
                bw.newLine();

                int num = 1; // 메뉴 순번 표시

                for(MenuItem menu : riskMenus) { // 위험 메뉴 리스트 순회하며 텍스트 작성
                    int totalCount = 0; // 전체 평가 수
                    int leftoverCount = 0; // 잔반 수

                    for(Evaluation eval : allEvaluations) {
                        if(eval.getMenuItem().equals(menu)) { // 해당 메뉴의 설문인지
                            totalCount++;
                            if(eval.isHasLeftover()) { // 잔반이 발생했는지
                                leftoverCount++;
                            }
                        }
                    }
                    // 잔반율 재계산
                    double leftoverRate = 0.0;
                    if(totalCount > 0) { // 설문 하나 이상
                        leftoverRate = ((double) leftoverCount / totalCount) * 100.0;
                    }

                    // 리포트 본문
                    bw.write(num + ". 메뉴명: " + menu.getName());
                    bw.newLine();
                    bw.write("   - 총 평가 건수: " + totalCount + "건");
                    bw.newLine();
                    bw.write("   - 잔반 발생 건수: " + leftoverCount + "건");
                    bw.newLine();
                    // String.format을 이용해 소수점 첫째 자리까지 출력
                    bw.write(String.format("   - 잔반율: %.1f%%", leftoverRate));
                    bw.newLine();

                    // 영양사 피드백 메시지 작성
                    bw.write("   - [영양사 피드백]: 잔반율이 매우 높습니다. 조리법 변경 및 기호도 재조사가 필요합니다.");
                    bw.newLine();
                    bw.write("-----------------------------------------");
                    bw.newLine();

                    num++; // 다음 메뉴 번호 증가
                }
            }
            bw.write("=== [ 리포트 생성 완료 ] ===");
            bw.newLine();

            System.out.println("리포트 파일 생성이 완료되었습니다: " + filePath);
        } catch (IOException e) {
            System.err.println("리포트 파일 생성 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}