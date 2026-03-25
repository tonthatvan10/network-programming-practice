import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ChatServer extends JFrame {
    private JTextArea txtLog;
    private static List<ClientHandler> clients = new ArrayList<>();
    private static int clientCount = 0;

    public ChatServer() {
        setTitle("SERVER CHAT ROOM - TCP PORT 6000");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setBackground(new Color(30, 30, 30));
        txtLog.setForeground(new Color(0, 255, 100));
        txtLog.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtLog.setMargin(new Insets(10, 10, 10, 10));

        add(new JScrollPane(txtLog));
        setVisible(true);

        startServer();
    }

    private void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(6000)) {
                log("Chat Server đã sẵn sàng tại cổng 6000...");
                while (true) {
                    Socket socket = serverSocket.accept();
                    clientCount++;
                    String clientName = "Người dùng " + clientCount;
                    
                    ClientHandler handler = new ClientHandler(socket, clientName);
                    clients.add(handler);
                    new Thread(handler).start();
                    
                    log(">>> " + clientName + " đã tham gia phòng.");
                    broadcast("Hệ thống: " + clientName + " đã tham gia đoạn chat!", null);
                }
            } catch (IOException e) {
                log("Lỗi Server: " + e.getMessage());
            }
        }).start();
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> txtLog.append(msg + "\n"));
    }

    // Gửi tin nhắn đến tất cả mọi người
    public static synchronized void broadcast(String msg, ClientHandler sender) {
        for (ClientHandler client : clients) {
            client.sendMessage(msg);
        }
    }

    // Xử lý khi một client rời đi
    public static synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    class ClientHandler implements Runnable {
        private Socket socket;
        private DataInputStream dis;
        private DataOutputStream dos;
        private String name;

        public ClientHandler(Socket socket, String name) {
            try {
                this.socket = socket;
                this.name = name;
                this.dis = new DataInputStream(socket.getInputStream());
                this.dos = new DataOutputStream(socket.getOutputStream());
                // Gửi tên định danh cho Client ngay khi kết nối
                dos.writeUTF(name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendMessage(String msg) {
            try {
                dos.writeUTF(msg);
                dos.flush();
            } catch (IOException e) {
                log("Lỗi gửi tin tới " + name);
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String msg = dis.readUTF();
                    if (msg.equalsIgnoreCase("exit")) break;
                    log(name + ": " + msg);
                    broadcast(name + ": " + msg, this);
                }
            } catch (IOException e) {
                // Kết nối bị ngắt đột ngột
            } finally {
                removeClient(this);
                log("!!! " + name + " đã rời phòng.");
                broadcast("Hệ thống: " + name + " đã rời khỏi cuộc trò chuyện.", null);
                try { socket.close(); } catch (IOException e) {}
            }
        }
    }

    public static void main(String[] args) { new ChatServer(); }
}