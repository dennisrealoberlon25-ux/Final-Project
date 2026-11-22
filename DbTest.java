
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DbTest {

    public static void main(String[] args) {

        String sql = """
            SELECT s.student_id, s.name,
                   a.attendance_date, a.status
            FROM student s
            INNER JOIN attendance a
            ON s.student_id = a.student_id
            ORDER BY s.name, a.attendance_date
            """;

        try (
            Connection con = DbConn.getConnection();
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery()
        ) {

            System.out.println("ATTENDANCE RECORDS");

            while (rs.next()) {
                System.out.println("Student ID: " + rs.getString("student_id"));
                System.out.println("Name: " + rs.getString("name"));
                System.out.println("Date: " + rs.getDate("attendance_date"));
                System.out.println("Status: " + rs.getString("status"));
                System.out.println("------------------------");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
