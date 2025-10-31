# Trợ lý TFT chuyên sâu trên Android

## 1. Giới thiệu
Ứng dụng "TFT Assistant" là một trợ lý chiến thuật dành cho người chơi Teamfight Tactics (TFT) trên nền tảng Android. Ứng dụng chạy song song với game, thu thập dữ liệu trận đấu theo thời gian thực, phân tích tình huống và đưa ra gợi ý giúp người chơi ra quyết định tối ưu mà vẫn tuân thủ giới hạn do Riot Games quy định.

## 2. Giá trị cốt lõi
- **Theo dõi trận đấu tức thời**: Nhận diện trạng thái bàn cờ, cửa hàng, trang bị, vòng đấu và các chỉ số kinh tế ngay khi game diễn ra.
- **Tư vấn chiến thuật cá nhân hóa**: Đề xuất ghép trang bị, lựa chọn Trái Ác Quỷ, hướng xây dựng đội hình, vị trí đứng và kế hoạch roll/level theo từng giai đoạn.
- **Cơ sở dữ liệu cập nhật liên tục**: Đồng bộ thông tin tướng, tộc/hệ, trang bị, Trái Ác Quỷ và meta mỗi mùa TFT.
- **Giao diện overlay thân thiện**: Hiển thị gợi ý trực quan, không che khuất tầm nhìn và có thể tương tác nhanh trong trận.

## 3. Tính năng chính
1. **Thu thập dữ liệu**
   - Chụp màn hình bằng `MediaProjection` và `ImageReader`.
   - Nhận dạng văn bản với Google ML Kit / tess-two.
   - Nhận dạng icon tướng, trang bị bằng OpenCV hoặc mô hình TensorFlow Lite.
2. **Phân tích chiến thuật**
   - Gợi ý ghép trang bị tối ưu cho các carry chủ lực.
   - Tư vấn lựa chọn Trái Ác Quỷ theo đội hình và trang bị hiện có.
   - Đề xuất mua tướng dựa trên synergy, khả năng nâng cấp sao và độ hiếm.
   - Định hướng đội hình mục tiêu, cảnh báo pivot khi bị tranh bài.
   - Khuyến nghị sắp xếp vị trí tướng đối phó từng dạng đội hình đối thủ.
   - Tư vấn kế hoạch kinh tế: thời điểm roll, lên cấp, giữ lợi tức.
3. **Giao diện người dùng**
   - Overlay nổi có thể bật/tắt nhanh, chế độ thông báo ngắn gọn.
   - Bảng điều khiển chính ngoài trận để cấu hình dữ liệu, tải meta mới, xem hướng dẫn.
4. **Cập nhật dữ liệu**
   - Module đồng bộ dữ liệu tĩnh (Data Dragon) và meta (LoLChess, MetaTFT, Mobalytics).
   - Hệ thống cập nhật OTA: kiểm tra phiên bản dữ liệu khi mở app và tải về bản mới.
5. **Hệ thống tuân thủ**
   - Cảnh báo chính sách, cho phép bật/tắt các gợi ý nhạy cảm.
   - Tập trung hỗ trợ người chơi tự quyết định, tránh tự động hóa hành vi.

## 4. Kiến trúc tổng quan
```
+------------------------------+       +-------------------------------+
|   TFT Mobile (ứng dụng gốc)  |       |  TFT Assistant (Android App)  |
|------------------------------|       |-------------------------------|
|  Video Frames / UI Elements  |  -->  |  1. Capture Service           |
|                              |       |     - MediaProjection         |
|                              |       |     - ImageReader             |
+------------------------------+       |-------------------------------|
                                       |  2. OCR & CV Pipeline         |
                                       |     - ML Kit / Tesseract      |
                                       |     - OpenCV / TFLite         |
                                       |-------------------------------|
                                       |  3. Game State Builder        |
                                       |     - Parsing text/icon       |
                                       |     - State normalization     |
                                       |-------------------------------|
                                       |  4. Recommendation Engine     |
                                       |     - Rule-based heuristics   |
                                       |     - Meta database           |
                                       |-------------------------------|
                                       |  5. Overlay UI Layer          |
                                       |     - Floating widgets        |
                                       |     - Notifications           |
                                       |-------------------------------|
                                       |  6. Data Manager              |
                                       |     - Data Dragon sync        |
                                       |     - Meta updates            |
                                       |-------------------------------|
                                       |  7. Compliance Guard          |
                                       |     - Policy toggles          |
                                       |     - Audit logging           |
                                       +-------------------------------+
```

