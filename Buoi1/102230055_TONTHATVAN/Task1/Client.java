import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class Client extends JFrame {
    private JTextField txtInput;
    private JTextArea txtOutput;
    private String myName = "Client";

    private DataOutputStream dos;
    private DataInputStream dis;

    public Client() {
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

        connectServer();

        btn.addActionListener(e -> send());
        txtInput.addActionListener(e -> send());
        
        setVisible(true);
    }

    private void connectServer() {
        try {
            Socket s = new Socket("localhost", 43);
            dos = new DataOutputStream(s.getOutputStream());
            dis = new DataInputStream(s.getInputStream());

            myName = dis.readUTF(); 
            setTitle("Cửa sổ của: " + myName);
            txtOutput.append("Chào " + myName + "! Bạn đã kết nối thành công.\n");
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy Server!");
            System.exit(0);
        }
    }

    private void send() {
        try {
            String msg = txtInput.getText().trim();
            if (msg.isEmpty()) return;
            
            dos.writeUTF(msg);
            txtOutput.append("\n[Tôi]: " + msg + "\n");
            
            String res = dis.readUTF();
            txtOutput.append("[Server trả lời]:\n" + res + "\n");
            txtInput.setText("");
        } catch (IOException e) {
            txtOutput.append("\nMất kết nối!");
        }
    }

    public static void main(String[] args) { new Client(); }
}