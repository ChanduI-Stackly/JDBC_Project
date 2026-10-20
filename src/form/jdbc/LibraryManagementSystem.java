package form.jdbc;

import java.sql.*;
import java.util.Scanner;

public class LibraryManagementSystem {

    static final String URL = "jdbc:mysql://localhost:3306/jdbc_projects";
    static final String USER = "root";
    static final String PASSWORD = "root";

    public static void main(String[] args) {
    	// TODO Auto-generated method stub
    	
    	Scanner sc = new Scanner(System.in);

        // Create the database connection only once
        try(Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {

            System.out.println("Connected to MySQL successfully!");

            while(true){
                System.out.println("\n--- Library Management System ---");
                System.out.println("1. Add Book");
                System.out.println("2. Register Member");
                System.out.println("3. Issue Book");
                System.out.println("4. Return Book");
                System.out.println("5. View Issue Records");
                System.out.println("6. Exit");
                System.out.print("Enter your choice: ");

                int choice = sc.nextInt();
                sc.nextLine();

                switch(choice){
                    case 1:
                        addBook(con, sc);
                        break;

                    case 2:
                        registerMember(con, sc);
                        break;

                    case 3:
                        issueBook(con, sc);
                        break;

                    case 4:
                        returnBook(con, sc);
                        break;

                    case 5:
                        viewIssueRecords(con);
                        break;

                    case 6:
                        System.out.println("Thank you for using the system.");
                        return;

                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }

        } catch(SQLException e){
            System.out.println("Database error: " + e.getMessage());
        } finally{
            sc.close();
        }
    }

    // 1. Add Book
    static void addBook(Connection con, Scanner sc) throws SQLException {

        System.out.print("Enter book title: ");
        String title = sc.nextLine();

        System.out.print("Enter author name: ");
        String author = sc.nextLine();

        String sql = "INSERT INTO books (title, author) VALUES (?, ?)";

        try(PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, author);

            ps.executeUpdate();

            System.out.println("Book added successfully");
        }
    }

    // 2. Register Member
    static void registerMember(Connection con, Scanner sc) throws SQLException{

        System.out.print("Enter member name: ");
        String name = sc.nextLine();

        String sql = "INSERT INTO members (member_name) VALUES (?)";

        try(PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, name);
            ps.executeUpdate();

            System.out.println("Member registered successfully");
        }
    }

    // 3. Issue Book
    static void issueBook(Connection con, Scanner sc) throws SQLException{

        System.out.print("Enter book ID: ");
        int bookId = sc.nextInt();

        System.out.print("Enter member ID: ");
        int memberId = sc.nextInt();
        sc.nextLine();

        try{
            con.setAutoCommit(false);

            String bookSql = "SELECT available FROM books " +
                             "WHERE book_id = ? FOR UPDATE";

            boolean bookExists;
            boolean available = false;

            try(PreparedStatement ps = con.prepareStatement(bookSql)){

                ps.setInt(1, bookId);

                try(ResultSet rs = ps.executeQuery()){
                    bookExists = rs.next();

                    if(bookExists){
                        available = rs.getBoolean("available");
                    }
                }
            }

            if(!bookExists){
                con.rollback();
                System.out.println("Book ID not found.");
                return;
            }

            if(!available){
                con.rollback();
                System.out.println("Book is not available.");
                return;
            }

            String memberSql = "SELECT member_id FROM members WHERE member_id = ?";

            boolean memberExists;

            try(PreparedStatement ps = con.prepareStatement(memberSql)){

                ps.setInt(1, memberId);

                try(ResultSet rs = ps.executeQuery()){
                    memberExists = rs.next();
                }
            }

            if(!memberExists){
                con.rollback();
                System.out.println("Member ID not found.");
                return;
            }

            String issueSql = "INSERT INTO book_issues " +
                    		"(book_id, member_id, issue_date) " +
                    		"VALUES (?, ?, CURDATE())";

            try(PreparedStatement ps = con.prepareStatement(issueSql)){

                ps.setInt(1, bookId);
                ps.setInt(2, memberId);
                ps.executeUpdate();
            }

            String updateBookSql = "UPDATE books SET available = FALSE " +
                    				"WHERE book_id = ?";

            try (PreparedStatement ps = con.prepareStatement(updateBookSql)) {

                ps.setInt(1, bookId);
                ps.executeUpdate();
            }

            con.commit();
            System.out.println("Book issued successfully");

        }catch(SQLException e){
            try{
                con.rollback();
            } catch(SQLException rollbackError){
                e.addSuppressed(rollbackError);
            }

            System.out.println("Book issue failed. Changes rolled back.");
            System.out.println("Reason: " + e.getMessage());

        } finally {
            con.setAutoCommit(true);
        }
    }

    // 4. Return Book
    static void returnBook(Connection con, Scanner sc) throws SQLException{

        System.out.print("Enter book ID to return: ");
        int bookId = sc.nextInt();
        sc.nextLine();

        try {
            con.setAutoCommit(false);

            String issueSql = "SELECT issue_id FROM book_issues " +
                    			"WHERE book_id = ? AND return_date IS NULL " +
                    			"FOR UPDATE";

            int issueId = -1;

            try(PreparedStatement ps = con.prepareStatement(issueSql)){

                ps.setInt(1, bookId);

                try(ResultSet rs = ps.executeQuery()){
                    if(rs.next()){
                        issueId = rs.getInt("issue_id");
                    }
                }
            }

            if(issueId == -1) {
                con.rollback();
                System.out.println("No active issue record found for this book.");
                return;
            }

            String returnSql = "UPDATE book_issues SET return_date = CURDATE() " +
                    			"WHERE issue_id = ?";

            try(PreparedStatement ps = con.prepareStatement(returnSql)){

                ps.setInt(1, issueId);
                ps.executeUpdate();
            }

            String updateBookSql = "UPDATE books SET available = TRUE WHERE book_id = ?";

            try (PreparedStatement ps = con.prepareStatement(updateBookSql)) {

                ps.setInt(1, bookId);
                ps.executeUpdate();
            }

            con.commit();
            System.out.println("Book returned successfully!");

        }catch(SQLException e){
            try{
                con.rollback();
            }catch (SQLException rollbackError){
                e.addSuppressed(rollbackError);
            }

            System.out.println("Book return failed. Changes rolled back.");
            System.out.println("Reason: " + e.getMessage());

        }finally {
            con.setAutoCommit(true);
        }
    }

    // 5. View Issue Records
    static void viewIssueRecords(Connection con) throws SQLException {

        String sql = "SELECT bi.issue_id, b.title, m.member_name, " +
                	"bi.issue_date, bi.return_date " +
                	"FROM book_issues bi " +
                	"JOIN books b ON bi.book_id = b.book_id " +
                	"JOIN members m ON bi.member_id = m.member_id";

        try(Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            boolean found = false;

            while(rs.next()){
                found = true;

                System.out.println( "Issue ID: " + rs.getInt("issue_id") +
                        " | Book: " + rs.getString("title") +
                        " | Member: " + rs.getString("member_name") +
                        " | Issue Date: " + rs.getDate("issue_date") +
                        " | Return Date: " + rs.getDate("return_date"));
            }

            if(!found){
                System.out.println("No issue records found.");
            }
        }
    }
}
