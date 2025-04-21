## Tổng quan về plugin MetaMiner
MetaMiner là một plugin Minecraft dành cho máy chủ Bukkit/Spigot, cho phép người chơi:
- Có một khu vực đào riêng (thế giới mining)
- Đào các khối để nhận điểm
- Nâng cấp các khả năng như tốc độ đào, giá trị khối, và dung lượng túi đồ
- Thu thập NFT (Non-Fungible Tokens) hiếm

## Cấu trúc plugin
### Core
- **MetaMiner.java**: Lớp chính của plugin, xử lý việc tạo thế giới đào và quản lý các sự kiện chính
- **PlayerDataManager.java**: Quản lý dữ liệu người chơi (điểm, nâng cấp)
- **ConfigManager.java**: Quản lý cấu hình plugin, xác định tỷ lệ và giá trị của các khối

### Commands
- **LobbyCommand.java**: Dịch chuyển người chơi đến khu vực chung (mining_lobby)
- **MineAreaCommand.java**: Dịch chuyển người chơi đến khu vực đào riêng (mine_[tên])
- **UpgradeCommand.java**: Mở menu nâng cấp
- **ClaimCommand.java**: Chuyển đổi khối thành điểm

### Listeners
- **InventoryManager.java**: Quản lý túi đồ dựa trên cấp độ nâng cấp Storage
- **MiningSpeedListener.java**: Cải thiện tốc độ đào dựa trên cấp độ nâng cấp Speed

### GUI
- **UpgradeGUI.java**: Giao diện menu nâng cấp

### Utilities
- **ScoreboardDisplay.java**: Hiển thị bảng thông tin người chơi
- **VoidChunkGenerator.java**: Tạo thế giới trống cho khu vực đào
- **ExternalNftReader.java**: Tích hợp với plugin NFT để thả NFT cho người chơi

## Luồng hoạt động
1. Người chơi sử dụng `/lobby` để đi đến mining_lobby
2. Sau đó dùng `/minearea` để đi đến khu vực đào riêng
3. Tại đây, họ có thể đào các khối để nhận điểm
4. Sử dụng `/claim` để đổi các khối thành điểm
5. Dùng `/upgrade` để mở menu nâng cấp và cải thiện khả năng đào

## Hệ thống nâng cấp
Có 3 loại nâng cấp:
- **Speed**: Tăng tốc độ đào (hiệu ứng Haste)
- **Value**: Tăng giá trị của khối khi claim
- **Storage**: Mở rộng túi đồ trong khu vực đào

## Tính năng đặc biệt
- **Scoreboard**: Hiển thị điểm và cấp độ nâng cấp của người chơi
- **NFT Drop**: Cơ hội nhận các vật phẩm NFT đặc biệt khi đào
- **Túi đồ riêng**: Túi đồ trong khu vực đào tách biệt với túi đồ trong các thế giới khác
- **Tự động làm mới mỏ**: Khu vực đào sẽ tự động làm mới sau khi đào xong

## Tích hợp
- **PlaceholderAPI**: Cho phép hiển thị thông tin MetaMiner trong các plugin khác thông qua placeholders

Plugin này cung cấp một trải nghiệm đào hoàn chỉnh cho người chơi với các tính năng nâng cấp và phần thưởng NFT, làm tăng tính hấp dẫn và lưu giữ người chơi trên máy chủ.
