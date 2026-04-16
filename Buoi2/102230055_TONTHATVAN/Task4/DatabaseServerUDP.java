import javax.swing.*;
import java.awt.*;
import java.net.*;
import java.sql.*;

public class DatabaseServerUDP extends JFrame {
    private JTextArea txtLog;
    private String dbUrl = "jdbc:mysql://localhost:3306/QuanLySinhVien";
    private String dbUser = "root";
    private String dbPass = "Tonthatvan0406@";
    private final int PORT = 7000;

    public DatabaseServerUDP() {
        setTitle("UDP DATABASE SERVER - PORT " + PORT);
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
            // UDP sử dụng DatagramSocket thay vì ServerSocket
            try (DatagramSocket serverSocket = new DatagramSocket(PORT)) {
                log("UDP Server đang lắng nghe cổng " + PORT + "...");
                
                byte[] receiveBuffer = new byte[4096]; // Buffer để nhận gói tin

                while (true) {
                    // 1. Nhận gói tin từ Client
                    DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    serverSocket.receive(receivePacket); // Đợi cho đến khi có gói tin đến

                    // Trích xuất dữ liệu và thông tin Client
                    String query = new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8").trim();
                    InetAddress clientAddress = receivePacket.getAddress();
                    int clientPort = receivePacket.getPort();

                    log(">>> Nhận từ [" + clientAddress.getHostAddress() + ":" + clientPort + "]: " + query);

                    // 2. Xử lý logic SQL và gửi phản hồi ngay lập tức
                    String response = handleDatabaseQuery(query);
                    
                    byte[] sendData = response.getBytes("UTF-8");
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                    serverSocket.send(sendPacket);
                }
            } catch (Exception e) {
                log("Lỗi Server: " + e.getMessage());
            }
        }).start();
    }

    private String handleDatabaseQuery(String query) {
        if (query.equalsIgnoreCase("exit")) return "Tạm biệt!";

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
                return sb.length() == 0 ? "Không có dữ liệu." : sb.toString();
            } else {
                int count = stmt.executeUpdate(query);
                return "Thành công! Số dòng bị ảnh hưởng: " + count;
            }
        } catch (SQLException e) {
            return "Lỗi SQL: " + e.getMessage();
        }
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            txtLog.append(msg + "\n");
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Không tìm thấy Driver MySQL!");
        }
        new DatabaseServerUDP();
    }}