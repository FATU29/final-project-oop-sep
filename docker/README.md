# Docker Setup cho SEP Framework

## Yêu cầu

- Docker Desktop hoặc Docker Engine
- Docker Compose

## Cài đặt nhanh

### 1. Copy file .env

```bash
cp .env.example .env
```

### 2. Chỉnh sửa .env (tùy chọn)

Mở file `.env` và thay đổi các giá trị nếu cần:
- Database passwords
- Port numbers
- Database names

### 3. Khởi động database

```bash
# Khởi động MySQL
docker-compose up -d mysql

# Hoặc khởi động PostgreSQL
docker-compose up -d postgresql

# Hoặc khởi động cả hai
docker-compose up -d
```

### 4. Kiểm tra trạng thái

```bash
docker-compose ps
```

### 5. Xem logs

```bash
# MySQL logs
docker-compose logs mysql

# PostgreSQL logs
docker-compose logs postgresql

# Tất cả logs
docker-compose logs -f
```

## Connection Strings

Sau khi khởi động containers, sử dụng các connection strings sau:

### MySQL
```
jdbc:mysql://localhost:3306/sep_demo?useSSL=false&serverTimezone=UTC
Username: sep_user
Password: sep_password
```

### PostgreSQL
```
jdbc:postgresql://localhost:5432/sep_demo
Username: sep_user
Password: sep_password
```

## Dừng containers

```bash
# Dừng nhưng giữ data
docker-compose stop

# Dừng và xóa containers (giữ volumes)
docker-compose down

# Dừng và xóa tất cả (bao gồm volumes - mất data!)
docker-compose down -v
```

## Backup và Restore

### Backup MySQL

```bash
docker exec sep_mysql mysqldump -u sep_user -psep_password sep_demo > backup.sql
```

### Restore MySQL

```bash
docker exec -i sep_mysql mysql -u sep_user -psep_password sep_demo < backup.sql
```

### Backup PostgreSQL

```bash
docker exec sep_postgres pg_dump -U sep_user sep_demo > backup.sql
```

### Restore PostgreSQL

```bash
docker exec -i sep_postgres psql -U sep_user sep_demo < backup.sql
```

## Troubleshooting

### Port đã được sử dụng

Nếu port 3306 hoặc 5432 đã được sử dụng, thay đổi trong file `.env`:
```
MYSQL_PORT=3307
POSTGRES_PORT=5433
```

### Reset database

```bash
# Xóa volumes và khởi động lại
docker-compose down -v
docker-compose up -d
```

### Kết nối từ host

```bash
# MySQL
mysql -h localhost -P 3306 -u sep_user -psep_password sep_demo

# PostgreSQL
psql -h localhost -p 5432 -U sep_user -d sep_demo
```

## Lưu ý

- Data được lưu trong Docker volumes, sẽ không mất khi restart containers
- File `.env` không nên commit vào Git (đã có trong .gitignore)
- File `.env.example` là template, có thể commit vào Git

