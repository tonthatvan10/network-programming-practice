import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class UDP_MathClient extends JFrame {
    private JTextField txtInput;
    private JTextArea txtOutput;
    
    // Các thành phần UDP thay thế cho Socket TCP
    private DatagramSocket clientSocket;
    private InetAddress serverAddress;
    private final int PORT = 5000;

    public UDP_MathClient() {
        setSize(500, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setTitle("Máy tính UDP Client");

        txtInput = new JTextField();
        txtInput.setFont(new Font("Consolas", Font.PLAIN, 14));
        
        txtOutput = new JTextArea();
        txtOutput.setEditable(false);
        txtOutput.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtOutput.setBackground(new Color(240, 240, 240));

        JPanel pnlTop = new JPanel(new BorderLayout(5, 5));
        pnlTop.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        
        JLabel lblInfo = new JLabel(" Nhập phép tính (+,-,*,/,()): ");
        lblInfo.setFont(new Font("Arial", Font.BOLD, 12));
        pnlTop.add(lblInfo, BorderLayout.WEST);
        
        pnlTop.add(txtInput, BorderLayout.CENTER);
        JButton btn = new JButton("Gửi / Tính");
        pnlTop.add(btn, BorderLayout.EAST);

        add(pnlTop, BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(txtOutput);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // Khởi tạo kết nối UDP
        initUDP();

        btn.addActionListener(e -> send());
        txtInput.addActionListener(e -> send());
        
        setVisible(true);
    }

    private void initUDP() {
        try {
            // UDP không cần thiết lập kết nối ngay lúc này
            clientSocket = new DatagramSocket();
            serverAddress = InetAddress.getByName("localhost");
            
            txtOutput.append("Hệ thống UDP sẵn sàng. Kết nối Server tại Port " + PORT + "\n");
            txtOutput.append("Ví dụ: 5 + 13 - (12 - 4 * 6)\n");
            txtOutput.append("--------------------------------------------------\n");
            
        } catch (SocketException | UnknownHostException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khởi tạo UDP: " + e.getMessage());
            System.exit(0);
        }
    }

    private void send() {
        try {
            String expr = txtInput.getText().trim();
            if (expr.isEmpty()) return;
            
            // 1. Gửi biểu thức đi (Đóng gói vào DatagramPacket)
            byte[] sendData = expr.getBytes("UTF-8");
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, PORT);
            clientSocket.send(sendPacket);
            
            txtOutput.append("[Tôi gửi]: " + expr + "\n");
            
            // 2. Chờ nhận kết quả phản hồi
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            
            // Thiết lập chờ tối đa 3 giây, nếu quá thì coi như mất gói tin
            clientSocket.setSoTimeout(3000); 
            
            try {
                clientSocket.receive(receivePacket);
                String res = new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8");
                txtOutput.append("[Server trả lời kết quả] = " + res + "\n\n");
            } catch (SocketTimeoutException e) {
                txtOutput.append("[Lỗi]: Server không phản hồi sau 3 giây.\n\n");
            }
            
            txtInput.setText("");
            txtInput.requestFocus();
            txtOutput.setCaretPosition(txtOutput.getDocument().getLength());
            
        } catch (IOException e) {
            txtOutput.append("\n!!! Lỗi truyền dữ liệu UDP!");
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        new UDP_MathClient(); 
    }
}