## 5. Công nghệ đề xuất
| Hạng mục | Lựa chọn chính | Ghi chú |
|----------|----------------|---------|
| Ngôn ngữ | Kotlin (UI & logic), Java (interop), C++ (NDK nếu cần tăng tốc CV) |  |
| OCR | Google ML Kit Text Recognition v2, tess-two (Tesseract) | Chạy on-device, hỗ trợ real-time |
| Computer Vision | OpenCV Android SDK, TensorFlow Lite | Template matching / phân loại icon |
| Lưu trữ dữ liệu | Room/SQLite, JSON cấu hình | Đồng bộ Data Dragon, meta |
| Đồng bộ mạng | Retrofit/OkHttp, WorkManager | Tải dữ liệu meta định kỳ |
| UI overlay | WindowManager + quyền `SYSTEM_ALERT_WINDOW`, Compose Multiplatform overlay hoặc View truyền thống | Hỗ trợ theme trong suốt |
| Kiểm thử | Espresso, UI Automator, Unit test (JUnit), Benchmark | Đảm bảo hiệu năng khi overlay |

## 6. Cơ sở dữ liệu & nguồn dữ liệu
- **Data Dragon (Riot Games)**: ảnh, chỉ số tướng, trang bị, tộc/hệ.
- **Nguồn cộng đồng**: LoLChess, MetaTFT, Mobalytics – cung cấp tier list, đội hình meta, tỷ lệ ghép đồ.
- **Nguồn tuỳ chỉnh**: Bộ quy tắc do đội ngũ chuyên gia xây dựng (ví dụ ưu tiên trang bị theo carry).
- **Thu thập người dùng**: Lịch sử trận đấu, lựa chọn ưu tiên cá nhân để tinh chỉnh đề xuất.

## 7. Thuật toán gợi ý
1. **Rule-based heuristics**: Bộ luật dựa trên kinh nghiệm cao thủ (ví dụ mốc roll, cấp chuẩn từng vòng, ngưỡng máu nguy hiểm).
2. **Scoring engine**: Tính điểm cho từng lựa chọn (mua tướng, ghép đồ) dựa trên synergy, độ hiếm, khả năng nâng cấp.
3. **Formation templates**: Tập mẫu sắp xếp (corner, frontline-center…) + trigger cảnh báo theo kỹ năng đối thủ.
4. **Pivot detection**: Giám sát tướng trùng lặp với đối thủ -> gợi ý comp thay thế ít cạnh tranh.
5. **Personalization layer (mở rộng)**: Học thói quen người chơi từ dữ liệu lịch sử để ưu tiên khuyến nghị phù hợp phong cách.

## 8. Lộ trình triển khai
| Giai đoạn | Hạng mục | Kết quả mong đợi |
|-----------|----------|------------------|
| **MVP 1** | Chụp màn hình, OCR cơ bản (shop, vàng, level) | Hiển thị thông tin dạng dashboard ngoài trận |
| **MVP 2** | Overlay realtime + gợi ý ghép trang bị, danh sách comp meta tĩnh | Người chơi xem gợi ý khi chơi, chưa cá nhân hóa |
| **Beta** | Nhận dạng icon tướng, trang bị; gợi ý mua tướng, định hướng comp | Đề xuất chiến thuật theo đội hình hiện tại |
| **Release** | Thuật toán roll/level, gợi ý vị trí, tùy chỉnh chính sách | Sản phẩm hoàn chỉnh có cảnh báo chính sách |
| **Post-release** | Machine learning nâng cao, học từ lịch sử trận đấu | Đề xuất cá nhân hóa theo phong cách người chơi |

