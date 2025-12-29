# Simple Enterprise Framework (SEP) - Java Swing Edition

## Giới thiệu

Framework SEP là một framework Java Swing được thiết kế để tạo ra các ứng dụng quản lý với các tính năng CRUD (Create, Read, Update, Delete) một cách nhanh chóng. Framework này áp dụng các mẫu thiết kế hướng đối tượng (GoF Design Patterns) để đảm bảo tính mở rộng và tái sử dụng.

## Quick Start với Docker

### 1. Setup Database (Docker)

```bash
# Copy file environment
cp env.example .env

# Khởi động MySQL
docker-compose up -d mysql

# Kiểm tra status
docker-compose ps
```

### 2. Build và chạy ứng dụng

```bash
cd "2.Source code"
mvn clean install
mvn exec:java -Dexec.mainClass="com.sep.framework.core.FrameworkApplication"
```

Connection string mặc định (MySQL):
```
jdbc:mysql://localhost:3306/sep_demo?useSSL=false&serverTimezone=UTC
Username: sep_user
Password: sep_password
```

## Cấu trúc dự án

```
SEP-Framework/
├── 1.Documents/          # Tài liệu báo cáo
├── 2.Source code/        # Mã nguồn
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/sep/
│   │   │   │       ├── framework/
│   │   │   │       │   ├── core/          # Core framework
│   │   │   │       │   ├── membership/     # Membership system
│   │   │   │       │   ├── crud/          # CRUD base forms
│   │   │   │       │   ├── ioc/           # IoC container
│   │   │   │       │   ├── database/      # Database abstraction
│   │   │   │       │   ├── codegen/        # Code generation
│   │   │   │       │   └── patterns/       # Design patterns
│   │   │   │       └── demo/              # Demo application
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
├── 3.Functions List/     # Danh sách tính năng
├── 4.Others/             # Tài liệu khác
├── docker-compose.yml     # Docker setup
├── env.example            # Environment variables template
└── README.md
```

## Yêu cầu hệ thống

- Java JDK 8 trở lên
- Maven 3.6+
- Docker (khuyến nghị) hoặc Database: MySQL, PostgreSQL, SQLite

## Cài đặt

### Với Docker (Khuyến nghị)

Xem: [1.Documents/HUONG_DAN_DOCKER.md](1.Documents/HUONG_DAN_DOCKER.md)

### Không dùng Docker

Xem: [1.Documents/HUONG_DAN_CAI_DAT.md](1.Documents/HUONG_DAN_CAI_DAT.md)

## Hướng dẫn sử dụng

Xem file `1.Documents/HUONG_DAN_SU_DUNG.md` để biết chi tiết.

## Design Patterns được sử dụng

1. **Factory Pattern** - Tạo forms và services
2. **Template Method Pattern** - Base CRUD form
3. **Strategy Pattern** - Database providers
4. **Singleton Pattern** - IoC container
5. **Observer Pattern** - Data binding
6. **Builder Pattern** - Code generation

## Tính năng

- ✅ Membership system (đăng nhập, đăng ký, phân quyền)
- ✅ Generic CRUD base form
- ✅ Custom IoC container
- ✅ Code generation từ database schema
- ✅ Data binding tự động
- ✅ Hỗ trợ nhiều database
- ✅ Docker support

## Tài liệu

- [Hướng dẫn cài đặt](1.Documents/HUONG_DAN_CAI_DAT.md)
- [Hướng dẫn Docker](1.Documents/HUONG_DAN_DOCKER.md)
- [Hướng dẫn IntelliJ IDEA](1.Documents/HUONG_DAN_INTELLIJ.md) ⭐
- [Hướng dẫn sử dụng](1.Documents/HUONG_DAN_SU_DUNG.md)
- [Design Patterns](1.Documents/DESIGN_PATTERNS.md)
- [Sơ đồ lớp](1.Documents/SO_DO_LOP.md)
- [Danh sách tính năng](3.Functions%20List/DANH_SACH_TINH_NANG.md)
- [Quick Start](QUICK_START.md)
- [IntelliJ Quick Start](INTELLIJ_QUICK_START.md)

## Tác giả

Nhóm sinh viên - Đồ án OOP Advanced
