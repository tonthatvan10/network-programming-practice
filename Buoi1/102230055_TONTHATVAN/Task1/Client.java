import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        String hostname = "127.0.0.1"; // Chạy trên cùng máy
        int port = 8888;

        try (Socket socket = new Socket(hostname, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            System.out.print("Nhập chuỗi ký tự gửi lên Server: ");
            String userInput = scanner.nextLine();
            
            out.println(userInput); // Gửi chuỗi sang Server

            // Nhận và in toàn bộ kết quả trả về
            String response;
            while ((response = in.readLine()) != null) {
                System.out.println(response);
            }

        } catch (UnknownHostException e) {
            System.err.println("Không tìm thấy Server: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Lỗi I/O: " + e.getMessage());
        }
    }
}