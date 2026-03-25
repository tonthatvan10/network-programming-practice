import java.sql.*;

public class TestDB {
    public static void main(String[] args) {
        // Cấu hình thông số (Hãy đảm bảo khớp với MySQL Workbench của bạn)
        String url = "jdbc:mysql://localhost:3306/QuanLySinhVien";
        String user = "root";
        String pass = "Tonthatvan0406@"; 

        System.out.println("--- ĐANG KIỂM TRA KẾT NỐI DATABASE ---");

        try {
            // 1. Nạp Driver (Cực kỳ quan trọng để kiểm tra thư viện .jar)
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("1. Đã tìm thấy Driver MySQL trong thư viện.");

            // 2. Thử kết nối
            Connection conn = DriverManager.getConnection(url, user, pass);
            System.out.println("2. Kết nối Database thành công!");

            // 3. Truy vấn thử dữ liệu
            String sql = "SELECT * FROM SinhVien";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("3. Dữ liệu trong bảng SinhVien:");
            System.out.println("------------------------------------");
            
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                // Thay đổi tên cột cho đúng với bảng của bạn (MaSV, HoTen, Lop)
                String id = rs.getString(1);
                String name = rs.getString(2);
                String className = rs.getString(3);
                System.out.println("Mã SV: " + id + " | Họ tên: " + name + " | Lớp: " + className);
            }

            if (!hasData) {
                System.out.println("Kết nối OK nhưng bảng đang trống (không có dữ liệu).");
            }

            // 4. Đóng kết nối
            rs.close();
            stmt.close();
            conn.close();
            System.out.println("------------------------------------");
            System.out.println("KIỂM TRA HOÀN TẤT!");

        } catch (ClassNotFoundException e) {
            System.out.println("LỖI: Chưa thêm file .jar vào Referenced Libraries!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("LỖI SQL: Sai mật khẩu, sai tên DB hoặc Port.");
            System.out.println("Chi tiết: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}