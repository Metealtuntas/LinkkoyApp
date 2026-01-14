<div align="center">

# 🔗 Linkkoy
**Modern Link ve Yer İmi Yönetim Asistanınız**

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-purple?style=flat&logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat&logo=android)
![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?style=flat&logo=firebase)
![License](https://img.shields.io/badge/License-MIT-green?style=flat)


</div>

---


https://github.com/user-attachments/assets/8cc24f75-148d-4b3b-bf1d-9dccedae65ae



## 📖 Hakkında

**Linkkoy**, dijital kaosunuzu düzenlemek için tasarlandı. Kişisel linklerinizi, makalelerinizi ve favori sitelerinizi **klasörler ve alt klasörler** yapısıyla organize edin. Gücünü **Firebase** altyapısından alan Linkkoy, verilerinizi bulutta güvenle saklar ve tüm cihazlarınızdan erişilebilir kılar.

## ✨ Öne Çıkan Özellikler

### 🔐 Güvenlik ve Erişim
* **Firebase Authentication:** E-posta/Şifre veya Google ile güvenli giriş ve kayıt sistemi.
* **Bulut Senkronizasyonu:** Verileriniz her zaman güncel ve her yerden erişilebilir.

### 📂 Gelişmiş Klasör Yapısı
* **Sınırsız Hiyerarşi:** İhtiyacınız kadar klasör ve alt klasör oluşturun.
* **Kişiselleştirme:** Her klasör için özel ikon ve renk tanımlayarak görsel hafızanızı kullanın.
* **Tam Kontrol:** Klasörleri dilediğiniz gibi düzenleyin veya silin.

### 🔗 Akıllı Link Yönetimi
* **Otomatik Favicon:** Linki eklediğiniz anda sitenin logosu otomatik olarak çekilir ve listelenir.
* **Organizasyon:** Linkleri klasörler arasında kolayca taşıyın, kopyalayın veya düzenleyin.

### ⚡ Kullanıcı Deneyimi (UX)
* **Kaydırarak Sil (Swipe to Delete):** Klasör veya linkleri sağa/sola kaydırarak hızla temizleyin.
* **Geri Al (Undo):** Yanlışlıkla mı sildiniz? 4 saniye içinde işleminizi geri alma şansınız var.
* **Anlık Arama:** Aradığınız link veya klasörü saniyeler içinde bulun.

## 🛠️ Teknik Altyapı

Bu proje, modern Android geliştirme standartları kullanılarak **%100 Kotlin** ile geliştirilmiştir.

| Teknoloji | Açıklama |
| :--- | :--- |
| **Dil** | Kotlin |
| **UI Framework** | Jetpack Compose (Modern, deklaratif UI) |
| **Mimari** | MVVM (Model-View-ViewModel) |
| **Asenkron Yapı** | Kotlin Coroutines & Flow |
| **Backend** | Firebase Firestore (NoSQL Veritabanı) |
| **Auth** | Firebase Authentication |
| **Görsel Yükleme** | Coil (Async Image Loading) |
| **Navigasyon** | Jetpack Navigation for Compose |

## 🚀 Kurulum ve Çalıştırma

Projeyi yerel makinenizde çalıştırmak için aşağıdaki adımları izleyin:

1.  **Repoyu Klonlayın:**
    ```bash
    git clone [https://github.com/Metealtuntas/LinkkoyApp.git](https://github.com/Metealtuntas/LinkkoyApp.git)
    cd LinkkoyApp
    ```

2.  **Android Studio ile Açın:**
    Projeyi Android Studio'da `Open Project` diyerek açın ve Gradle senkronizasyonunun bitmesini bekleyin.

3.  **Firebase Yapılandırması (Önemli!):**
    > ⚠️ **Uyarı:** `google-services.json` dosyası güvenlik nedeniyle GitHub reposunda bulunmamaktadır.
    
    * [Firebase Console](https://console.firebase.google.com/)'a gidin ve yeni bir proje oluşturun.
    * Android uygulaması ekleyin (Paket adının projedeki `build.gradle` ile eşleştiğinden emin olun).
    * İndirdiğiniz `google-services.json` dosyasını projenin **`app/`** klasörünün içine yapıştırın.
    * Firebase Authentication kısmından "Email/Password" giriş yöntemini etkinleştirin.

4.  **Çalıştırın:**
    Uygulamayı emülatörde veya fiziksel cihazda çalıştırın.

## 🤝 İletişim





Geliştirici: **Mete Altuntaş** ve **Uğur Pişkin** Proje ile ilgili önerileriniz veya hata bildirimleriniz için [Issue](https://github.com/Metealtuntas/LinkkoyApp/issues) açabilirsiniz.
