import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class UDP_Server extends JFrame {
    private JTextArea txtLog;
    private DatagramSocket serverSocket;
    private static final int PORT = 43;

    public UDP_Server() {
        setTitle("SERVER UDP GIÁM SÁT - PORT 43");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setBackground(new Color(25, 25, 25));
        txtLog.setForeground(Color.WHITE);
        txtLog.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtLog.setMargin(new Insets(10, 10, 10, 10));

        add(new JScrollPane(txtLog));
        setVisible(true);

        startServer();
    }

    private void startServer() {
        new Thread(() -> {
            try {
                // UDP sử dụng DatagramSocket thay vì ServerSocket
                serverSocket = new DatagramSocket(PORT);
                log("Server UDP đang khởi động...");
                log("Đang lắng nghe gói tin tại cổng " + PORT + "...");

                byte[] receiveData = new byte[1024];

                while (true) {
                    // Chuẩn bị gói tin để nhận
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket); // Đợi nhận gói tin từ Client

                    // Lấy dữ liệu từ gói tin
                    String input = new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8").trim();
                    
                    // Lấy thông tin Client (để gửi phản hồi ngược lại)
                    InetAddress clientAddress = receivePacket.getAddress();
                    int clientPort = receivePacket.getPort();
                    String clientID = clientAddress.toString() + ":" + clientPort;

                    log(">>> Nhận từ [" + clientID + "]: " + input);

                    if (input.equalsIgnoreCase("exit")) {
                        log("!!! Client " + clientID + " yêu cầu thoát.");
                        continue; 
                    }

                    // Xử lý logic chuỗi (Giữ nguyên các hàm của bạn)
                    String response = "1. Đảo ngược: " + myReverse(input) + "\n"
                                    + "2. In hoa: " + myToUpperCase(input) + "\n"
                                    + "3. In thường: " + myToLowerCase(input) + "\n"
                                    + "4. Hoa-Thường: " + myMixCase(input) + "\n"
                                    + "5. Thống kê: " + myStatistics(input);

                    // Đóng gói và gửi trả lại cho Client
                    byte[] sendData = response.getBytes("UTF-8");
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                    serverSocket.send(sendPacket);
                }
            } catch (IOException e) {
                log("Lỗi Server: " + e.getMessage());
            }
        }).start();
    }

    // --- CÁC HÀM LOGIC XỬ LÝ CHUỖI (GIỮ NGUYÊN TỪ CODE TCP CỦA BẠN) ---

    private String myReverse(String s) {
        String res = "";
        for (int i = s.length() - 1; i >= 0; i--) res += s.charAt(i);
        return res;
    }

    private String myToUpperCase(String s) {
        char[] c = s.toCharArray();
        for (int i = 0; i < c.length; i++) 
            if (c[i] >= 'a' && c[i] <= 'z') c[i] = (char)(c[i] - 32);
        return new String(c);
    }

    private String myToLowerCase(String s) {
        char[] c = s.toCharArray();
        for (int i = 0; i < c.length; i++) 
            if (c[i] >= 'A' && c[i] <= 'Z') c[i] = (char)(c[i] + 32);
        return new String(c);
    }

    private String myMixCase(String s) {
        char[] c = s.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (i % 2 == 0) {
                if (c[i] >= 'a' && c[i] <= 'z') c[i] = (char)(c[i] - 32);
            } else {
                if (c[i] >= 'A' && c[i] <= 'Z') c[i] = (char)(c[i] + 32);
            }
        }
        return new String(c);
    }

    private String myStatistics(String s) {
        int words = 0, vowels = 0;
        boolean inWord = false;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            char low = (ch >= 'A' && ch <= 'Z') ? (char)(ch+32) : ch;
            if (low == 'a' || low == 'e' || low == 'i' || low == 'o' || low == 'u') vowels++;
            if (ch != ' ' && ch != '\t') {
                if (!inWord) { words++; inWord = true; }
            } else inWord = false;
        }
        return "Số từ: " + words + " | Nguyên âm: " + vowels;
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> txtLog.append(msg + "\n"));
    }

    public static void main(String[] args) { new UDP_Server(); }
}