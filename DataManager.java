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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;

public class DataManager {
    // 시연용 초기 마일리지 - 발표 때 이 숫자만 바꾸고 다시 실행하면 학생이 이 마일리지로 시작함
    /* 여기 = 0 값을 10000으로 바꾸고 쿠폰 구매 */ private static final int DEMO_INITIAL_MILEAGE = 10000;

    // 영양사(관리자) 전용 계정 - 학생과 달리 아무나 가입할 수 없고 이 아이디/비번으로만 접속 가능
    private static final String ADMIN_ID = "admin";
    private static final String ADMIN_PW = "mealback1234";

    // 앱 전체에서 하나의 데이터 저장소만 쓰도록 하는 싱글턴 인스턴스
    private static DataManager instance;

    private List<Student> students = new ArrayList<>();
    private List<MenuItem> menus = new ArrayList<>();
    private List<Evaluation> evaluations = new ArrayList<>();
    private List<MenuSuggestion> suggestions = new ArrayList<>(); // 학생들이 제안한 메뉴 의견함
    private List<Announcement> announcements = new ArrayList<>(); // 영양사 공지사항 게시판
    private List<MealSlot> mealSlots = new ArrayList<>(); // 영양사가 등록한 급식 일정
    private Student currentStudent;

    // 외부에서 new DataManager()로 직접 생성하지 못하도록 생성자를 감춤
    private DataManager() {
        // 로그인 기능이 아직 없어 임시로 현재 학생 한 명을 기본 등록
        currentStudent = new Student("S001", "김민준");
        currentStudent.addMileage(DEMO_INITIAL_MILEAGE); // 시연용 초기 마일리지 적용
        students.add(currentStudent);

        seedMockMealSlots(); // 시연/테스트용 가상 급식 일정 등록
    }

