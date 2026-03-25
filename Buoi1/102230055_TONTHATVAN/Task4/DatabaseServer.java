import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.sql.*;

public class DatabaseServer extends JFrame {
    private JTextArea txtLog;
    // Đảm bảo thông tin này khớp với MySQL Workbench
    private String dbUrl = "jdbc:mysql://localhost:3306/QuanLySinhVien";
    private String dbUser = "root";
    private String dbPass = "Tonthatvan0406@"; 

    public DatabaseServer() {
        setTitle("DATABASE SERVER - PORT 7000");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setBackground(new Color(20, 30, 40));
        txtLog.setForeground(Color.CYAN);
        txtLog.setFont(new Font("Consolas", Font.PLAIN, 13));
        add(new JScrollPane(txtLog));
        
        setVisible(true);
        startServer();
    }

    private void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(7000)) {
                log("Server đang lắng nghe cổng 7000...");
                while (true) {
                    Socket socket = serverSocket.accept();
                    new Thread(() -> handleClient(socket)).start();
                }
            } catch (IOException e) {
                log("Lỗi Server: " + e.getMessage());
            }
        }).start();
    }

    private void handleClient(Socket socket) {
        try (DataInputStream dis = new DataInputStream(socket.getInputStream());
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

            log(">>> Client [" + socket.getInetAddress().getHostAddress() + "] đã kết nối.");
            
            while (true) {
                String query = dis.readUTF();
                if (query.equalsIgnoreCase("exit")) break;
                log("Thực thi SQL: " + query);

                // Mở kết nối Database cho mỗi lần truy vấn để đảm bảo không bị timeout
                try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
                    Statement stmt = conn.createStatement();
                    
                    if (query.toLowerCase().trim().startsWith("select")) {
                        ResultSet rs = stmt.executeQuery(query);
                        ResultSetMetaData rsmd = rs.getMetaData();
                        int cols = rsmd.getColumnCount();
                        
                        StringBuilder sb = new StringBuilder();
                        while (rs.next()) {
                            for (int i = 1; i <= cols; i++) {
                                sb.append(rs.getString(i)).append(" | ");
                            }
                            sb.append("\n");
                        }
                        dos.writeUTF(sb.length() == 0 ? "Không có dữ liệu." : sb.toString());
                    } else {
                        int count = stmt.executeUpdate(query);
                        dos.writeUTF("Thành công! Số dòng bị ảnh hưởng: " + count);
                    }
                } catch (SQLException e) {
                    dos.writeUTF("Lỗi SQL: " + e.getMessage());
                }
                dos.flush();
            }
        } catch (Exception e) {
            log("Ngắt kết nối với một Client.");
        }
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            txtLog.append(msg + "\n");
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        // ÉP JAVA NẠP DRIVER TỪ THƯ VIỆN
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Không tìm thấy Driver trong thư mục libs!");
        }
        new DatabaseServer();
    }
}