## 9. Tuân thủ chính sách Riot Games
- Tôn trọng quy định không cung cấp thông tin vượt quá những gì người chơi có thể biết sẵn.
- Tránh gợi ý trực tiếp theo trạng thái đối thủ thời gian thực (có thể tắt/bật tùy chọn).
- Không tự động hóa hành động, chỉ đưa ra khuyến nghị và highlight lựa chọn quan trọng.
- Cung cấp cảnh báo trong ứng dụng và tài liệu hướng dẫn sử dụng an toàn.

## 10. Kiểm thử & đo lường
- **Hiệu năng**: Đo FPS game khi overlay hoạt động, đảm bảo không gây giật lag.
- **Độ chính xác OCR/CV**: Bộ test ảnh chuẩn cho từng độ phân giải, kiểm tra tỉ lệ nhận dạng chính xác.
- **A/B testing**: So sánh tỉ lệ thắng, thứ hạng người chơi khi bật/tắt từng nhóm gợi ý.
- **Telemetry**: Thu thập sự kiện (opt-in) để biết tính năng nào được sử dụng nhiều, từ đó cải thiện thuật toán.

## 11. Hướng dẫn đóng góp
1. Fork repository, tạo nhánh mới từ `main`.
2. Hoàn thành tính năng cùng test tương ứng.
3. Gửi pull request kèm mô tả chi tiết và bằng chứng test.
4. Tuân thủ chuẩn code Kotlin/Java, bố cục module và guideline UI của Material Design.

## 12. Ghi chú pháp lý
Ứng dụng chỉ nhằm mục đích hỗ trợ học hỏi và nâng cao trải nghiệm cá nhân. Người dùng tự chịu trách nhiệm khi sử dụng. Nhà phát triển khuyến cáo tuân thủ mọi điều khoản dịch vụ của Riot Games và có thể vô hiệu hóa các tính năng nhạy cảm theo yêu cầu.

## 13. Tài liệu tham khảo
- Bot TFT-OCR-BOT và các dự án OCR tương tự.
- ML Kit Text Recognition v2 – Google Developers.
- Riot Developer Portal – tài liệu chính thức về TFT.
- Mobalytics, LoLChess, MetaTFT – nguồn meta và overlay.
- Bài viết cộng đồng về Trái Ác Quỷ (Power Ups) mùa 15.

## 14. Cấu trúc mã nguồn & hướng dẫn build

```
TFT-ASSISTANT-
├── app/                       # Mã nguồn ứng dụng Android
│   ├── build.gradle.kts       # Cấu hình module, Compose, Hilt, ML Kit
│   ├── src/main/              # Mã nguồn chính
│   │   ├── java/com/tftassistant/app/
│   │   │   ├── capture/       # MediaProjection + xử lý khung hình
│   │   │   ├── data/          # Repository, Room, Retrofit, model
│   │   │   ├── domain/        # Bộ điều phối trạng thái & gợi ý
│   │   │   ├── ocr/           # OCR + nhận dạng icon (stub TFLite)
│   │   │   ├── overlay/       # Service overlay nổi hiển thị gợi ý
│   │   │   └── ui/            # Activity chính & Compose UI
│   │   └── res/               # Resources (string, theme, icon)
│   └── assets/meta/           # Dữ liệu meta seed ban đầu
├── build.gradle.kts           # Định nghĩa plugin phiên bản
├── settings.gradle.kts        # Định nghĩa module
└── gradle.properties          # Tham số Gradle
```

### Build nhanh với Android Studio
1. Mở Android Studio (Giraffe trở lên) > `File` > `Open` > trỏ tới thư mục repo.
2. Đồng bộ Gradle, cài đặt dependency ML Kit, TensorFlow Lite từ Google Maven.
3. Tạo thiết bị ảo API 33+ hoặc cắm máy thật (Android 8.0+). Cấp quyền overlay & ghi màn hình khi được yêu cầu.
4. Chạy cấu hình `app` ở chế độ debug. Overlay sẽ hiển thị khi ấn nút "Bắt đầu overlay" trong app.

> Lưu ý: Repo không đính kèm Gradle Wrapper (.jar) hay model nhị phân nhằm tuân thủ yêu cầu “không tệp nhị phân”. Hãy sử dụng Gradle cài sẵn trong Android Studio hoặc tự cài thủ công.

