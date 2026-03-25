import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class ChatClient extends JFrame {
    private JTextField txtInput;
    private JTextArea txtChat;
    private DataOutputStream dos;
    private DataInputStream dis;
    private String myName = "";

    public ChatClient() {
        setSize(450, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        txtChat = new JTextArea();
        txtChat.setEditable(false);
        txtChat.setLineWrap(true);
        txtChat.setWrapStyleWord(true);
        txtChat.setFont(new Font("Arial", Font.PLAIN, 14));
        txtChat.setMargin(new Insets(5, 5, 5, 5));

        txtInput = new JTextField();
        txtInput.setFont(new Font("Arial", Font.PLAIN, 14));
        JButton btnSend = new JButton("Gửi");

        JPanel pnlBottom = new JPanel(new BorderLayout(5, 5));
        pnlBottom.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        pnlBottom.add(txtInput, BorderLayout.CENTER);
        pnlBottom.add(btnSend, BorderLayout.EAST);

        add(new JScrollPane(txtChat), BorderLayout.CENTER);
        add(pnlBottom, BorderLayout.SOUTH);

        connectServer();

        btnSend.addActionListener(e -> send());
        txtInput.addActionListener(e -> send());

        setVisible(true);
    }

    private void connectServer() {
        try {
            Socket socket = new Socket("localhost", 6000);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            // Nhận tên định danh từ Server
            myName = dis.readUTF();
            setTitle("Chat Room - " + myName);

            // Luồng lắng nghe tin nhắn từ Server
            new Thread(() -> {
                try {
                    while (true) {
                        String msg = dis.readUTF();
                        txtChat.append(msg + "\n");
                        // Tự động cuộn xuống tin mới nhất
                        txtChat.setCaretPosition(txtChat.getDocument().getLength());
                    }
                } catch (IOException e) {
                    txtChat.append(">> Mất kết nối tới Server.\n");
                }
            }).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối Server!");
            System.exit(0);
        }
    }

    private void send() {
        try {
            String msg = txtInput.getText().trim();
            if (!msg.isEmpty()) {
                dos.writeUTF(msg);
                dos.flush();
                txtInput.setText("");
            }
        } catch (IOException e) {
            txtChat.append(">> Không thể gửi tin nhắn.\n");
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        new ChatClient();
    }
}