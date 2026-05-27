# 💬 reactlqnt — Remix: Messenger 🚀

[![Kotlin Version](https://img.shields.io/badge/Kotlin-1.9.x-purple.svg?logo=kotlin)](https://kotlinlang.org/)
[![Compose Material 3](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-blue.svg?logo=android)](https://developer.android.com/jetpack/compose)
[![Database](https://img.shields.io/badge/Database-Room%20ORM-green.svg?logo=sqlite)](https://developer.android.com/training/data-storage/room)
[![Security](https://img.shields.io/badge/Security-Android%20Keystore%20%26%20Biometrics-red.svg)](https://developer.android.com/identity/sign-in/biometric)
[![License](https://img.shields.io/badge/License-MIT-teal.svg)](LICENSE)

**reactlqnt** (tên cấu hình: *Remix: Messenger*) là một ứng dụng di động nhắn tin thời gian thực cao cấp, sở hữu giao diện người dùng hiện đại, lấy cảm hứng từ sự tổng hòa giữa lối thiết kế gọn gàng của **Facebook Messenger** và khả năng tùy biến mạnh mẽ từ **Discord**. 

Ứng dụng được lập trình hoàn toàn bằng **Kotlin** kết hợp với **Jetpack Compose (Material 3)**, tích hợp cơ sở dữ liệu nội bộ **Room DB**, công cụ mã hóa **Android Keystore**, hệ thống khóa vân tay **Biometric**, trình mô phỏng cuộc gọi thoại/video tương tác và một bảng điều khiển giám sát hệ thống lập trình lập nghiệp **DevOps Hub Screen** độc đáo.

---

## 🎨 Trải Nghiệm Thị Giác & Chủ Đề Nổi Bật

Ứng dụng hỗ trợ tùy biến giao diện cuộc hội thoại theo phong cách cá nhân hóa sâu sắc với **4 dải Gradient màu độc đáo**:
*   🔷 **Mặc định (Sóng Xanh)**: Tông Indigo và Cyan biển sâu sắc nét, tạo cảm giác chuyên nghiệp, thanh lịch.
*   🔶 **Hoàng Hôn rực rỡ**: Sự kết hợp rực lửa của Orange và Rose, mang phong cách sôi động, trẻ trung.
*   🟢 **Cyberpunk Emerald**: Lớp chuyển sắc lục bảo (Green & Emerald) neon hầm hố của tương lai công nghệ.
*   🟣 **Lavender Neon**: Sắc tím tím huỳnh quang (Purple & Violet) bí ẩn như bầu trời mây đêm.

---

## 🌟 Các Tính Năng Trọng Tâm

### 1. 📂 Lưu trữ Trạng thái Liên tục (Persistent Cache Room Database)
*   Sử dụng **Room ORM** với Kotlin Coroutines để lưu trữ tức thì tin nhắn, tài khoản người dùng, phòng chat nhóm, danh bạ bạn bè và thông báo đẩy.
*   Hỗ trợ trải nghiệm ngoại tuyến mượt mà (Offline-First Cache) giúp ứng dụng tải cực nhanh mà không gây nhấp nháy màn hình.

### 2. 🔍 Trình Tìm Kiếm Tin Nhắn Nội Bộ (In-Chat Deep Search)
*   Tích hợp thanh tìm kiếm động ngay tại đầu mỗi phòng hội thoại.
*   Nhập từ khóa để lọc các đoạn hội thoại lịch sử theo thời gian thực đơn giản, loại bỏ hoàn toàn việc cuộn màn hình tìm kiếm mỏi mắt.

### 3. 🔐 Bảo Mật Sinh Trắc Học & Mã Hóa Hardware-Backed Keystore
*   **Biometric Lock Screen**: Bảo vệ ứng dụng nghiêm ngặt bằng Face ID, Vân tay hoặc PIN hệ thống thông qua `androidx.biometric`.
*   **Keystore Crypto System**: Tự động mã hóa tệp dữ liệu mật khẩu cá nhân bằng chuẩn quân đội AES thông qua `CryptoManager`.
*   *Tính năng an toàn tự động*: Cơ chế tự biến chuyển an toàn (Fallback Base64 mode) đảm bảo cấu hình mượt mà không bị treo lỗi phần cứng trên các dòng thiết bị giả lập cũ.

### 4. 📞 Giả Lập Trình Phủ Cuộc Gọi Thoại & Video (Interactive Call Overlay)
*   Sơ đồ lớp phủ trực quan hỗ trợ cả cuộc gọi âm thanh (Audio) và hình ảnh (Video).
*   Các tính năng tương tác thật bao gồm: Tắt/Mở Micro, Tắt/Mở Camera, Chuyển Loa ngoài/Loa trong cùng với bộ chỉ số thời lượng nhấp nháy động.

### 5. 🛠 DevOps Hub Screen - Bảng Điều Khiển Hệ Thống Tiên Tiến
*   Màn hình giả lập nâng cao phục vụ cho lập trình viên trình diễn mô hình Full-Stack.
*   **Trình hiển thị Docker & Kubernetes-Mocks** trực quan hóa các cụm dịch vụ ảo.
*   **Live Console Log Simulator**: Dòng dữ liệu hệ thống (INFO, SUCCESS, WARNING, FATAL EXCEPTION) liên tục nhảy số, phác họa lưu lượng tải Websocket và tốc độ phản hồi API bằng mili-giây chân thực.

### 6. 💬 Tương Tác Tin Nhắn Đa Dạng
*   Ghim tin nhắn quan trọng gắn với đầu cuộc hội thoại (Pinned Message Header).
*   Trình thả biểu tượng cảm xúc (Emoji Reactions Picker).
*   Phản hồi (Reply) trích dẫn trực quan đối với bất kỳ tin nhắn nào trong luồng trò chuyện.
*   Chia sẻ đính kèm tệp đa văn bản, hình ảnh, tài liệu văn phòng linh hoạt.

---

## 🏗 Kiến Trúc Hệ Thống (Clean MVVM)

Hệ thống được phát triển theo mô hình **MVVM (Model-View-ViewModel)** tinh gọn, chuẩn chỉ:
```
[ UI Layer (Jetpack Compose Screen) ]
      ▲                         │
      │ (Observe Flow State)    ▼ (Call Actions)
[ State-Holder Layer (ChatViewModel) ]
      ▲                         │
      │ (Flowable Streams)      ▼ (Retrieve & Set)
[ Domain Repository Layer (ChatRepository) ]
      ▲                         │
      │ (Local Cache Entities)   ▼ (Execute Commands)
[ Data Source Layer (Room DB / SQLite) ]
```

---

## 🛠 Công Nghệ & Thư Viện Sử Dụng

| Nhóm chức năng | Thư viện & Công nghệ | Mô tả tác vụ |
| :--- | :--- | :--- |
| **Giao diện** | *Jetpack Compose, Material 3* | Toàn bộ giao diện tùy biến tối tân, Dynamic Color, Edge-to-Edge phím mờ. |
| **Cơ sở dữ liệu** | *Room SQL Data Engine* | Đồng bộ luồng luân chuyển Offline-First bằng SQLite cao cấp và KSP. |
| **Xử lý Độc lập** | *Kotlin Coroutines & Flow* | Quản lý đa nhiệm không chặn UI chính, cập nhật trạng thái thời gian thực. |
| **Bảo mật** | *Android Bio, KeyStore, Crypto AES* | Xác thực khóa bảo vệ nội dung, mã hóa thông tin xác thực gốc. |
| **Ảnh đại diện** | *Coil Image Loader* | Tải ảnh phi tập trung bất đồng bộ với cơ chế cache thông minh. |
| **Mạng & JSON** | *Retrofit 2 & Moshi Serialization* | Chuẩn bị sẵn cơ sở dữ liệu kết nối API và truyền dữ liệu JSON. |
| **Kiểm tra (Test)**| *Robolectric & Roborazzi* | Đảm bảo tính ổn định và tính nhất quán giao diện qua ảnh chụp kiểm tra. |

---

## 📸 Hình Ảnh Giao Diện & Điều Hướng

Ứng dụng gồm các màn hình chính được thiết kế hiện đại, thông suốt:
1.  **Splash & Lock Screen**: Giao diện khởi tạo mượt mà và màn hình yêu cầu vân tay/mã khóa cực chất.
2.  **Login Screen**: Form đăng ký thông tin cá nhân lần đầu (Nickname, Tên hiển thị, Ảnh đại diện, Mã PIN bí mật).
3.  **Home Screen**: Danh sách cuộc trò chuyện nhóm / cá nhân hoạt động, hàng tin câu chuyện (Stories) hấp dẫn kiểu vòng tròn sắc màu và bảng Cài đặt ứng dụng.
4.  **Chat Screen**: Portal trò chuyện không gian sâu, hỗ trợ đính kèm, đổi chủ đề, tìm kiếm nội dung, phản hồi, thả biểu cảm và gọi mô phỏng.
5.  **DevOps Hub Screen**: Nơi trình diễn thiết kế hạ tầng Docker, kiểm soát kiến trúc mạng microservices và xem logs realtime ấn tượng.

---

## 📄 Bản Quyền

Dự án này được phân phối dưới giấy phép **MIT**. Xem chi tiết tại tệp [LICENSE](LICENSE) để biết thêm thông tin.

---

## 🤝 Liên Hệ Hỗ Trợ

Nếu bạn có bất kỳ câu hỏi nào về thiết kế, kiến thức di động, hoặc gặp lỗi trong quá trình khởi tạo ứng dụng, vui lòng liên hệ với nhóm phát triển qua:
*   **Tác giả**: [khoiplus2102pro@gmail.com](mailto:khoiplus2102pro@gmail.com)
*   **GitHub**: [@chatsreactlqnt](https://github.com/khoiplus2102pro)
*   **Trình xem thử (Demo Applet Stream)**: [Google AI Studio Shared Portal](https://ais-pre-5qqsbrl2idwyojvgbgomvp-418883067818.asia-southeast1.run.app)
