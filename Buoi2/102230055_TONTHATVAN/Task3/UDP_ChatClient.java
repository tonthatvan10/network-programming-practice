import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class UDP_ChatClient extends JFrame {
    private JTextField txtInput;
    private JTextArea txtChat;
    private String myName = "Người dùng UDP";
    
    // Thành phần UDP
    private DatagramSocket clientSocket;
    private InetAddress serverAddress;
    private final int PORT = 6000;

    public UDP_ChatClient() {
        setTitle("Chat Room UDP");
        setSize(450, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        txtChat = new JTextArea();
        txtChat.setEditable(false);
        txtChat.setLineWrap(true);
        txtChat.setFont(new Font("Arial", Font.PLAIN, 14));
        
        txtInput = new JTextField();
        JButton btnSend = new JButton("Gửi");

        JPanel pnlBottom = new JPanel(new BorderLayout(5, 5));
        pnlBottom.add(txtInput, BorderLayout.CENTER);
        pnlBottom.add(btnSend, BorderLayout.EAST);

        add(new JScrollPane(txtChat), BorderLayout.CENTER);
        add(pnlBottom, BorderLayout.SOUTH);

        setVisible(true); 

        // Khởi tạo UDP và bắt đầu luồng lắng nghe
        initUDP();

        btnSend.addActionListener(e -> send());
        txtInput.addActionListener(e -> send());
    }

    private void initUDP() {
        try {
            clientSocket = new DatagramSocket();
            serverAddress = InetAddress.getByName("localhost");
            
            // Tên của bạn sẽ được định danh bằng Port mà OS cấp cho socket này
            myName = "User:" + clientSocket.getLocalPort();
            setTitle("Chat Room - " + myName);
            
            // Gửi một tin nhắn "Chào sân" để Server ghi nhận địa chỉ của bạn
            sendRawMessage("Hệ thống: " + myName + " đã online.");

            // Luồng lắng nghe tin nhắn từ Server (Broadcast)
            new Thread(() -> {
                try {
                    byte[] receiveBuffer = new byte[1024];
                    while (true) {
                        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        clientSocket.receive(receivePacket); // Đợi tin nhắn từ Server bắn về

                        String msg = new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8");
                        
                        SwingUtilities.invokeLater(() -> {
                            txtChat.append(msg + "\n");
                            txtChat.setCaretPosition(txtChat.getDocument().getLength());
                        });
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> txtChat.append(">> LỖI: Mất kết nối UDP!\n"));
                }
            }).start();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không thể khởi tạo UDP!");
        }
    }

    private void send() {
        String msg = txtInput.getText().trim();
        if (!msg.isEmpty()) {
            sendRawMessage(msg);
            txtInput.setText("");
            if (msg.equalsIgnoreCase("exit")) {
                System.exit(0);
            }
        }
    }

    // Hàm bổ trợ để đóng gói gói tin gửi đi
    private void sendRawMessage(String msg) {
        try {
            byte[] sendData = msg.getBytes("UTF-8");
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, PORT);
            clientSocket.send(sendPacket);
        } catch (IOException e) {
            txtChat.append(">> Không thể gửi tin nhắn.\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UDP_ChatClient());
    }
}