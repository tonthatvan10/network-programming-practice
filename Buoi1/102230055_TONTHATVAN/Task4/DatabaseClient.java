import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class DatabaseClient extends JFrame {
    private JTextField txtQuery;
    private JTextArea txtResult;
    private DataOutputStream dos;
    private DataInputStream dis;

    public DatabaseClient() {
        setTitle("Database Client Query");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        txtQuery = new JTextField("SELECT * FROM SinhVien");
        txtQuery.setFont(new Font("Consolas", Font.PLAIN, 14));
        JButton btnSend = new JButton("Thực thi SQL");

        JPanel pnlTop = new JPanel(new BorderLayout(5, 5));
        pnlTop.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pnlTop.add(new JLabel("Nhập lệnh SQL:"), BorderLayout.NORTH);
        pnlTop.add(txtQuery, BorderLayout.CENTER);
        pnlTop.add(btnSend, BorderLayout.EAST);

        txtResult = new JTextArea();
        txtResult.setEditable(false);
        txtResult.setFont(new Font("Monospaced", Font.PLAIN, 13));

        add(pnlTop, BorderLayout.NORTH);
        add(new JScrollPane(txtResult), BorderLayout.CENTER);

        connectServer();

        btnSend.addActionListener(e -> sendQuery());
        txtQuery.addActionListener(e -> sendQuery());

        setVisible(true);
    }

    private void connectServer() {
        try {
            Socket socket = new Socket("localhost", 7000);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            txtResult.append("Đã kết nối tới Database Server.\n");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối Server!");
        }
    }

    private void sendQuery() {
        try {
            String sql = txtQuery.getText().trim();
            if (sql.isEmpty()) return;

            dos.writeUTF(sql);
            dos.flush();

            String res = dis.readUTF();
            txtResult.setText("--- KẾT QUẢ TRUY VẤN ---\n" + res);
        } catch (IOException e) {
            txtResult.append("\nLỗi kết nối Server.");
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        new DatabaseClient();
    }
}