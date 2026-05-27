# 🛠 Hướng Dẫn Cài Đặt và Khởi Chạy (SETUP.md)

Tài liệu này hướng dẫn chi tiết cách thiết lập môi trường lập trình, cấu hình và biên dịch ứng dụng **chatsreactlqnt** trên máy tính cá nhân hoặc môi trường giả lập Android.

---

## 📋 Yêu Cầu Hệ Thống & Môi Trường

Trước khi bắt đầu, hãy đảm bảo máy tính của bạn đã được cài đặt đầy đủ các công cụ sau:

1.  **Hệ điều hành**: Windows 10/11 (64-bit), macOS (với chip Apple Silicon hoặc Intel), hoặc Linux (Ubuntu/Debian).
2.  **Java Development Kit (JDK)**: **JDK 11** hoặc **JDK 17** (Khuyên dùng JDK 17 cho khả năng tương thích tối ưu với hệ thống Gradle hiện tại).
3.  **Android Studio**: Phiên bản **Ladybug (2024.2.1)** hoặc **Koala (2024.1.1)** trở lên để hỗ trợ Jetpack Compose Live Edit và Gradle Kotlin DSL ổn định.
4.  **Android SDK**:
    *   **Compile SDK & Target SDK**: API 36 (`release 36` / `minorApiLevel = 1`)
    *   **Minimum SDK**: API 24 (Android 7.0 Nougat trở lên)
5.  **Dụng cụ bổ trợ**: Git (để quản lý mã nguồn).

---

## 🚀 Các Bước Thiết Lập Dự Án

### Bước 1: Sao chép Mã Nguồn (Clone Repository)
Mở terminal hoặc Git Bash trên máy tính và chạy lệnh:
```bash
git clone https://github.com/manucian-official/chatsreactlqnt.git
cd chatsreactlqnt
```

### Bước 2: Cài Đặt Tệp Môi Trường `.env`
Dự án sử dụng thư viện **Secrets Gradle Plugin** để tự động tích hợp các API Key bảo mật tại thời điểm biên dịch mà không cần ghi đè vào mã nguồn mở.

1.  Sao chép tệp mẫu cấu hình:
    ```bash
    cp .env.example .env
    ```
2.  Mở tệp `.env` vừa tạo và điền khóa API của bạn (ví dụ: Google Gemini API Key nếu cần tích hợp dịch vụ AI thông minh):
    ```env
    GEMINI_API_KEY=AIzaSyD-Your-Actual-Gemini-API-Key
    ```

> ⚙️ **Lưu ý**: Tệp `.env` đã được cấu hình mặc định trong `.gitignore` để tránh rò rỉ mã bí mật lên hệ thống quản lý công khai của GitHub.

### Bước 3: Mở Dự Án Lần Đầu Trong Android Studio
1.  Khởi động **Android Studio**.
2.  Chọn **File** -> **Open** (hoặc chọn *Open an Existing Project*).
3.  Hãy trỏ thư mục đến vị trí bạn đã clone mã nguồn `chatsreactlqnt` ở bước 1 và bấm **OK**.
4.  Đợi vài phút để hệ thống **Gradle Sync** tự động nhận diện cấu hình dôi dư, tải bộ thư viện (Jetpack Compose, Room, Biometric) và tổ chức các module nội bộ.

---

## 🛠 Lệnh Biên Dịch & Chạy Ứng Dụng với Gradle CLI

Nếu bạn muốn thao tác trực tiếp qua dòng lệnh (Terminal) thay vì giao diện đồ họa Android Studio, hãy cài đặt các lệnh chuẩn sau đây:

> ⚠️ **Chú ý quan trọng**: Luôn chạy lệnh sử dụng từ khóa `gradle` hệ thống, KHÔNG sử dụng lệnh `./gradlew` hoặc `gradlew` do các tính toán tương thích hệ thống trung gian trên nền tảng Cloud.

### 1. Dọn Dẹp và Làm Sạch Bộ Nhớ Đệm (Clean Project)
```bash
gradle clean
```
*(Chỉ sử dụng khi cấu hình Studio thay đổi liên tục hoặc xuất hiện lỗi biên dịch không rõ nguyên nhân)*

