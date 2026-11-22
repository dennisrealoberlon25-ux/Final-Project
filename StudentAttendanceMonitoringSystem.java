import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.*;
import java.util.ArrayList;
import javax.swing.*;

public class StudentAttendanceMonitoringSystem extends JFrame {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MM/dd/uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);

    // Format: LASTNAME, FIRSTNAME M.   (e.g. DELA CRUZ, JUAN R.)
    private static final String NAME_REGEX =
            "[A-Z]+(?: [A-Z]+)*, [A-Z]+(?: [A-Z]+)* [A-Z]\\.";

    private JTextField idField, nameField, addDateField, attendanceDateField;
    private JComboBox<String> addStatusBox, attendanceStatusBox, studentBox;
    private JTextArea recordsArea;

    // STUDENT CLASS
    class Student {
        String studentId, name, block, course, year;
        ArrayList<String> attendance = new ArrayList<>();

        Student(String id, String name, String block, String course, String year) {
            this.studentId = id;
            this.name = name;
            this.block = block;
            this.course = course;
            this.year = year;
        }

        String getStudentInfo() {
            return "Student ID : " + studentId
                    + "\nName       : " + name
                    + "\nBlock      : " + block
                    + "\nProgram    : " + course
                    + "\nYear       : " + year;
        }

        String getAttendanceInfo() {
            int present = 0, absent = 0;
            String result = "Attendance Record:\n";

            if (attendance.isEmpty()) {
                result += "No attendance record yet.\n";
            } else {
                for (String record : attendance) {
                    result += record + "\n";

                    if (record.endsWith("Present")) {
                        present++;
                    } else if (record.endsWith("Absent")) {
                        absent++;
                    }
                }
            }

            return result + "\nTotal Present: " + present
                    + "\nTotal Absent : " + absent;
        }
    }

    // MAIN CONSTRUCTOR
    public StudentAttendanceMonitoringSystem() {
        setTitle("Student Attendance Monitoring System");
        setSize(720, 570);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        showMenu();
    }

    // SHOW A SCREEN
    private void show(JPanel panel) {
        setContentPane(panel);
        revalidate();
        repaint();
    }

    // MAIN MENU
    private void showMenu() {
        JPanel panel = new JPanel(new GridLayout(7, 1, 12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(35, 45, 35, 45));

        JLabel title = new JLabel(
                "STUDENT ATTENDANCE MONITORING SYSTEM",
                SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));

        JButton addButton = new JButton("Add Student");
        JButton recordsButton = new JButton("View Student Records");
        JButton attendanceButton = new JButton("Mark Attendance");
        JButton updateButton = new JButton("Update Student");
        JButton deleteButton = new JButton("Delete Student");
        JButton exitButton = new JButton("Exit");

        addButton.addActionListener(e -> showAddStudent());
        recordsButton.addActionListener(e -> showRecords());
        attendanceButton.addActionListener(e -> showAttendance());
        updateButton.addActionListener(e -> showUpdateStudent());
        deleteButton.addActionListener(e -> deleteStudent());
        exitButton.addActionListener(e -> System.exit(0));

        panel.add(title);
        panel.add(addButton);
        panel.add(recordsButton);
        panel.add(attendanceButton);
        panel.add(updateButton);
        panel.add(deleteButton);
        panel.add(exitButton);

        show(panel);
    }

    // ADD STUDENT FORM
    private void showAddStudent() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));

        JLabel title = new JLabel("ADD STUDENT", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));

        JPanel form = new JPanel(new GridLayout(7, 2, 8, 8));

        idField = new JTextField();
        nameField = new JTextField();
        addDateField = new JTextField();
        addStatusBox = new JComboBox<>(new String[]{"Present", "Absent"});

        addField(form, "Student ID (2025-01-12345):", idField);
        addField(form, "Name (DELA CRUZ, JUAN R.):", nameField);
        addField(form, "First Attendance Date (MM/DD/YYYY):", addDateField);
        addField(form, "First Attendance Status:", addStatusBox);
        addField(form, "Block:", new JLabel("BLOCK A"));
        addField(form, "Program/Course:", new JLabel("BSIT"));
        addField(form, "Year:", new JLabel("2ND YEAR"));

        JButton saveButton = new JButton("Save Student");
        JButton backButton = new JButton("Back");

        saveButton.addActionListener(e -> addStudent());
        backButton.addActionListener(e -> showMenu());

        JPanel buttons = new JPanel();
        buttons.add(saveButton);
        buttons.add(backButton);

        panel.add(title, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        show(panel);
    }

    // ADD STUDENT
    private void addStudent() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim().toUpperCase();
        String date = addDateField.getText().trim();

        if (!id.matches("2025-\\d{2}-\\d{5}")) {
            showMessage("Invalid Student ID.\nRequired format: 2025-01-12345");
            return;
        }

        if (!name.matches(NAME_REGEX)) {
            showMessage("Invalid name format.\nExample: DELA CRUZ, JUAN R.");
            return;
        }

        if (date.isEmpty() || !validDate(date)) {
            return;
        }

        String status = (String) addStatusBox.getSelectedItem();

        Student student = new Student(id, name, "BLOCK A", "BSIT", "2ND YEAR");

        if (!saveStudentToDatabase(student, date, status)) {
            return;
        }

        showMessage("Student saved successfully!\n\n"
                + student.getStudentInfo()
                + "\n\nFirst attendance: " + date + " - " + status);

        showMenu();
    }

    // SAVE STUDENT + FIRST ATTENDANCE TO DATABASE
    private boolean saveStudentToDatabase(Student student, String date, String status) {

        String studentSQL = "INSERT INTO student "
                + "(student_id, name, block, course, year_level) "
                + "VALUES (?, ?, ?, ?, ?)";

        String attendanceSQL = "INSERT INTO attendance "
                + "(student_id, attendance_date, status) "
                + "VALUES (?, ?, ?)";

        try (Connection con = DbConn.getConnection()) {

            con.setAutoCommit(false);

            try (PreparedStatement studentPst = con.prepareStatement(studentSQL);
                 PreparedStatement attendancePst = con.prepareStatement(attendanceSQL)) {

                studentPst.setString(1, student.studentId);
                studentPst.setString(2, student.name);
                studentPst.setString(3, student.block);
                studentPst.setString(4, student.course);
                studentPst.setString(5, student.year);
                studentPst.executeUpdate();

                attendancePst.setString(1, student.studentId);
                attendancePst.setDate(2, Date.valueOf(LocalDate.parse(date, DATE_FORMAT)));
                attendancePst.setString(3, status);
                attendancePst.executeUpdate();

                con.commit();
                return true;

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }

        } catch (SQLException e) {
            // SQLState 23xxx = integrity constraint violation (duplicate ID)
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                showMessage("Student ID already exists.");
            } else {
                showMessage("Database Error: " + e.getMessage());
            }
            return false;
        }
    }

    // DELETE STUDENT
    private void deleteStudent() {
        String studentId = JOptionPane.showInputDialog(
                this, "Enter Student ID to delete:");

        if (studentId == null || studentId.trim().isEmpty()) {
            return;
        }

        studentId = studentId.trim();

        try (Connection con = DbConn.getConnection()) {

            String studentName;

            try (PreparedStatement pst = con.prepareStatement(
                    "SELECT name FROM student WHERE student_id = ?")) {
                pst.setString(1, studentId);

                try (ResultSet rs = pst.executeQuery()) {
                    if (!rs.next()) {
                        showMessage("Student not found.");
                        return;
                    }
                    studentName = rs.getString("name");
                }
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Delete " + studentName + " and all attendance records?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            con.setAutoCommit(false);

            try (PreparedStatement deleteAtt = con.prepareStatement(
                    "DELETE FROM attendance WHERE student_id = ?");
                 PreparedStatement deleteStu = con.prepareStatement(
                    "DELETE FROM student WHERE student_id = ?")) {

                deleteAtt.setString(1, studentId);
                deleteAtt.executeUpdate();

                deleteStu.setString(1, studentId);
                deleteStu.executeUpdate();

                con.commit();

                showMessage("Student deleted successfully!");
                showMenu();

            } catch (SQLException ex) {
                con.rollback();
                showMessage("Delete failed: " + ex.getMessage());
            }

        } catch (SQLException ex) {
            showMessage("Database Error: " + ex.getMessage());
        }
    }

    // UPDATE STUDENT FORM
    private void showUpdateStudent() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));

        JLabel title = new JLabel("UPDATE STUDENT", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));

        JComboBox<String> updateStudentBox = new JComboBox<>();
        JTextField updateNameField = new JTextField();

        loadStudentChoices(updateStudentBox);

        addField(form, "Select Student:", updateStudentBox);
        addField(form, "Student Name:", updateNameField);
        addField(form, "Block:", new JLabel("BLOCK A"));
        addField(form, "Program/Course:", new JLabel("BSIT"));
        addField(form, "Year:", new JLabel("2ND YEAR"));

        updateStudentBox.addActionListener(e ->
                loadNameInto(updateStudentBox, updateNameField));

        // Load the name of the first student right away
        loadNameInto(updateStudentBox, updateNameField);

        JButton saveButton = new JButton("Save Changes");
        JButton backButton = new JButton("Back");

        saveButton.addActionListener(e -> {
            String selected = (String) updateStudentBox.getSelectedItem();

            if (selected == null) {
                showMessage("Please select a student.");
                return;
            }

            String id = extractId(selected);
            String newName = updateNameField.getText().trim().toUpperCase();

            if (!newName.matches(NAME_REGEX)) {
                showMessage("Invalid name format.\nExample: DELA CRUZ, JUAN R.");
                return;
            }

            String sql = "UPDATE student SET name = ? WHERE student_id = ?";

            try (Connection con = DbConn.getConnection();
                 PreparedStatement pst = con.prepareStatement(sql)) {

                pst.setString(1, newName);
                pst.setString(2, id);
                pst.executeUpdate();

                showMessage("Student information updated successfully!");
                showMenu();

            } catch (SQLException ex) {
                showMessage("Database Error: " + ex.getMessage());
            }
        });

        backButton.addActionListener(e -> showMenu());

        JPanel buttons = new JPanel();
        buttons.add(saveButton);
        buttons.add(backButton);

        panel.add(title, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        show(panel);
    }

    // LOAD SELECTED STUDENT'S NAME INTO TEXT FIELD
    private void loadNameInto(JComboBox<String> box, JTextField field) {
        String selected = (String) box.getSelectedItem();

        if (selected == null) {
            field.setText("");
            return;
        }

        String sql = "SELECT name FROM student WHERE student_id = ?";

        try (Connection con = DbConn.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, extractId(selected));

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    field.setText(rs.getString("name"));
                }
            }

        } catch (SQLException ex) {
            showMessage("Database Error: " + ex.getMessage());
        }
    }

    // MARK ATTENDANCE FORM
    private void showAttendance() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));

        JLabel title = new JLabel("MARK ATTENDANCE", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));

        studentBox = new JComboBox<>();
        attendanceDateField = new JTextField();
        attendanceStatusBox = new JComboBox<>(new String[]{"Present", "Absent"});

        loadStudentChoices(studentBox);

        addField(form, "Select Student:", studentBox);
        addField(form, "Attendance Date (MM/DD/YYYY):", attendanceDateField);
        addField(form, "Attendance Status:", attendanceStatusBox);

        JButton saveButton = new JButton("Save Attendance");
        JButton backButton = new JButton("Back");

        saveButton.addActionListener(e -> markAttendance());
        backButton.addActionListener(e -> showMenu());

        JPanel buttons = new JPanel();
        buttons.add(saveButton);
        buttons.add(backButton);

        panel.add(title, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        show(panel);
    }

    // MARK ATTENDANCE
    private void markAttendance() {
        String selectedStudent = (String) studentBox.getSelectedItem();

        if (selectedStudent == null) {
            showMessage("Please select a student.");
            return;
        }

        String id = extractId(selectedStudent);
        String date = attendanceDateField.getText().trim();

        if (date.isEmpty() || !validDate(date)) {
            return;
        }

        String status = (String) attendanceStatusBox.getSelectedItem();

        String checkSQL = "SELECT COUNT(*) FROM attendance "
                + "WHERE student_id = ? AND attendance_date = ?";

        String insertSQL = "INSERT INTO attendance "
                + "(student_id, attendance_date, status) "
                + "VALUES (?, ?, ?)";

        try (Connection con = DbConn.getConnection()) {

            Date sqlDate = Date.valueOf(LocalDate.parse(date, DATE_FORMAT));

            try (PreparedStatement checkPst = con.prepareStatement(checkSQL)) {
                checkPst.setString(1, id);
                checkPst.setDate(2, sqlDate);

                try (ResultSet rs = checkPst.executeQuery()) {
                    rs.next();

                    if (rs.getInt(1) > 0) {
                        showMessage("This student already has an attendance record for "
                                + date + ".");
                        return;
                    }
                }
            }

            try (PreparedStatement pst = con.prepareStatement(insertSQL)) {
                pst.setString(1, id);
                pst.setDate(2, sqlDate);
                pst.setString(3, status);
                pst.executeUpdate();
            }

            showMessage("Attendance marked as " + status.toUpperCase() + ".");
            showMenu();

        } catch (SQLException e) {
            showMessage("Database Error: " + e.getMessage());
        }
    }

    // VIEW STUDENT RECORDS
    private void showRecords() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));

        JLabel title = new JLabel("ALL STUDENT RECORDS", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));

        recordsArea = new JTextArea();
        recordsArea.setEditable(false);
        recordsArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JButton refreshButton = new JButton("Refresh");
        JButton backButton = new JButton("Back");

        refreshButton.addActionListener(e -> displayRecords());
        backButton.addActionListener(e -> showMenu());

        JPanel buttons = new JPanel();
        buttons.add(refreshButton);
        buttons.add(backButton);

        panel.add(title, BorderLayout.NORTH);
        panel.add(new JScrollPane(recordsArea), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        show(panel);

        displayRecords();
    }

    // DISPLAY ALL RECORDS
    private void displayRecords() {
        String sql = "SELECT s.student_id, s.name, s.block, "
                + "s.course, s.year_level, "
                + "a.attendance_date, a.status "
                + "FROM student s "
                + "LEFT JOIN attendance a "
                + "ON s.student_id = a.student_id "
                + "ORDER BY s.name, a.attendance_date";

        StringBuilder output = new StringBuilder();
        String currentId = "";

        try (Connection con = DbConn.getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            Student student = null;

            while (rs.next()) {
                String id = rs.getString("student_id");

                if (!id.equals(currentId)) {
                    if (student != null) {
                        output.append(student.getStudentInfo()).append("\n\n")
                              .append(student.getAttendanceInfo()).append("\n\n");
                    }

                    student = new Student(
                            id,
                            rs.getString("name"),
                            rs.getString("block"),
                            rs.getString("course"),
                            rs.getString("year_level"));

                    currentId = id;
                }

                Date attendanceDate = rs.getDate("attendance_date");

                if (attendanceDate != null) {
                    student.attendance.add(
                            attendanceDate.toLocalDate().format(DATE_FORMAT)
                            + " - " + rs.getString("status"));
                }
            }

            if (student != null) {
                output.append(student.getStudentInfo()).append("\n\n")
                      .append(student.getAttendanceInfo()).append("\n\n");
            }

            if (output.length() == 0) {
                output.append("No student records found.");
            }

        } catch (SQLException e) {
            showMessage("Database Error: " + e.getMessage());
            return;
        }

        recordsArea.setText(output.toString());
        recordsArea.setCaretPosition(0);
    }

    // LOAD STUDENT LIST FROM DATABASE INTO A COMBO BOX
    private void loadStudentChoices(JComboBox<String> box) {
        box.removeAllItems();

        String sql = "SELECT student_id, name FROM student ORDER BY name";

        try (Connection con = DbConn.getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                box.addItem(rs.getString("name")
                        + " (" + rs.getString("student_id") + ")");
            }

        } catch (SQLException e) {
            showMessage("Unable to load students: " + e.getMessage());
        }
    }

    // GET STUDENT ID FROM "NAME (ID)"
    private String extractId(String selected) {
        return selected.substring(
                selected.lastIndexOf("(") + 1,
                selected.lastIndexOf(")"));
    }

    // ADD LABEL AND INPUT FIELD
    private void addField(JPanel panel, String label, Component field) {
        panel.add(new JLabel(label));
        panel.add(field);
    }

    // DISPLAY MESSAGE
    private void showMessage(String message) {
        JDialog dialog = new JDialog(this, "Student Attendance System", true);
        dialog.setLayout(new BorderLayout(10, 10));

        // HTML so that \n shows as a real line break
        JLabel label = new JLabel(
                "<html>" + message.replace("\n", "<br>") + "</html>");
        label.setBorder(BorderFactory.createEmptyBorder(18, 20, 8, 20));

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);

        dialog.add(label, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // DATE VALIDATION
    private boolean validDate(String date) {
        try {
            LocalDate.parse(date, DATE_FORMAT);
            return true;
        } catch (Exception e) {
            showMessage("Invalid date. Please use MM/DD/YYYY.");
            return false;
        }
    }

    // MAIN METHOD
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new StudentAttendanceMonitoringSystem().setVisible(true));
    }
}
