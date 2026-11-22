
import java.awt.*;
import java.sql.*;
import java.util.Arrays;
import javax.swing.*;

public class LoginForm extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginForm() {
        setTitle("Student Attendance - Login");
        setSize(400, 320);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        showLogin();
    }

    private void showLogin() {

        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setBorder(
            BorderFactory.createEmptyBorder(30, 40, 30, 40)
        );

        JLabel title = new JLabel(
            "STUDENT ATTENDANCE SYSTEM",
            SwingConstants.CENTER
        );
        title.setFont(new Font("Arial", Font.BOLD, 16));

        usernameField = new JTextField();
        passwordField = new JPasswordField();

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        JPanel buttons = new JPanel();
        buttons.add(loginButton);
        buttons.add(registerButton);

        panel.add(title);
        panel.add(createField("Username:", usernameField));
        panel.add(createField("Password:", passwordField));
        panel.add(buttons);

        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> register());

        setContentPane(panel);
    }

    private JPanel createField(String label, Component field) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel(label), BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private void login() {

        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();

        if (username.isEmpty() || password.length == 0) {
            JOptionPane.showMessageDialog(
                this, "Please enter username and password."
            );
            Arrays.fill(password, '\0');
            return;
        }

        String sql =
            "SELECT salt, password_hash FROM users WHERE username = ?";

        try (Connection con = DbConn.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, username);

            try (ResultSet rs = pst.executeQuery()) {

                if (rs.next()) {

                    byte[] salt = rs.getBytes("salt");
                    byte[] storedHash = rs.getBytes("password_hash");

                    boolean valid = PasswordUtil.verifyPassword(
                        new String(password), salt, storedHash
                    );

                    if (valid) {
                        JOptionPane.showMessageDialog(
                            this, "Login successful!"
                        );

                        dispose();

                        SwingUtilities.invokeLater(() ->
                            new StudentAttendanceMonitoringSystem()
                                .setVisible(true)
                        );

                    } else {
                        JOptionPane.showMessageDialog(
                            this, "Incorrect username or password."
                        );
                    }

                } else {
                    JOptionPane.showMessageDialog(
                        this, "Incorrect username or password."
                    );
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this, "Login error: " + e.getMessage()
            );
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private void register() {

        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();

        if (username.isEmpty() || password.length == 0) {
            JOptionPane.showMessageDialog(
                this, "Please enter a username and password."
            );
            Arrays.fill(password, '\0');
            return;
        }

        if (username.length() > 50 || password.length < 8) {
            JOptionPane.showMessageDialog(
                this,
                "Username must be at most 50 characters.\n"
                + "Password must contain at least 8 characters."
            );
            Arrays.fill(password, '\0');
            return;
        }

        String sql =
            "INSERT INTO users (username, salt, password_hash) "
            + "VALUES (?, ?, ?)";

        try {

            byte[] salt = PasswordUtil.generateSalt();

            byte[] hash = PasswordUtil.hashPassword(
                new String(password), salt
            );

            try (Connection con = DbConn.getConnection();
                 PreparedStatement pst = con.prepareStatement(sql)) {

                pst.setString(1, username);
                pst.setBytes(2, salt);
                pst.setBytes(3, hash);

                pst.executeUpdate();

                JOptionPane.showMessageDialog(
                    this, "Registration successful! You can now log in."
                );

                passwordField.setText("");
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            JOptionPane.showMessageDialog(
                this, "Username already exists."
            );

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this, "Registration error: " + e.getMessage()
            );

        } finally {
            Arrays.fill(password, '\0');
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new LoginForm().setVisible(true)
        );
    }
}
