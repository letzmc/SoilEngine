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
    <version>1.1.3</version>
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
    implementation 'com.github.letzmc:soilengine:1.1.3'
}
```

## Hướng dẫn sử dụng Config Manager

SoilEngine cung cấp hệ thống Config Manager mạnh mẽ cho phép tự động serialize và deserialize các đối tượng Java sang/từ các file cấu hình YAML.

## Loại Config

Config Manager hỗ trợ 2 loại config chính:

### Config Tĩnh (Static Config)
- Sử dụng `.target(Class.class)` 
- Làm việc với các **static fields** trong class
- Thích hợp cho việc tạo file config chính cho plugin
- Dữ liệu được lưu trực tiếp vào static fields của class

### Config Động (Dynamic Config)  
- Sử dụng `.target(objectInstance)`
- Làm việc với instance của object
- **Thường được dùng như sub-config bên trong config tĩnh**
- Không cần khởi tạo riêng với `.create()` và `.target()`

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

#### Config Tĩnh (Static Config)
```java
@ConfigMappable  
public class MainConfig {
    public static String serverName = "MyServer";
    public static int maxPlayers = 100;
    public static List<String> allowedCommands = Arrays.asList("help", "info");
}

// Khởi tạo với Class
ConfigManager mainConfig = ConfigManager
    .create(plugin, "config.yml")
    .target(MainConfig.class);  // Sử dụng Class
```

#### Config Động (Dynamic Config) - Sub-config
```java
@ConfigMappable
public class ItemTemplate {
    @ConfigPath
    public String id = "default_item";
    public String name = "Untitled Item";
    public Material material = Material.IRON_SWORD;
    // ... các field khác
}

@ConfigMappable  
public class MainConfig {
    public static String serverName = "MyServer";
    public static int maxPlayers = 100;
    
    // Config động như sub-config
    public static ItemTemplate defaultItem = new ItemTemplate();
    public static List<ItemTemplate> itemTemplates = new ArrayList<>();
}

// Chỉ cần khởi tạo config tĩnh chính
ConfigManager mainConfig = ConfigManager
    .create(plugin, "config.yml")
    .target(MainConfig.class);  // Sub-configs sẽ được xử lý tự động
```

#### Sử dụng trong Module
```java
public class ConfigModule extends Module {
    private final ConfigManager mainConfig;

    public ConfigModule(AeroPlugin plugin) {
        super(plugin);
        
        // Chỉ cần khởi tạo config tĩnh chính
        // Config động (sub-configs) sẽ được xử lý tự động
        mainConfig = ConfigManager
                .create(plugin, "config.yml")
                .target(MainConfig.class);
    }
    
    @Override
    public void onEnable() {
        mainConfig.saveDefaults().load();
        
        // Truy cập sub-configs thông qua static fields
        String itemName = MainConfig.defaultItem.name;
        List<ItemTemplate> templates = MainConfig.itemTemplates;
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

#### Config Tĩnh - File config chính
```java
@ConfigMappable
public class MainConfig {
    @Comment("Tên server hiển thị trong tin nhắn")
    public static String serverName = "MyServer";
    
    @ConfigName("max_players") 
    public static int maxPlayers = 100;
    
    public static List<String> allowedCommands = Arrays.asList("help", "info");
    
    @ConfigPath
    public static String configVersion = "1.0";
}

// Sử dụng Config Tĩnh
ConfigManager config = ConfigManager
    .create(plugin, "config.yml")
    .target(MainConfig.class)  // Truyền Class
    .saveDefaults()  // Tạo file với giá trị mặc định
    .load();         // Load giá trị vào static fields
```

#### Config Động - Sub-config trong config tĩnh
```java
@ConfigMappable
public class ItemTemplate {
    @ConfigPath
    public String id = "default_item";
    
    @Comment("Tên hiển thị của item")
    public String name = "Untitled Item";
    
    public Material material = Material.IRON_SWORD;
    public ItemTier tier = ItemTier.B;
}

@ConfigMappable
public class GameConfig {
    @Comment("Cài đặt server")
    public static String serverName = "MyServer";
    
    @Comment("Template item mặc định")
    public static ItemTemplate defaultItem = new ItemTemplate();
    
    @Comment("Danh sách các template items")
    public static List<ItemTemplate> itemTemplates = Arrays.asList(
        new ItemTemplate() {{ id = "sword"; name = "Basic Sword"; }},
        new ItemTemplate() {{ id = "bow"; name = "Basic Bow"; }}
    );
}

// Sử dụng - chỉ cần một ConfigManager
ConfigManager config = ConfigManager
    .create(plugin, "config.yml")
    .target(GameConfig.class)  // Sub-configs được xử lý tự động
    .saveDefaults()
    .load();

// Truy cập sub-config
String defaultItemName = GameConfig.defaultItem.name;
GameConfig.itemTemplates.forEach(template -> 
    plugin.getLogger().info("Item: " + template.name)
);
```
