    import java.util.*;

class Student {
    private int regNo;
    private String name;
    private double performance;

    Student(int regNo, String name, double performance) {
        this.regNo = regNo;
        this.name = name;
        this.performance = performance;
    }

    int getRegNo() {
        return regNo;
    }

    double getPerformance() {
        return performance;
    }

    void display() {
        System.out.println("RegNo:" +regNo+ " | Name:" +name+" | Performance:"+performance);
    }
}

public class StudentPerformance {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = 7;
        List<Student> students = new ArrayList<>();

        System.out.println("Enter details for " + n + " students:");
        for (int i = 1; i <= n; i++) {
            System.out.println("Enter details for students:"+i);
            System.out.print("Enter Registration No: ");
            int regNo = sc.nextInt();
            sc.nextLine();
            System.out.print("Enter Name: ");
            String name = sc.nextLine();
            System.out.print("Enter Performance Score: ");
            double perf = sc.nextDouble();
            students.add(new Student(regNo, name, perf));
        }

        // Sort using performance and regNo 
        students.sort((s1, s2) -> {
            if (s1.getPerformance() == s2.getPerformance())
                return Integer.compare(s1.getRegNo(), s2.getRegNo());
            else
                return Double.compare(s2.getPerformance(), s1.getPerformance());
        });

        System.out.println("\n--- Sorted by Performance (Desc), RegNo (Asc) ---");
        for (Student s : students)
            s.display();
    }
}