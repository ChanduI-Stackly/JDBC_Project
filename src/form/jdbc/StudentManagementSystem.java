package form.jdbc;

import java.sql.*;
import java.util.Scanner;

public class StudentManagementSystem {

    static final String URL = "jdbc:mysql://localhost:3306/jdbc_projects";
    static final String USER = "root";
    static final String PASSWORD = "root";

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        
        // Create the database connection only once
        try(Connection con = DriverManager.getConnection(URL,USER,PASSWORD)){
        	
        	System.out.println("Connected to MySQL successfully.");
        	
        	while(true){
				System.out.println("\n--- Student Management System ---");
				System.out.println("1. Add Student");
				System.out.println("2. view All Students");
				System.out.println("3. Search Student");
				System.out.println("4. Update Marks");
				System.out.println("5. Delete Students");
				System.out.println("6. Exit");
				System.out.println("Enter Your choice: ");
				
				int choice = sc.nextInt();
				sc.nextLine();
				
				switch (choice){
				case 1:
					addStudent(con,sc);
					break;
					
				case 2:
					viewStudents(con);
					break;
					
				case 3:
					searchStudent(con,sc);
					break;
					
				case 4:
					updateMarks(con, sc);
					break;
					
				case 5:
					deleteStudent(con,sc);
					break;
					
				case 6:
					System.out.println("Thank you for using the system.");
					return;
					
				default:
					System.out.println("Invalid choice. Try again.");
				}
			}
        }catch(SQLException e){
			// TODO: handle exception
        	System.out.println("Database error: " + e.getMessage());
		}finally{
			sc.close();
		}
    }
    
    // 1. Add student
    static void addStudent(Connection con, Scanner sc) throws SQLException{
    	System.out.println("Enter student name: ");
    	String name = sc.nextLine();
    	
    	System.out.println("Enter course: ");
    	String course = sc.nextLine();
    	
    	System.out.println("Enter email: ");
    	String email = sc.nextLine();
    	
    	System.out.println("Enter marks: ");
    	double marks = sc.nextDouble();
    	sc.nextLine();
    	
    	String sql = "INSERT INTO students " + "(student_name, course, email, marks)"+ "VALUES(?,?,?,?)";
    	
    	try(PreparedStatement ps = con.prepareStatement(sql)){
    		ps.setString(1, name);
    		ps.setString(2, course);
    		ps.setString(3, email);
    		ps.setDouble(4, marks);
    		
    		int rows = ps.executeUpdate();
    		
    		if(rows>0){
				System.out.println("Student added successfully.");
			}
    	}
    }
    
 // 2. View All Students
    static void viewStudents(Connection con) throws SQLException{

        String sql = "SELECT * FROM students";

        try(Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)){

            boolean found = false;

            while (rs.next()){
                found = true;

                System.out.println(
                        rs.getInt("student_id") + " | " +
                        rs.getString("student_name") + " | " +
                        rs.getString("course") + " | " +
                        rs.getString("email") + " | " +
                        rs.getDouble("marks"));
            }

            if(!found){
                System.out.println("No student records found.");
            }
        }
    }
    
 // 3. Search Student
    static void searchStudent(Connection con, Scanner sc) throws SQLException{

        System.out.print("Enter student ID: ");
        int id = sc.nextInt();
        sc.nextLine();

        String sql = "SELECT * FROM students WHERE student_id = ?";

        try(PreparedStatement ps = con.prepareStatement(sql)){
            ps.setInt(1, id);

            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    System.out.println("Student ID: " + rs.getInt("student_id"));
                    System.out.println("Name: " + rs.getString("student_name"));
                    System.out.println("Course: " + rs.getString("course"));
                    System.out.println("Email: " + rs.getString("email"));
                    System.out.println("Marks: " + rs.getDouble("marks"));
                } else{
                    System.out.println("Student not found.");
                }
            }
        }
    }
    
 // 4. Update Student Marks
    static void updateMarks(Connection con, Scanner sc) throws SQLException{

        System.out.print("Enter student ID: ");
        int id = sc.nextInt();

        System.out.print("Enter new marks: ");
        double marks = sc.nextDouble();
        sc.nextLine();

        String sql = "UPDATE students SET marks = ? " + "WHERE student_id = ?";

        try(PreparedStatement ps = con.prepareStatement(sql)){
            ps.setDouble(1, marks);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows>0){
                System.out.println("Marks updated successfully!");
            } else{
                System.out.println("Student not found.");
            }
        }
    }
    
 // 5. Delete Student
    static void deleteStudent(Connection con, Scanner sc) throws SQLException{

        System.out.print("Enter student ID to delete: ");
        int id = sc.nextInt();
        sc.nextLine();

        String sql = "DELETE FROM students WHERE student_id = ?";

        try(PreparedStatement ps = con.prepareStatement(sql)){
            ps.setInt(1, id);

            int rows= ps.executeUpdate();

            if(rows>0){
                System.out.println("Student deleted successfully");
            } else{
                System.out.println("Student not found.");
            }
        }
    }
}
