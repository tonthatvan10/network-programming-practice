import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Set;

public class UDP_ChatServer extends JFrame {
    private JTextArea txtLog;
    private DatagramSocket serverSocket;
    // Lưu danh sách các Client dựa trên định danh Address + Port
    private static Set<ClientInfo> clients = new HashSet<>();
    private final int PORT = 6000;

    public UDP_ChatServer() {
        setTitle("SERVER CHAT ROOM - UDP PORT 6000");
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
            try {
                serverSocket = new DatagramSocket(PORT);
                log("UDP Chat Server đã sẵn sàng tại cổng " + PORT + "...");

                byte[] receiveBuffer = new byte[4096];

                while (true) {
                    DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    serverSocket.receive(receivePacket);

                    String msg = new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8").trim();
                    InetAddress address = receivePacket.getAddress();
                    int port = receivePacket.getPort();
                    
                    ClientInfo currentClient = new ClientInfo(address, port);

                    // Nếu là Client mới, thêm vào danh sách và thông báo
                    if (!clients.contains(currentClient)) {
                        clients.add(currentClient);
                        String welcomeMsg = "Hệ thống: Một người dùng mới đã tham gia (" + port + ")";
                        log(">>> " + welcomeMsg);
                        broadcast(welcomeMsg, null);
                    }

                    if (msg.equalsIgnoreCase("exit")) {
                        clients.remove(currentClient);
                        String leaveMsg = "Hệ thống: Người dùng tại cổng " + port + " đã rời phòng.";
                        log("!!! " + leaveMsg);
                        broadcast(leaveMsg, null);
                    } else {
                        log(port + ": " + msg);
                        broadcast(port + ": " + msg, currentClient);
                    }
                }
            } catch (IOException e) {
                log("Lỗi Server: " + e.getMessage());
            }
        }).start();
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> txtLog.append(msg + "\n"));
    }

    // Gửi tin nhắn tới tất cả mọi người trong danh sách
    public void broadcast(String msg, ClientInfo sender) {
        byte[] sendData;
        try {
            sendData = msg.getBytes("UTF-8");
            for (ClientInfo client : clients) {
                // Có thể bỏ qua người gửi nếu muốn (sender)
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, client.address, client.port);
                serverSocket.send(sendPacket);
            }
        } catch (IOException e) {
            log("Lỗi broadcast: " + e.getMessage());
        }
    }

    // Lớp hỗ trợ lưu trữ định danh Client
    class ClientInfo {
        InetAddress address;
        int port;

        public ClientInfo(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ClientInfo) {
                ClientInfo other = (ClientInfo) obj;
                return this.address.equals(other.address) && this.port == other.port;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return address.hashCode() + port;
        }
    }

    public static void main(String[] args) { new UDP_ChatServer(); }
}