    // 앱을 켤 때마다 "오늘" 기준으로 며칠치 가상 급식 일정을 미리 채워 넣는 메서드
    // (영양사가 아직 아무것도 등록하지 않아도 캘린더와 학생 평가 화면에 데이터가 보이도록 하기 위함)
    private void seedMockMealSlots() {
        java.time.LocalDate today = java.time.LocalDate.now();

        mealSlots.add(new MealSlot(today.minusDays(1).toString(), "중식",
                Arrays.asList("제육볶음", "계란국", "요구르트")));
        mealSlots.add(new MealSlot(today.toString(), "중식",
                Arrays.asList("수제 등심 돈까스", "김치찌개", "미니 초코케이크")));
        mealSlots.add(new MealSlot(today.plusDays(1).toString(), "중식",
                Arrays.asList("궁중떡볶이", "계란후라이", "식혜")));
        mealSlots.add(new MealSlot(today.plusDays(2).toString(), "중식",
                Arrays.asList("치킨마요덮밥", "미역국", "과일샐러드")));
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

    // [핵심] 학번으로 로그인 - 이미 등록된 학번이면 그 학생으로 전환, 처음 보는 학번이면 새로 등록
    public Student loginStudent(String stuID, String name) {
        // 1) 이미 등록된 학번인지 students 리스트를 처음부터 끝까지 확인
        for (Student st : students) {
            if (st.getId().equals(stuID)) {
                currentStudent = st; // 찾았으면 그 학생으로 전환 (마일리지, 평가 기록 그대로 유지)
                return st;
            }
        }

        // 2) 반복문을 다 돌았는데도 못 찾았으면 = 처음 보는 학번 → 새 학생을 만들어서 등록
        Student newStudent = new Student(stuID, name);
        newStudent.addMileage(DEMO_INITIAL_MILEAGE); // 시연용 초기 마일리지 적용 (처음 로그인하는 학생에게만 적용)
        students.add(newStudent);
        currentStudent = newStudent;
        return newStudent;
    }

    // [핵심] 영양사(관리자) 로그인 - 입력한 아이디/비번이 관리자 계정과 일치하는지 확인
    // char[]로 받는 이유: 비밀번호 칸(JPasswordField)의 getPassword()가 String이 아니라 char[]를 돌려주기 때문
    public boolean loginNutritionist(String id, char[] password) {
        return ADMIN_ID.equals(id) && Arrays.equals(ADMIN_PW.toCharArray(), password);
    }



    // [핵심] 현재 로그인한 학생이 같은 날짜 + 같은 메뉴로 이미 평가를 남겼는지 확인
    public boolean hasEvaluationFor(String date, String menuName) {
        for (Evaluation eval : evaluations) {
            if (eval.getDate().equals(date)
                    && eval.getMenuName().equalsIgnoreCase(menuName)
                    && eval.getStudent() != null
                    && eval.getStudent().getId().equals(currentStudent.getId())) {
                return true;
            }
        }
        return false;
    }

    public void addEvaluation(Evaluation eval) {
        evaluations.add(eval);
        currentStudent.addMileage(50); // 급식 평가 제출 시 50P 적립 (StudentMainPanel 안내 문구와 일치)
        currentStudent.incrementReviewCount(); // [추가] 리뷰 횟수 증가 -> 30회/100회 기준 등급 갱신

        // 새로 나온 메뉴면 menus 리스트에 등록
        // -> MenuItem.equals()가 이름+열량 기준이라, 같은 메뉴를 여러 번 평가해도 중복으로 안 쌓임
        MenuItem menu = eval.getMenuItem();
        if (!menus.contains(menu)) {
            menus.add(menu);
        }
    }

    // Collections.unmodifiableList()로 감싸서 외부에서 이 리스트를 직접 수정 못 하게 막음
    // -> 예전엔 DataManager.getInstance().getEvaluationList().clear() 처럼 외부에서 내부 상태를
    //    마음대로 건드릴 수 있었는데, 지금은 addEvaluation()/replaceAllData()를 거치지 않고는
    //    평가/메뉴/학생 목록을 바꿀 방법이 없음
    public List<Evaluation> getEvaluationList() {
        return Collections.unmodifiableList(evaluations);
    }

    public List<Student> getStudents() {
        return Collections.unmodifiableList(students);
    }

    public List<MenuItem> getMenus() {
        return Collections.unmodifiableList(menus);
    }

    // 메뉴 의견함에 새 추천 등록
    public void addSuggestion(MenuSuggestion suggestion) {
        suggestions.add(suggestion);
    }

    // 지금까지 등록된 메뉴 추천 전체 목록 (읽기 전용으로 반환 - 캡슐화)
    public List<MenuSuggestion> getSuggestions() {
        return Collections.unmodifiableList(suggestions);
    }

    // 영양사가 새 공지사항 등록
    public void addAnnouncement(Announcement announcement) {
        announcements.add(announcement);
    }

    // 전체 공지사항 목록 (읽기 전용으로 반환 - 캡슐화)
    public List<Announcement> getAnnouncements() {
        return Collections.unmodifiableList(announcements);
    }

    // 영양사가 새 급식 일정 등록
    public void addMealSlot(MealSlot slot) {
        mealSlots.add(slot);
    }

    // 등록된 급식 일정 전체 목록 (읽기 전용으로 반환 - 캡슐화)
    public List<MealSlot> getMealSlots() {
        return Collections.unmodifiableList(mealSlots);
    }

    // 백업 파일에서 복원한 데이터로 메모리 전체 교체 (현재 학생은 같은 ID의 복원 객체로 다시 연결)
    public void replaceAllData(List<Student> newStudents, List<MenuItem> newMenus, List<Evaluation> newEvaluations,
                                List<MenuSuggestion> newSuggestions, List<Announcement> newAnnouncements, List<MealSlot> newMealSlots) {
        String currentId = currentStudent != null ? currentStudent.getId() : null;

        students.clear();
        students.addAll(newStudents);
        menus.clear();
        menus.addAll(newMenus);
        evaluations.clear();
        evaluations.addAll(newEvaluations);
        suggestions.clear();
        suggestions.addAll(newSuggestions);
        announcements.clear();
        announcements.addAll(newAnnouncements);
        mealSlots.clear();
        mealSlots.addAll(newMealSlots);

        // 백업 파일 안에 현재 학생과 같은 ID가 있을 때만 currentStudent를 교체함
        // -> 못 찾았다고 아무나(첫 번째 학생)로 바꿔버리면, 복원했더니 다른 사람 계정이 되는
        //    버그가 될 수 있어서 아예 안 바꾸는 쪽을 선택
        for (Student st : students) {
            if (st.getId().equals(currentId)) {
                currentStudent = st;
                break;
            }
        }
    }

    // 현재 메모리 데이터 전체를 .dat 백업 파일로 저장 (성공 여부 반환)
    public boolean saveData(String filePath) {
        return saveData(students, menus, evaluations, suggestions, announcements, mealSlots, filePath);
    }

    // 1. 학생/메뉴/평가/추천/공지/일정 전체를 backup.dat 파일에 바이트 형태로 통째로 저장함.
    public boolean saveData(List<Student> students, List<MenuItem> menus, List<Evaluation> evaluations,
        List<MenuSuggestion> suggestions, List<Announcement> announcements, List<MealSlot> mealSlots,
        String filePath) {
        // students: 저장할 학생 리스트 / menus: 저장할 메뉴 리스트 / evaluations: 저장할 평가 리스트 / filePath: 저장할 파일 경로

        // try-with-resources 구문: try() 괄호 안에 생성된 스트림은 작업 종료 후 자동으로 close() 됨.
        try (
                FileOutputStream fos = new FileOutputStream(filePath); // 파일에 바이트를 쓸 기본 통로 생성
                ObjectOutputStream oos = new ObjectOutputStream(fos) // 객체를 바이트 데이터로 변환해주는 보조 통로 연결
        ) { // out.writeObject()를 통해 리스트 객체 통째로 파일 기록
            oos.writeObject(students); // 학생 목록 전체 백업
            oos.writeObject(menus); // 메뉴 목록 전체 백업
            oos.writeObject(evaluations); // 평가 목록 전체 백업
            oos.writeObject(suggestions); // 메뉴 추천 목록 전체 백업
            oos.writeObject(announcements); // 공지사항 전체 백업
            oos.writeObject(mealSlots); // 급식 일정 전체 백업

            System.out.println("데이터 백업이 성공적으로 완료되었습니다: " + filePath);
            return true;
        } catch(IOException e) {
            // 파일 경로 오류, 용량 부족, 권한 문제 등이 생겼을 때의 예외 처리
            System.out.println("데이터 백업 중 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 2. 위험 식단 목록과 분석 데이터를 사람용 report.txt 파일로 저장함.
    public boolean generateReport(List<MenuItem> riskMenus, List<Evaluation> allEvaluations, String filePath) {
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
                    int[] stats = new EvaluationManager().getMenuWasteStats(menu, allEvaluations);
                    int totalCount = stats[0]; // 전체 평가 수
                    int leftoverCount = stats[1]; // 잔반 수

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
            return true;
        } catch (IOException e) {
            System.err.println("리포트 파일 생성 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    // 정렬 기준에 따라 평가 목록을 정렬하여 반환하는 메서드
    public List<Evaluation> getSortedEvaluations(String criterion) {
        // 1. 원본 데이터 보호를 위해 사본 리스트 생성
        List<Evaluation> sortedList = new ArrayList<>(evaluations);

        // 2. 조건별 정렬 처리 (람다식 활용)
        if ("RATING_DESC".equals(criterion)) {
            // 만족도 높은 순 (별점 내림차순: e2 vs e1)
            sortedList.sort((e1, e2) -> Double.compare(e2.getScore(), e1.getScore()));
        } else if ("DATE_DESC".equals(criterion)) {
            // 최신 날짜 순 (날짜 문자열 내림차순)
            sortedList.sort(Comparator.comparing(Evaluation::getDate, Comparator.nullsLast(Comparator.<String>reverseOrder())));
        } else if ("WASTE_DESC".equals(criterion)) {
            // 잔반율 높은 순 (별점이 낮을수록 잔반량이 높다고 간주하여 별점 오름차순: e1 vs e2)
            sortedList.sort((e1, e2) -> Double.compare(e1.getScore(), e2.getScore()));
        }

        return sortedList;
    }
}