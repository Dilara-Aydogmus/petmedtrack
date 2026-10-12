# PetMedTrack

## PetMedTrack – Mikroservis Tabanlı Evcil Hayvan Sağlık ve Tedavi Takip Sistemi

PetMedTrack, evcil hayvan sahiplerinin sahip, evcil hayvan, ilaç ve tedavi kayıtlarını yönetmesini sağlayan mikroservis tabanlı bir sağlık takip sistemidir. Proje, PetMedTrack Service ve Notification Service olmak üzere iki Spring Boot mikroservisinden oluşur.

PetMedTrack Service; kullanıcı kayıt ve giriş işlemlerini, JWT tabanlı kimlik doğrulamayı, BCrypt ile parola şifrelemeyi ve USER, VET ve ADMIN rollerine dayalı yetkilendirmeyi yönetir. Sahip, evcil hayvan, ilaç ve tedavi işlemleri için REST API sunar; doğrulama, merkezi istisna yönetimi ve Redis caching kullanır.

Notification Service, RabbitMQ üzerinden gelen mesajları tüketerek kullanıcı ve tedavi işlemlerine bağlı e-posta bildirimlerini asenkron olarak gönderir. Servislerin ayrılması, ana uygulama süreçleri ile bildirim süreçlerinin bağımsız çalışmasını sağlar.

Uygulamalar Docker ve Docker Compose ile konteynerleştirilir. Kubernetes Deployment, Service, NetworkPolicy, PersistentVolumeClaim ve Secret kaynaklarıyla Google Kubernetes Engine üzerinde çalıştırılır. SQL Server bağlantısı Tailscale üzerinden güvenli şekilde sağlanır. Jenkins CI/CD pipeline’ı Maven build, Docker image oluşturma, Docker Hub’a push etme ve GKE’ye otomatik deployment adımlarını yürütür.

**Teknolojiler:** Java, Spring Boot, Spring Security, Spring Data JPA, JWT, BCrypt, Microsoft SQL Server, Redis, RabbitMQ, REST API, Maven, Docker, Docker Compose, Kubernetes, Google Kubernetes Engine, Jenkins, Git, Tailscale, HTML, CSS, JavaScript, Postman
