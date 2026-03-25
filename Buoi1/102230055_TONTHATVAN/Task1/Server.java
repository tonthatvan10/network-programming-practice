import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) {
        int port = 8888;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server đang đợi kết nối tại cổng " + port + "...");
            
            while (true) {
                try (Socket socket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                    
                    System.out.println("Client đã kết nối!");
                    String input = in.readLine(); // Nhận chuỗi từ Client
                    
                    if (input != null) {
                        // 1. Đảo ngược chuỗi
                        String reversed = new StringBuilder(input).reverse().toString();
                        // 2. In hoa
                        String upper = input.toUpperCase();
                        // 3. In thường
                        String lower = input.toLowerCase();
                        // 4. Vừa hoa vừa thường (Ví dụ: hElLo)
                        String mixed = mixCase(input);
                        // 5. Đếm từ và nguyên âm
                        String stats = countStats(input);

                        // Gửi trả kết quả cho Client (ngăn cách bằng dấu xuống dòng hoặc ký tự đặc biệt)
                        out.println("--- KẾT QUẢ TỪ SERVER ---");
                        out.println("Đảo ngược: " + reversed);
                        out.println("In hoa: " + upper);
                        out.println("In thường: " + lower);
                        out.println("Hoa thường: " + mixed);
                        out.println(stats);
                    }
                } catch (IOException e) {
                    System.out.println("Lỗi kết nối: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Không thể mở cổng: " + e.getMessage());
        }
    }

    // Hàm đổi chữ vừa hoa vừa thường
    private static String mixCase(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i % 2 == 0) sb.append(Character.toUpperCase(s.charAt(i)));
            else sb.append(Character.toLowerCase(s.charAt(i)));
        }
        return sb.toString();
    }

    // Hàm đếm số từ và nguyên âm
    private static String countStats(String s) {
        int words = s.trim().isEmpty() ? 0 : s.trim().split("\\s+").length;
        int vowels = 0;
        String v = "aeiouAEIOU";
        for (char c : s.toCharArray()) {
            if (v.indexOf(c) != -1) vowels++;
        }
        return "Số từ: " + words + " | Số nguyên âm: " + vowels;
    }
}