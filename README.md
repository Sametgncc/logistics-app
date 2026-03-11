# 🚚 Logistics App Backend

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![Docker](https://img.shields.io/badge/Docker-Containerization-lightblue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blueviolet)
![Build](https://img.shields.io/badge/Build-Maven-orange)

Modern bir lojistik yönetim sistemi için geliştirilmiş **backend servisleri** bu projede yer almaktadır.

---

## 🛠 Teknolojiler

- **Java 21 & Spring Boot 3.x** – REST API geliştirme
- **PostgreSQL** – Veritabanı yönetimi
- **Docker & Docker Compose** – Containerizasyon ve kolay dağıtım
- **Spring Security & JWT** – Kimlik doğrulama ve güvenlik
- **Lombok & MapStruct** – Kod basitleştirme ve veri dönüşümleri

---

## ⚡ Başlangıç

### Gereksinimler

- Docker Desktop (Mac/Windows/Linux)
- Java 21+
- Maven

### Kurulum ve Çalıştırma

1. Projeyi klonlayın:

```bash

git clone <repo-url>
cd logistics-app

Docker ile veritabanını başlatın

cd docker
docker-compose up -d --build


Backend artık http://localhost:8080
 adresinde çalışıyor.

.env dosyası ile veritabanı bilgilerini güvenli şekilde yönetebilirsiniz.