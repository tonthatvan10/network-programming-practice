import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.Vector;

public class ChatServer extends JFrame {
    private JTextArea txtLog;
    private static Vector<ClientHandler> clients = new Vector<>();
    private static int clientCount = 0;

    public ChatServer() {
        setTitle("SERVER CHAT ROOM - PORT 6000");
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
            try (ServerSocket serverSocket = new ServerSocket(6000)) {
                log("Server đang lắng nghe cổng 6000...");
                while (true) {
                    Socket socket = serverSocket.accept();
                    clientCount++;
                    String name = "Người dùng " + clientCount;
                    
                    ClientHandler handler = new ClientHandler(socket, name);
                    clients.add(handler);
                    new Thread(handler).start();
                    
                    log(name + " đã kết nối.");
                    broadcast("Hệ thống: " + name + " đã tham gia phòng chat.");
                }
            } catch (IOException e) {
                log("Lỗi Server: " + e.getMessage());
            }
        }).start();
    }

    public void log(String msg) {
        SwingUtilities.invokeLater(() -> txtLog.append(msg + "\n"));
    }

    public synchronized void broadcast(String msg) {
        for (ClientHandler client : clients) {
            client.send(msg);
        }
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
                dos.writeUTF(name);
                dos.flush();
            } catch (IOException e) {}
        }

        public void send(String msg) {
            try {
                dos.writeUTF(msg);
                dos.flush();
            } catch (IOException e) {}
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String msg = dis.readUTF();
                    broadcast(name + ": " + msg);
                }
            } catch (IOException e) {
                clients.remove(this);
                log(name + " đã thoát.");
                broadcast("Hệ thống: " + name + " đã rời phòng.");
            }
        }
    }

    public static void main(String[] args) { new ChatServer(); }
}