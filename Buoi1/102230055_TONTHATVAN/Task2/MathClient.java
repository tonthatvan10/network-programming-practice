import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class MathClient extends JFrame {
    private JTextField txtInput;
    private JTextArea txtOutput;
    private String myName = "Client";

    private DataOutputStream dos;
    private DataInputStream dis;

    public MathClient() {
        setSize(500, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

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

        connectServer();

        btn.addActionListener(e -> send());
        txtInput.addActionListener(e -> send());
        
        setVisible(true);
    }

    private void connectServer() {
        try {
            Socket s = new Socket("localhost", 5000);
            dos = new DataOutputStream(s.getOutputStream());
            dis = new DataInputStream(s.getInputStream());

            myName = dis.readUTF(); 
            setTitle("Cửa sổ Máy tính: " + myName);
            
            txtOutput.append("Chào " + myName + "! Kết nối Server Tính Toán thành công.\n");
            txtOutput.append("Ví dụ: 5 + 13 - (12 - 4 * 6)\n");
            txtOutput.append("--------------------------------------------------\n");
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy Server! Hãy chắc chắn MathServer đang chạy port 5000.");
            System.exit(0);
        }
    }

    private void send() {
        try {
            String expr = txtInput.getText().trim();
            if (expr.isEmpty()) return;
            
            dos.writeUTF(expr);
            txtOutput.append("[Tôi gửi]: " + expr + "\n");
            
            String res = dis.readUTF();
            txtOutput.append("[Server trả lời kết quả] = " + res + "\n\n");
            
            txtInput.setText("");
            txtInput.requestFocus();
            
            txtOutput.setCaretPosition(txtOutput.getDocument().getLength());
            
        } catch (IOException e) {
            txtOutput.append("\n!!! Mất kết nối tới Server!");
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        new MathClient(); 
    }
}