### 2. Biên Dịch Ứng Dụng Phiên Bản Kiểm Thử (Build Debug APK)
```bash
gradle assembleDebug
```
*Tệp APK sau khi hoàn thiện sẽ được xuất tại vị trí:*  
`app/build/outputs/apk/debug/app-debug.apk`

### 3. Biên Dịch Dự Án Lên Bản Phát Hành (Build Release AAB/APK)
```bash
gradle assembleRelease
```
*Tệp xuất ra nằm tại vị trí:*  
`app/build/outputs/bundle/release/app-release.aab` hoặc `app/build/outputs/apk/release/app-release-unsigned.apk`

---

## 🧪 Hệ Thống Kiểm Thử (Testing Core)

Dự án có sẵn cơ chế viết test tự động cục bộ cao cấp qua **Robolectric** (giả lập môi trường phần cứng ngay trên máy ảo cục bộ JVM của máy tính mà không cần cắm điện thoại vật lý) và bộ **Roborazzi** (chụp ảnh màn hình lưu giữ cấu trúc giao diện).

### 1. Cách Chạy Toàn Bộ Unit Test Cục Bộ
Chạy lệnh sau trên terminal của dự án để đảm bảo không có dòng mã nào bị biên dịch hỏng:
```bash
gradle :app:testDebugUnitTest
```

### 2. Gỡ Lỗi Hoặc Ghi Đè Ảnh Chụp Giao Diện Test (Roborazzi Screenshots)
Nếu bạn thay đổi thiết kế giao diện (Font, Màu chủ đề, Card layout) và muốn ghi lại tệp chụp màn hình đối chuẩn mới:
```bash
gradle :app:recordRoborazziDebug
```

---

## 🔍 Giải Quyết Lỗi Thường Gặp (Troubleshooting)

### 1. Không Thiết Lập Được Sinh Trắc Học Dự Phòng Trên Thiết Bị Máy Ảo (Emulator)
*   **Triệu chứng**: Gạt nút "Khóa ứng dụng (Biometric)" trong màn hình Cài đặt báo lỗi: *"Chưa đăng ký vân tay/khuôn mặt trên thiết bị"* hoặc *"Cảm biến không khả dụng"*.
*   **Khắc phục**: 
    1. Trên máy ảo Emulator, mở phần mềm **Settings** của Android.
    2. Chọn **Security** -> **Screen Lock** -> Đặt mật khẩu hoặc mã PIN tùy ý.
    3. Chọn tiếp **Fingerprint** (hoặc **Face Unlock** tùy phiên bản API) và đi theo hướng dẫn thiết lập vân tay mô phỏng.
    4. Trở lại ứng dụng và kích hoạt lại tính năng Khóa sinh trắc học một cách mượt mà.

### 2. Sự Cố Giải Mã Thất Bại sau khi Cài Đè Ứng Dụng (Keystore Decryption Error)
*   **Triệu chứng**: Khi cài đè ứng dụng debug mới lên một ứng dụng cũ đã lưu mật khẩu bảo vệ mã hóa, đôi khi Keystore bị hết hạn hoặc khóa phần cứng đổi cấu trúc sinh ra lỗi treo thông tin giải mã.
*   **Khắc phục**: Hệ thống của chúng tôi đã tích hợp giải pháp **Fallback Base64 tự động** cực kỳ thông minh trong `CryptoManager.kt` và `BiometricHelper.kt`. Nếu xảy ra va chạm khóa, hãy gỡ ứng dụng cũ ngoài màn hình chính máy ảo, tiến hành cài đặt sạch lại từ đầu.

### 3. Lỗi Tràn RAM Khi Biên Dịch Trong Android Studio (Daemon OutOfMemory)
*   Mở tệp `gradle.properties` trong thư mục gốc dự án và điều chỉnh lượng RAM cấp cho Gradle tùy thuộc vào cấu hình thực tế máy chủ phát triển của bạn (khuyên dùng mức tối đa là 2048MB đến 4096MB):
    ```properties
    org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
    ```
