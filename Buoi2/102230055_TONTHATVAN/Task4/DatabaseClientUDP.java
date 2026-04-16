import javax.swing.*;
import java.awt.*;
import java.net.*;

public class DatabaseClientUDP extends JFrame {
    private JTextField txtQuery;
    private JTextArea txtResult;
    
    // Đối với UDP, chúng ta sử dụng DatagramSocket
    private DatagramSocket clientSocket;
    private InetAddress serverAddress;
    private final int SERVER_PORT = 7000;

    public DatabaseClientUDP() {
        setTitle("UDP Database Client Query");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        txtQuery = new JTextField("SELECT * FROM SinhVien");
        txtQuery.setFont(new Font("Consolas", Font.PLAIN, 14));
        JButton btnSend = new JButton("Thực thi SQL");

        JPanel pnlTop = new JPanel(new BorderLayout(5, 5));
        pnlTop.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pnlTop.add(new JLabel("Nhập lệnh SQL (UDP):"), BorderLayout.NORTH);
        pnlTop.add(txtQuery, BorderLayout.CENTER);
        pnlTop.add(btnSend, BorderLayout.EAST);

        txtResult = new JTextArea();
        txtResult.setEditable(false);
        txtResult.setFont(new Font("Monospaced", Font.PLAIN, 13));
        txtResult.setBackground(new Color(245, 245, 245));

        add(pnlTop, BorderLayout.NORTH);
        add(new JScrollPane(txtResult), BorderLayout.CENTER);

        // Khởi tạo Socket UDP
        initUDP();

        btnSend.addActionListener(e -> sendQuery());
        txtQuery.addActionListener(e -> sendQuery());

        setVisible(true);
    }

    private void initUDP() {
        try {
            clientSocket = new DatagramSocket();
            serverAddress = InetAddress.getByName("localhost");
            txtResult.append("Sẵn sàng gửi truy vấn UDP tới Server (Port " + SERVER_PORT + ")...\n");
        } catch (SocketException | UnknownHostException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khởi tạo UDP: " + e.getMessage());
        }
    }

    private void sendQuery() {
        try {
            String sql = txtQuery.getText().trim();
            if (sql.isEmpty()) return;

            // 1. Đóng gói và gửi yêu cầu
            byte[] sendBuffer = sql.getBytes("UTF-8");
            DatagramPacket sendPacket = new DatagramPacket(
                sendBuffer, sendBuffer.length, serverAddress, SERVER_PORT
            );
            clientSocket.send(sendPacket);

            // 2. Chuẩn bị buffer để nhận phản hồi từ Server
            byte[] receiveBuffer = new byte[8192]; // Buffer lớn để nhận dữ liệu bảng CSDL
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            
            // Đặt thời gian chờ nhận phản hồi (Tránh treo app nếu gói tin bị mất)
            clientSocket.setSoTimeout(5000); 

            try {
                clientSocket.receive(receivePacket);
                String res = new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8");
                txtResult.setText("--- KẾT QUẢ TRUY VẤN (UDP) ---\n" + res);
            } catch (SocketTimeoutException e) {
                txtResult.setText("Lỗi: Server không phản hồi (Timeout).");
            }

        } catch (Exception e) {
            txtResult.append("\nLỗi khi gửi/nhận dữ liệu: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try { 
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
        } catch (Exception e) {}
        new DatabaseClientUDP();
    }
}