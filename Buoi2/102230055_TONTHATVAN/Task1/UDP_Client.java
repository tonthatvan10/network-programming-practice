import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class UDP_Client extends JFrame {
    private JTextField txtInput;
    private JTextArea txtOutput;
    private String myName = "Client UDP";
    
    private DatagramSocket clientSocket;
    private InetAddress serverAddress;
    private final int PORT = 43;

    public UDP_Client() {
        setSize(450, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        txtInput = new JTextField();
        txtOutput = new JTextArea();
        txtOutput.setEditable(false);
        txtOutput.setFont(new Font("Consolas", Font.PLAIN, 14));

        JPanel pnlTop = new JPanel(new BorderLayout(5, 5));
        pnlTop.add(new JLabel(" Nhập chuỗi: "), BorderLayout.WEST);
        pnlTop.add(txtInput, BorderLayout.CENTER);
        JButton btn = new JButton("Gửi");
        pnlTop.add(btn, BorderLayout.EAST);

        add(pnlTop, BorderLayout.NORTH);
        add(new JScrollPane(txtOutput), BorderLayout.CENTER);

        initUDP();

        btn.addActionListener(e -> send());
        txtInput.addActionListener(e -> send());
        
        setVisible(true);
    }

    private void initUDP() {
        try {
            // Khởi tạo Socket UDP
            clientSocket = new DatagramSocket();
            serverAddress = InetAddress.getByName("localhost");
            
            setTitle("Cửa sổ: " + myName);
            txtOutput.append("Hệ thống UDP sẵn sàng. Nhập gì đó để gửi đến Server...\n");
            
        } catch (SocketException | UnknownHostException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khởi tạo UDP: " + e.getMessage());
            System.exit(0);
        }
    }

    private void send() {
        try {
            String msg = txtInput.getText().trim();
            if (msg.isEmpty()) return;

            // 1. Gửi dữ liệu đi
            byte[] sendData = msg.getBytes("UTF-8");
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, PORT);
            clientSocket.send(sendPacket);
            
            txtOutput.append("\n[Tôi]: " + msg + "\n");
            txtInput.setText("");

            // 2. Đợi nhận phản hồi từ Server
            byte[] receiveData = new byte[2048]; // Buffer đủ lớn để chứa kết quả thống kê
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            
            // Cài đặt timeout (tùy chọn) để tránh treo giao diện nếu Server không phản hồi
            clientSocket.setSoTimeout(3000); 
            
            try {
                clientSocket.receive(receivePacket);
                String res = new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8");
                txtOutput.append("[Server trả lời]:\n" + res + "\n");
            } catch (SocketTimeoutException e) {
                txtOutput.append("\n[Lỗi]: Server không phản hồi (Timeout)!\n");
            }

        } catch (IOException e) {
            txtOutput.append("\nLỗi truyền tải dữ liệu!");
        }
    }

    public static void main(String[] args) { 
        // Đảm bảo chạy trong Event Dispatch Thread của Swing
        SwingUtilities.invokeLater(() -> new UDP_Client()); 
    }
}