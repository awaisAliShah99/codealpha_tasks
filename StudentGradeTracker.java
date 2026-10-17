/*
 * Author: Awais Ali Shah
 * CodeAlpha Student ID: CA/DF1/301415
 * Internship Domain: Java Programming (Sept 2026 batch)
 */
import java.util.*;

/** CodeAlpha Task 1: Student Grade Tracker (console-based). */
public class StudentGradeTracker {

    static class Student {
        private final String name;
        private final List<Double> grades = new ArrayList<>();

        Student(String name) { this.name = name; }

        String getName() { return name; }
        void addGrade(double g) { grades.add(g); }
        boolean hasGrades() { return !grades.isEmpty(); }
        double average() { return grades.stream().mapToDouble(d -> d).average().orElse(0); }
        double highest() { return grades.stream().mapToDouble(d -> d).max().orElse(0); }
        double lowest() { return grades.stream().mapToDouble(d -> d).min().orElse(0); }
        List<Double> getGrades() { return grades; }
    }

    private static final List<Student> students = new ArrayList<>();
    private static final Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n=== Student Grade Tracker ===");
            System.out.println("1. Add student");
            System.out.println("2. Add grade to student");
            System.out.println("3. View student details");
            System.out.println("4. Summary report");
            System.out.println("5. Exit");
            switch (readInt("Choose: ")) {
                case 1 -> addStudent();
                case 2 -> addGrade();
                case 3 -> viewStudent();
                case 4 -> summary();
                case 5 -> { System.out.println("Goodbye!"); return; }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private static void addStudent() {
        System.out.print("Student name: ");
        String name = in.nextLine().trim();
        if (name.isEmpty()) { System.out.println("Name cannot be empty."); return; }
        if (find(name) != null) { System.out.println("Student already exists."); return; }
        students.add(new Student(name));
        System.out.println("Added " + name);
    }

    private static void addGrade() {
        Student s = pickStudent();
        if (s == null) return;
        double g = readDouble("Grade (0-100): ");
        if (g < 0 || g > 100) { System.out.println("Grade must be 0-100."); return; }
        s.addGrade(g);
        System.out.println("Grade recorded.");
    }

    private static void viewStudent() {
        Student s = pickStudent();
        if (s == null) return;
        if (!s.hasGrades()) { System.out.println(s.getName() + " has no grades yet."); return; }
        System.out.printf("%s -> grades: %s%nAverage: %.2f | Highest: %.2f | Lowest: %.2f%n",
                s.getName(), s.getGrades(), s.average(), s.highest(), s.lowest());
    }

    private static void summary() {
        List<Student> graded = students.stream().filter(Student::hasGrades).toList();
        if (graded.isEmpty()) { System.out.println("No grades to report."); return; }
        System.out.println("\n--- Summary Report ---");
        System.out.printf("%-20s %8s %8s %8s%n", "Name", "Average", "Highest", "Lowest");
        for (Student s : graded)
            System.out.printf("%-20s %8.2f %8.2f %8.2f%n", s.getName(), s.average(), s.highest(), s.lowest());
        Student top = Collections.max(graded, Comparator.comparingDouble(Student::average));
        Student bottom = Collections.min(graded, Comparator.comparingDouble(Student::average));
        double classAvg = graded.stream().mapToDouble(Student::average).average().orElse(0);
        System.out.printf("%nClass average: %.2f%nTop student: %s (%.2f)%nLowest average: %s (%.2f)%n",
                classAvg, top.getName(), top.average(), bottom.getName(), bottom.average());
    }

    private static Student pickStudent() {
        if (students.isEmpty()) { System.out.println("No students yet."); return null; }
        System.out.print("Student name: ");
        Student s = find(in.nextLine().trim());
        if (s == null) System.out.println("Student not found.");
        return s;
    }

    private static Student find(String name) {
        for (Student s : students) if (s.getName().equalsIgnoreCase(name)) return s;
        return null;
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(in.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Enter a whole number."); }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Double.parseDouble(in.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Enter a number."); }
        }
    }
}
