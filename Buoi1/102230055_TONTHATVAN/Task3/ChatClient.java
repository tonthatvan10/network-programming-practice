import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class ChatClient extends JFrame {
    private JTextField txtInput;
    private JTextArea txtChat;
    private DataOutputStream dos;
    private DataInputStream dis;
    private String myName = "Đang kết nối...";

    public ChatClient() {
        setTitle("Chat Room");
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

        // HIỆN CỬA SỔ TRƯỚC KHI KẾT NỐI
        setVisible(true); 

        // GỌI KẾT NỐI TRONG THREAD RIÊNG ĐỂ KHÔNG TREO UI
        new Thread(() -> connectServer()).start();

        btnSend.addActionListener(e -> send());
        txtInput.addActionListener(e -> send());
    }

    private void connectServer() {
        try {
            Socket socket = new Socket("localhost", 6000);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            myName = dis.readUTF();
            SwingUtilities.invokeLater(() -> setTitle("Chat Room - " + myName));

            while (true) {
                String msg = dis.readUTF();
                SwingUtilities.invokeLater(() -> {
                    txtChat.append(msg + "\n");
                    txtChat.setCaretPosition(txtChat.getDocument().getLength());
                });
            }
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> {
                txtChat.append(">> LỖI: Không thể kết nối Server (Port 6000)!\n");
                JOptionPane.showMessageDialog(this, "Hãy bật Server trước!");
            });
        }
    }

    private void send() {
        try {
            String msg = txtInput.getText().trim();
            if (!msg.isEmpty() && dos != null) {
                dos.writeUTF(msg);
                dos.flush();
                txtInput.setText("");
            }
        } catch (IOException e) {
            txtChat.append(">> Mất kết nối, không thể gửi.\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClient());
    }
}