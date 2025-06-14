# WhatsAppClone - Sebuah Kloning WhatsApp Fungsional

Sebuah aplikasi kloning WhatsApp yang dibangun secara native untuk platform Android menggunakan Java. Proyek ini bukan hanya sekadar tiruan antarmuka, tetapi juga mengimplementasikan berbagai fitur inti dari aplikasi perpesanan modern, termasuk chat real-time, obrolan grup, status, dan panggilan video/suara.

## 📱 Screenshot Aplikasi
| Halaman Login | Daftar Obrolan | Ruang Obrolan |
| :---: |:---:|:---:|
| ![image](https://github.com/user-attachments/assets/974bc3c9-a893-48e8-90c6-26714a6cf2a6) | ![image](https://github.com/user-attachments/assets/5d972730-f459-48ed-a129-aeba1d09712c) | ![image](https://github.com/user-attachments/assets/a518f9a1-70c2-4f34-92b7-b2ec73f7c0ef) |
| *Halaman Status* | *Halaman Panggilan* | *Pembuatan Grup* |
| ![image](https://github.com/user-attachments/assets/1b57eaac-b02a-4970-9cdb-401a5cddd732) | ![image](https://github.com/user-attachments/assets/a186c66c-f5b9-4dc8-9dc5-347be16d49cd) | ![image](https://github.com/user-attachments/assets/e046f29b-0f1c-4c5a-9ab5-67c8904afbf2) |

## ✨ Fitur Utama

Aplikasi ini memiliki serangkaian fitur yang komprehensif:

* *Otentikasi Pengguna:* Login yang aman menggunakan Akun Google.
* *Chat Real-time:*
    * Obrolan satu-lawan-satu secara instan.
    * Obrolan grup dengan banyak anggota.
    * Indikator status "Online" dan "Terakhir aktif".
    * Tanda pesan "telah dibaca" (centang biru).
* *Fitur Status:* Pengguna bisa memposting status gambar yang akan hilang secara otomatis.
* *Panggilan Video & Suara:*
    * Panggilan real-time menggunakan *Agora SDK*.
    * Layar panggilan masuk dengan opsi terima/tolak.
    * Pencatatan riwayat panggilan di halaman "Panggilan".
* *Fitur Unik Lokal:*
    * *Tombol "Kirim Pantun":* Sebuah fitur kreatif untuk mengirim pantun acak di dalam chat.
* *Manajemen Kontak & Grup:*
    * Halaman daftar kontak untuk memulai percakapan baru.
    * Alur lengkap untuk membuat grup baru: memilih anggota, memberi nama, dan foto grup.


## 🛠 Teknologi yang Digunakan

Proyek ini dibangun menggunakan tumpukan teknologi modern untuk pengembangan aplikasi Android.

*Frontend (Aplikasi Android):*
* *Bahasa:* Java
* *Arsitektur UI:* XML Layouts & Android Views
* *Library Utama:*
    * androidx.appcompat, com.google.android.material (Komponen Material Design)
    * androidx.recyclerview.widget.RecyclerView (Untuk semua daftar)
    * com.bumptech.glide:glide (Memuat gambar dari internet)
    * de.hdodenhof:circleimageview.CircleImageView (Gambar profil bulat)
    * com.github.shts:StoriesProgressView (Untuk tampilan status)

*Backend & Layanan Cloud:*
* *Firebase:*
    * *Authentication:* Untuk otentikasi pengguna dengan Google Sign-In.
    * *Cloud Firestore:* Sebagai database NoSQL real-time untuk semua data (pengguna, chat, grup, status, riwayat panggilan).
    * *Cloud Storage:* Untuk menyimpan file yang diunggah pengguna (foto profil, gambar status).
    * *Cloud Functions & FCM:* (Dalam proses) Untuk menangani notifikasi push.
* *Agora.io:*
    * *Video & Voice SDK:* Untuk menangani transmisi data panggilan video dan suara secara real-time.

## 📂 Struktur Proyek

Proyek ini telah dirapikan (refactored) dari struktur standar menjadi struktur berdasarkan fitur (feature-based) untuk skalabilitas dan kemudahan pengelolaan yang lebih baik.


/  
├── auth/ -------------> (Fitur Login)  
├── main/ -------------> (Activity & Fragment utama)  
├── chat/ -------------> (Fitur Chat 1-on-1)  
├── contacts/ ---------> (Fitur Daftar Kontak)  
├── status/ -----------> (Fitur Status)  
├── group/ ------------> (Semua fitur Grup)  
├── call/ -------------> (Fitur Panggilan)  
├── shared/ -----------> (Kode yang dipakai bersama (Adapter, Model, Utils))  
└── services/ ---------> (Layanan latar belakang (FCM))  


## 🚀 Setup & Instalasi

Untuk menjalankan proyek ini di komputer Anda:

1.  *Clone repositori ini:*
    bash
    git clone [https://github.com/](https://github.com/)dHunter1811/WhatsAppClone.git
    
2.  *Buka dengan Android Studio.*
3.  *Hubungkan ke Proyek Firebase Anda:*
    * Buat proyek baru di [Firebase Console](https://console.firebase.google.com/).
    * Tambahkan aplikasi Android dengan nama paket com.example.whatsappclone (atau sesuai dengan milik Anda).
    * Aktifkan layanan *Authentication (Google), **Cloud Firestore, dan **Cloud Storage*.
    * Unduh file google-services.json yang baru dan letakkan di dalam folder app/ di proyek Anda.
4.  *Konfigurasi Agora:*
    * Buat akun di [Agora.io](https://www.agora.io/) dan dapatkan *App ID* Anda.
    * Buka file app/src/main/res/values/strings.xml dan masukkan App ID Anda di sana.
5.  *Build dan Jalankan* aplikasi.

## ✍ Penulis

* *[Muhammad Dimas Aditya]* - [[dHunter1811](https://github.com/dHunter1811)]
