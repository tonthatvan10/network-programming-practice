import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class Server extends JFrame {
    private JTextArea txtLog;
    private static int clientCount = 0;

    public Server() {
        setTitle("SERVER GIÁM SÁT - PORT 43");
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
            try (ServerSocket serverSocket = new ServerSocket(43)) {
                log("Server đang khởi động...");
                log("Đang lắng nghe kết nối tại cổng 43...");

                while (true) {
                    Socket socket = serverSocket.accept();
                    clientCount++;
                    String clientID = "Máy " + clientCount;
                    
                    log(">>> " + clientID + " đã kết nối.");
                    
                    new Thread(() -> handleClient(socket, clientID)).start();
                }
            } catch (IOException e) {
                log("Lỗi Server: " + e.getMessage());
            }
        }).start();
    }

    private void handleClient(Socket socket, String clientID) {
        try (DataInputStream dis = new DataInputStream(socket.getInputStream());
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

            dos.writeUTF(clientID);
            dos.flush();

            while (true) {
                String input = dis.readUTF();
                
                log(clientID + ": " + input);

                if (input.equalsIgnoreCase("exit")) break;

                String response = "1. Đảo ngược: " + myReverse(input) + "\n"
                                + "2. In hoa: " + myToUpperCase(input) + "\n"
                                + "3. In thường: " + myToLowerCase(input) + "\n"
                                + "4. Hoa-Thường: " + myMixCase(input) + "\n"
                                + "5. Thống kê: " + myStatistics(input);

                dos.writeUTF(response);
                dos.flush();
            }
        } catch (IOException e) {
            log("!!! " + clientID + " đã ngắt kết nối.");
        }
    }

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

    public static void main(String[] args) { new Server(); }
}