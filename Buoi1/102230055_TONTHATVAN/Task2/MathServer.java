import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.Stack;
import java.util.ArrayList;
import java.util.List;

public class MathServer extends JFrame {
    private JTextArea txtLog;
    private static int clientCount = 0;

    public MathServer() {
        setTitle("SERVER TÍNH TOÁN TCP - PORT 5000");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setBackground(new Color(25, 25, 25));
        txtLog.setForeground(Color.WHITE);
        txtLog.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtLog.setMargin(new Insets(10, 10, 10, 10));

        add(new JScrollPane(txtLog));
        setVisible(true);

        startServer();
    }

    private void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(5000)) {
                log("Server đang hoạt động...");
                while (true) {
                    Socket socket = serverSocket.accept();
                    clientCount++;
                    String clientID = "Máy " + clientCount;
                    log(">>> " + clientID + " kết nối.");
                    new Thread(() -> handleClient(socket, clientID)).start();
                }
            } catch (IOException e) {
                log("Lỗi: " + e.getMessage());
            }
        }).start();
    }

    private void handleClient(Socket socket, String clientID) {
        try (DataInputStream dis = new DataInputStream(socket.getInputStream());
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
            dos.writeUTF(clientID);
            dos.flush();
            while (true) {
                String input = dis.readUTF();
                if (input.equalsIgnoreCase("exit")) break;
                log(clientID + ": " + input);
                String result;
                try {
                    result = calculate(input);
                } catch (Exception e) {
                    result = "Lỗi biểu thức";
                }
                dos.writeUTF(result);
                dos.flush();
                log("Trả về: " + result);
            }
        } catch (IOException e) {
            log("!!! " + clientID + " ngắt kết nối.");
        }
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> txtLog.append(msg + "\n"));
    }

    private String calculate(String expr) throws Exception {
        List<String> tokens = tokenize(expr);
        List<String> postfix = infixToPostfix(tokens);
        double res = evaluatePostfix(postfix);
        if (res == (long) res) return String.format("%d", (long) res);
        return String.format("%.2f", res);
    }

    private List<String> tokenize(String s) {
        List<String> tokens = new ArrayList<>();
        String number = "";
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isDigit(c) || c == '.') {
                number += c;
            } else {
                if (!number.isEmpty()) {
                    tokens.add(number);
                    number = "";
                }
                if (c == ' ' || c == '\t') continue;
                
                if (c == '-') {
                    boolean isUnary = tokens.isEmpty() || tokens.get(tokens.size() - 1).equals("(") || 
                                     isOperator(tokens.get(tokens.size() - 1));
                    if (isUnary) {
                        tokens.add("u-");
                        continue;
                    }
                }
                tokens.add(String.valueOf(c));
            }
        }
        if (!number.isEmpty()) tokens.add(number);
        return tokens;
    }

    private boolean isOperator(String t) {
        return t.equals("+") || t.equals("-") || t.equals("*") || t.equals("/");
    }

    private int priority(String op) {
        if (op.equals("u-")) return 3;
        if (op.equals("*") || op.equals("/")) return 2;
        if (op.equals("+") || op.equals("-")) return 1;
        return 0;
    }

    private List<String> infixToPostfix(List<String> tokens) {
        List<String> output = new ArrayList<>();
        Stack<String> stack = new Stack<>();
        for (String t : tokens) {
            if (Character.isDigit(t.charAt(0)) || (t.length() > 1 && Character.isDigit(t.charAt(1)))) {
                output.add(t);
            } else if (t.equals("(")) {
                stack.push(t);
            } else if (t.equals(")")) {
                while (!stack.isEmpty() && !stack.peek().equals("(")) output.add(stack.pop());
                stack.pop();
            } else {
                while (!stack.isEmpty() && priority(stack.peek()) >= priority(t)) {
                    if (t.equals("u-") && stack.peek().equals("u-")) break;
                    output.add(stack.pop());
                }
                stack.push(t);
            }
        }
        while (!stack.isEmpty()) output.add(stack.pop());
        return output;
    }

    private double evaluatePostfix(List<String> postfix) {
        Stack<Double> stack = new Stack<>();
        for (String t : postfix) {
            if (t.equals("u-")) {
                stack.push(-stack.pop());
            } else if (isOperator(t)) {
                double b = stack.pop();
                double a = stack.pop();
                if (t.equals("+")) stack.push(a + b);
                else if (t.equals("-")) stack.push(a - b);
                else if (t.equals("*")) stack.push(a * b);
                else if (t.equals("/")) stack.push(a / b);
            } else {
                stack.push(Double.parseDouble(t));
            }
        }
        return stack.pop();
    }

    public static void main(String[] args) { new MathServer(); }
}