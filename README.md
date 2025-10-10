## Cài đặt

Package này có sẵn trên GitHub Packages dưới dạng dự án mã nguồn mở.

### Maven

Đầu tiên, thêm repository GitHub Packages vào `pom.xml` của bạn:

```xml
<repositories>
    <repository>
        <id>github</id>
        <name>GitHub letzmc Apache Maven Packages</name>
        <url>https://maven.pkg.github.com/letzmc/SoilEngine</url>
    </repository>
</repositories>
```

Sau đó thêm dependency:

```xml
<dependency>
    <groupId>com.github.letzmc</groupId>
    <artifactId>soilengine</artifactId>
    <version>1.0</version>
</dependency>
```

### Gradle

Thêm repository GitHub Packages vào `build.gradle` của bạn:

```groovy
repositories {
    maven {
        name = "GitHubPackages"
        url = "https://maven.pkg.github.com/letzmc/SoilEngine"
    }
}
```

Sau đó thêm dependency:

```groovy
dependencies {
    implementation 'com.github.letzmc:soilengine:1.0'
}
```

## Hướng dẫn sử dụng Config Manager

SoilEngine cung cấp hệ thống Config Manager mạnh mẽ cho phép tự động serialize và deserialize các đối tượng Java sang/từ các file cấu hình YAML.

### 1. Tạo Configuration Classes

```java
@ConfigMappable
public class ItemTemplatesConfig {
    @ConfigPath
    public String id = "default_item";
    
    public String name = "Untitled Item";
    public Material material = Material.IRON_SWORD;
    public int customModelData = 0;
    public HashMap<String, AttributeValue> attributes = new HashMap<>();
}
```

#### Các Annotation chính:
- `@ConfigMappable`: Đánh dấu class có thể tự động serialize/deserialize
- `@ConfigPath`: Field sẽ được điền với tên key trong config
- `@ConfigName("tên_tuỳ_chỉnh")`: Đặt tên tuỳ chỉnh cho field trong config
- `@Comment("Mô tả")`: Thêm comment cho field (chỉ hỗ trợ từ 1.18.1+)

### 2. Khởi tạo ConfigManager

```java
public class ConfigModule extends Module {
    private final ConfigManager templateConfig;
    private final ConfigManager mainConfig;

    public ConfigModule(AeroPlugin plugin) {
        super(plugin);
        
        // Tạo ConfigManager cho file item_templates.yml
        templateConfig = ConfigManager
                .create(plugin, "item_templates.yml")
                .target(ItemTemplatesConfig.class);
                
        // Tạo ConfigManager cho file config.yml mặc định  
        mainConfig = ConfigManager
                .create(plugin, "config.yml")
                .target(MainConfig.class);
    }
}
```

### 3. Các phương thức chính

#### Load config từ file:
```java
configManager.load();      // Load từ memory
configManager.reload();    // Load từ disk và cập nhật memory
```

#### Lưu config:
```java
configManager.save();          // Lưu tất cả values
configManager.saveDefaults();  // Chỉ lưu values chưa có trong config
```

#### Gọi chuỗi phương thức:
```java
ConfigManager.create(plugin, "config.yml")
    .target(MyConfig.class)
    .load()
    .save();
```

### 4. Các tính năng được hỗ trợ

- **Kiểu dữ liệu nguyên thuỷ**: String, int, boolean, double, v.v.
- **Collections**: List, Map, HashMap, v.v.
- **Enums**: Tự động serialize/deserialize
- **Đối tượng lồng nhau**: Các class được đánh dấu `@ConfigMappable`
- **Kiểu phức tạp**: Material, ItemStack (Bukkit), v.v.

### 5. Ví dụ hoàn chỉnh

```java
@ConfigMappable
public class MainConfig {
    @Comment("Tên server hiển thị trong tin nhắn")
    public String serverName = "MyServer";
    
    @ConfigName("max_players")
    public int maxPlayers = 100;
    
    public List<String> allowedCommands = Arrays.asList("help", "info");
    
    @ConfigPath
    public String configVersion = "1.0";
}

// Cách sử dụng
ConfigManager config = ConfigManager
    .create(plugin, "config.yml")
    .target(MainConfig.class)
    .saveDefaults()  // Tạo file với giá trị mặc định
    .load();         // Load giá trị từ file
```