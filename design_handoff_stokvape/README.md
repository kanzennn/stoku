# Handoff: StokVape — Aplikasi Manajemen Inventori Toko Vape

## Overview
StokVape adalah aplikasi manajemen inventori + kasir (POS) untuk toko vape. Mencakup autentikasi berbasis peran, dashboard, scan barang masuk/keluar (barcode), input manual, mode kasir (katalog → keranjang → pembayaran tunai → struk PDF), daftar stok, statistik, dan riwayat transaksi. UI sepenuhnya berbahasa Indonesia.

Spesifikasi fungsional aslinya ditulis untuk implementasi Android (Kotlin, Jetpack Compose, Room, ML Kit, Hilt). File dalam paket ini adalah **prototipe desain**, bukan kode Kotlin — lihat bagian berikutnya.

## About the Design Files
File dalam bundel ini adalah **referensi desain yang dibuat dalam HTML** — prototipe yang menunjukkan tampilan dan perilaku yang diinginkan, **bukan kode produksi untuk disalin langsung**. Tugasnya adalah **membangun ulang desain ini di environment codebase target**:

- **Jika target Android (sesuai spec asli):** implementasikan dengan Kotlin + Jetpack Compose, Room (SQLite lokal), ML Kit Barcode + CameraX, Navigation Compose, Hilt (DI), DataStore (settings/sesi), Coroutines + Flow. Arsitektur MVVM + Repository.
- **Jika target lain (web React/Vue, dsb.):** gunakan pola & library yang sudah ada di codebase tersebut.

Jangan kirim HTML ini sebagai produk akhir. Gunakan sebagai sumber kebenaran untuk layout, warna, tipografi, copy, dan alur interaksi.

> ⚠️ **Format `.dc.html`**: file desain memakai sebuah micro-framework internal ("Design Components") — template HTML dengan lubang `{{ }}` dan satu kelas `class Component extends DCLogic`. Anda **tidak perlu** framework ini di produksi. Anggap template sebagai struktur markup/CSS, dan kelas `Component` sebagai dokumentasi logika/state (lihat method `renderVals()` dan handler). `support.js` adalah runtime prototipe — **abaikan untuk produksi**.

## Fidelity
**High-fidelity (hifi).** Warna, tipografi, spacing, radius, dan interaksi sudah final. Bangun ulang UI dengan presisi memakai library/komponen yang ada di codebase target. Dua ukuran disertakan:
- **Ponsel** — `Inventory App.dc.html` (frame 366×786 konten, bottom-nav + bottom sheets).
- **Tablet** — `Inventory App Tablet.dc.html` (frame 1180×840, sidebar nav + layout dua-panel + modal).

Keduanya berbagi logika & data yang identik; hanya tata letak yang berbeda. Pada Android, perlakukan ini sebagai layout responsif: bottom navigation + layar penuh di kompak; navigation rail/drawer + master-detail di expanded (tablet).

## Roles & Access
Tiga peran, dipilih saat login (demo memungkinkan ganti peran cepat):
- **Owner** (Budi): akses penuh — semua fitur, lihat harga modal & kolom harga di log, pengaturan, kelola.
- **Admin** (Sari): kelola inventori — scan masuk/keluar, input manual, lihat stok & statistik, lihat harga modal, pengaturan.
- **Kasir** (Dewi): hanya scan keluar + mode kasir + lihat stok. **Harga modal disembunyikan.** Tidak bisa scan masuk, input manual, atau buka pengaturan.

Aturan visibilitas (dipakai di seluruh layar):
- `canManage = role ∈ {owner, admin}` → tombol Scan Masuk, Input Manual, Edit Produk, Pengaturan, nav Pengaturan/Input Manual.
- `canSeeCost = role ∈ {owner, admin}` → kolom/field Harga Modal di stok, detail, riwayat, flow, ekspor CSV.

## Design Tokens

### Colors
| Token | Hex | Penggunaan |
|---|---|---|
| Primary / accent (green) | `#0E9F6E` | Tombol utama, FAB, highlight aktif, bar chart masuk |
| Primary dark (teks hijau) | `#0B7A52` | Teks harga jual, label di atas bg hijau muda |
| Primary bg (green tint) | `#E7F6EF` | Background ikon/badge sukses, chip nav aktif |
| Danger (red) | `#C42B2B` | Keluar/out, stok habis, logout |
| Danger bg | `#FDECEC` | Background ikon keluar/alert |
| Danger border | `#F5D2D2` | Border kartu alert |
| Warning (amber) | `#D97706` / teks `#8A5A09` | Stok menipis (angka & teks) |
| Warning bg | `#FBF0E0`, border `#F3DEB6`, ikon `#F5C877`/teks `#7C4A03` | Kartu alert stok menipis |
| Cash accent (amber) | teks `#B07A12`, bg `#FFF1D9` | Ikon "Rp" mode kasir |
| Blue (Scan) | teks `#1D4ED8`, bg `#E7EEFD` | Pill sumber "Scan" |
| Purple (Manual) | teks `#6D28D9`, bg `#F0EAFB` | Pill sumber "Manual" |
| Scan green (kamera) | `#2BD49B` | Sudut & garis pemindai |
| Ink / teks utama | `#15171C` | Heading, angka besar |
| Teks sekunder | `#454953` | Label form, ikon netral |
| Teks muted | `#6B7280` | Subjudul, deskripsi |
| Teks placeholder/abu | `#9AA0AA` | Meta, hint, ikon nonaktif |
| App background | `#F4F5F6` | Latar layar di dalam frame |
| Surface / card | `#FFFFFF` | Kartu, panel, sheet |
| Surface alt | `#F7F8F9` | Tombol sekunder, baris di dalam kartu |
| Border halus | `#ECEEF1` | Border kartu |
| Border garis | `#E6E8EC` / `#E2E4E8` | Border input & tombol |
| Divider | `#F1F2F4` / `#F4F5F6` | Garis pemisah baris tabel/list |
| Device bezel | `#0B0B0D` | Bingkai mockup (kosmetik, bukan UI app) |
| Selected pill (peran/filter) | bg `#15171C`, teks `#fff` | Chip filter & toggle peran aktif |

### Typography
- **Font family:** `Manrope` (Google Fonts, weights 400/500/600/700/800). Fallback `sans-serif`.
  - Di Android gunakan padanan terdekat (mis. font Manrope dari Google Fonts via `downloadable fonts`, atau Inter/Plus Jakarta Sans jika Manrope tak tersedia).
- **Struk (receipt):** monospace — `'Menlo','Consolas','Courier New',monospace`.
- **Skala (px):**
  - Angka stat besar: 28–34, weight 800, letter-spacing −1px
  - Judul layar (H1): 22, weight 800, letter-spacing −0.4px
  - Judul kartu/section: 14–16, weight 700–800
  - Body / item: 13–14, weight 600–700
  - Label form: 12.5–13, weight 600
  - Meta/caption: 11–12, weight 500–600
  - Total bayar (modal): 30–34, weight 800
- **text-overflow ellipsis** dipakai untuk nama produk panjang (1 baris) dan judul kartu katalog (2 baris, `-webkit-line-clamp:2`).

### Spacing & Radius
- Radius: input/tombol `12–13px`, kartu `14–18px`, ikon-kotak `9–14px`, sheet `26px` (atas), frame layar tablet `22px`, pill/chip `20px` (penuh), avatar/ikon bulat `50%`.
- Padding kartu umum: `16–18px`. Padding layar: ponsel `18px` horizontal; tablet konten `26–30px`.
- Gap grid kartu: `11–14px`. Gap list: `9px`.
- Shadow: kartu nyaris flat (hanya border). FAB hijau: `0 8px 18px -4px rgba(14,159,110,.5)`. Bottom bar kasir: `0 -6px 18px -10px rgba(0,0,0,.12)`. Toast: `0 12px 28px -8px rgba(0,0,0,.4)`. Modal & sheet tanpa shadow berat (mengandalkan overlay).
- Overlay modal/sheet: `rgba(10,11,13,.4–.45)`.

### Animations
- `fade` 0.2–0.3s ease — transisi layar.
- `sheetIn` (ponsel) 0.28s `cubic-bezier(.2,.8,.2,1)` — bottom sheet naik dari bawah.
- `pop` (tablet) 0.25s ease — modal muncul (opacity + scale .96→1).
- `toastIn` 0.3s `cubic-bezier(.2,.8,.2,1)` — snackbar.
- `scanline` 2.4s ease-in-out infinite — garis hijau pemindai bergerak naik-turun (12%↔84%).

## Screens / Views

> Catatan navigasi: **Ponsel** memakai bottom-nav 5 ikon (Beranda, Stok, Scan[FAB tengah], Statistik, Riwayat) + bottom sheet untuk aksi/profil; detail & flow adalah layar penuh. **Tablet** memakai sidebar kiri (230px) berisi nav + kartu profil + toggle peran + logout; detail, flow, pembayaran, dan struk adalah **modal terpusat**; kasir adalah **layout dua-panel** (katalog kiri, keranjang kanan menetap).

### 1. Login
- **Purpose:** masuk & memilih peran.
- **Ponsel layout:** terpusat vertikal, padding 30px. Logo kotak hijau 56px (radius 16) dengan ikon "kotak" putih. Judul "StokVape" 28/800. Subjudul muted. Input Username & Password (radius 13, border `#E2E4E8`, padding 14×16). Tombol "Masuk" hijau penuh. Divider "DEMO — pilih peran". Tiga tombol peran (Budi/Owner, Sari/Admin, Dewi/Kasir) dengan avatar inisial berwarna (Owner hijau `#E7F6EF`/`#0B7A52`, Admin biru `#E7EEFD`/`#1D4ED8`, Kasir ungu `#F0EAFB`/`#6D28D9`).
- **Tablet layout:** split 45% / 55%. Panel kiri hijau `#0E9F6E` penuh: logo + headline besar putih ("Kelola inventori toko vape Anda.") + deskripsi + footer. Panel kanan: form (380px) identik fungsinya, tiga tombol peran berdampingan.
- **Behavior:** klik tombol peran → login langsung sebagai peran itu. Tombol "Masuk" → login sebagai Owner (demo). Setelah login → Dashboard.

### 2. Dashboard
- **Purpose:** ringkasan + aksi cepat + transaksi terakhir.
- **Komponen:**
  - Header: sapaan + nama depan (22/800). Ponsel: avatar inisial (buka profil sheet). Tablet: tanggal hari ini.
  - **4 kartu stat** (grid 2×2 ponsel / 4 kolom tablet): Total SKU, Total Stok, Stok Menipis (angka amber `#D97706`), Transaksi Hari Ini. Angka 28–30/800.
  - **Alert stok menipis** (jika ada): kartu amber penuh, ikon "!", teks "{n} produk di bawah ambang stok minimum", klik → Stok (sort by stok).
  - **Aksi Cepat:** Scan Masuk (canManage), Scan Keluar (semua), Input Manual (canManage), Kasir (semua), Buka Mode Kasir (tablet). Ponsel: grid 3 ikon-tombol vertikal. Tablet: panel kanan daftar tombol baris.
  - **Transaksi Terakhir:** 4–5 baris terakhir; tiap baris ikon panah ↓ (masuk, hijau) / ↑ (keluar, merah), nama produk, "SKU · tanggal · user", qty `+n`/`−n`. Klik → detail transaksi. Link "Semua ›" → Riwayat.

### 3. Scan Barang Masuk (canManage)
Flow modal/layar:
1. **Kamera** (overlay gelap `#111317`): viewfinder 240×240 (ponsel) / 230×200 (tablet) dengan 4 sudut hijau + garis pemindai beranimasi. Karena ini prototipe, ada daftar **"Simulasi scan — pilih barcode"**: 3 produk contoh + opsi **"Produk baru (SKU tak terdaftar)"**. Di Android, ganti dengan CameraX + ML Kit; hasil decode SKU memicu langkah berikut.
2. **Hasil:**
   - **SKU ditemukan:** kartu produk (nama, brand, SKU, stok kini, harga jual, harga modal jika canSeeCost) + field editable **Harga Modal** & **Harga Jual** + stepper **Quantity ditambah** + tombol hijau "Tambah Stok". Jika harga berubah → catat ke `price_histories`.
   - **SKU tak ditemukan:** form **produk baru** — banner biru "SKU belum terdaftar", field: SKU (prefilled, editable), Nama Brand, Nama Produk, Kategori (chip pilih: Device/Liquid/Coil/Baterai/Aksesori), Harga Modal, Harga Jual, Quantity Awal (stepper), tombol "Simpan Produk Baru". Membuat product + transaction (source SCAN, IN).
3. **Sukses:** layar/modal centang hijau, judul "Stok Berhasil Ditambah" / "Produk Baru Disimpan", ringkasan (Produk, Jumlah Masuk, Stok Sekarang), tombol "Scan Lagi" + "Selesai".

### 4. Scan Barang Keluar (semua peran)
Sama seperti di atas tetapi OUT:
- Simulasi scan menyertakan opsi uji **"Produk stok habis"** (VP-008) & **"Barcode tak dikenal"** (XX-999).
- **Tak ditemukan** → alert merah "Produk tidak ditemukan". **Stok = 0** → alert "Stok habis". Keduanya + tombol "Scan Produk Lain".
- **Ditemukan:** kartu produk (Harga Modal disembunyikan untuk Kasir) + stepper **Quantity keluar** dengan **maks = stok** + tombol merah "Konfirmasi Keluar". Simpan transaksi OUT (snapshot cost & sell), kurangi stok.
- Sukses → "Barang Berhasil Keluar".

### 5. Input Manual (canManage)
- **Manual Home:** dua pilihan kartu besar — **Input Masuk** (ikon ↓ hijau) & **Input Keluar** (ikon ↑ merah).
- **Input Masuk / Keluar:** memakai flow form yang sama seperti scan, tetapi tanpa kamera (langsung ke form), `source = MANUAL`, dan menampilkan field **Keterangan (opsional)**. Transaksi manual diberi flag MANUAL → tampil pill ungu "Manual" di Riwayat, dan keterangan tersimpan & tampil di detail.
- Tombol sukses: "Input Lagi" / "Selesai".

### 6. Mode Kasir (POS) — semua peran
Inti fitur kasir gaya Indomaret.
- **Ponsel:** layar penuh — header (back, "Kasir", tombol scan ⊡, avatar), search, **grid katalog 2 kolom**. Tiap kartu: **image-slot** (placeholder foto produk, bisa diisi user via drag-drop), nama (2 baris), harga jual, label stok, dan tombol "＋ Tambah" → berubah jadi stepper − qty + dengan badge jumlah di pojok gambar. Produk habis → overlay "STOK HABIS" + tombol "Habis" nonaktif. Bar bawah menetap: ringkasan item + total + link "Lihat keranjang" → **sheet keranjang** (ubah qty/hapus) + tombol Bayar.
- **Tablet:** **dua panel**. Kiri: header + search + **grid katalog 3 kolom** (kartu sama, image-slot 118px). Kanan (372px, menetap): panel **Pesanan** — badge jumlah item, daftar baris keranjang (nama, subtotal, stepper, hapus), footer Total + tombol Bayar. Keranjang kosong → empty state.
- **Sumber per item:** ditambah via tombol = `MANUAL`; via scan = `SCAN` (mempengaruhi pill di Riwayat).
- **Scan ke keranjang:** tombol scan membuka kamera (mode `kasirScan`); memilih barcode menambah item ke keranjang & kembali ke kasir dengan toast konfirmasi.

### 7. Pembayaran (dari Kasir)
Dipicu tombol "Bayar" (aktif hanya jika keranjang ada isi).
- **Ponsel:** layar penuh. **Tablet:** modal 600px dua kolom.
- **Komponen:** kartu **Total Tagihan** (bg gelap `#15171C`, angka putih besar) + jumlah item. **Metode Pembayaran:** **Tunai** (aktif, terpilih, centang hijau) + **QRIS** & **Kartu** (nonaktif, badge "Segera", opacity .55). **Uang Diterima:** input numerik dengan prefix "Rp" + **chip cepat** (Uang Pas, pembulatan ke 5rb/50rb, 50/100/150/200rb — difilter ≥ total). **Kembalian:** dihitung live (`uang − total`), hijau jika positif, abu jika belum cukup. Tombol "Konfirmasi Pembayaran" aktif hanya jika `uang ≥ total`.
- **Behavior:** konfirmasi → buat transaksi OUT per item (snapshot harga), kurangi stok, generate `TRX-xxxxxx`, simpan total/tunai/kembalian → layar Struk.

### 8. Struk / Receipt
- Tampilan thermal-print (monospace) dalam container `id="struk"`: header toko (STOKVAPE, alamat, telepon), garis putus-putus, No transaksi + Tanggal + Kasir, daftar item (nama, "qty x harga", subtotal), TOTAL (14/800), Tunai, Kembalian, footer "Terima kasih…".
- Tombol **"Simpan Struk (PDF)"** memanggil `window.print()`. **Print CSS** menyembunyikan semua kecuali `#struk` dan memaksanya lebar 280px tanpa border/radius → praktis "save as PDF". Di Android, ganti dengan generate PDF (mis. `PdfDocument`/`PrintManager`) atau cetak ke printer thermal.
- Tombol "Transaksi Baru" (kembali ke kasir) & "Kembali ke Beranda".

### 9. Stock List
- **Ponsel:** judul + search + chip kategori (horizontal scroll: Semua/Device/Liquid/Coil/Baterai/Aksesori) + baris "{n} produk" & tombol sort (cycle: Nama→Stok→Terbaru) + **list kartu**. Kartu: kotak kategori (4 huruf), nama, "brand · SKU", harga jual, dan di kanan angka stok + tag ("stok"/"menipis"). Stok < threshold → warna amber.
- **Tablet:** search + tombol sort sebaris + chip kategori + **tabel** (header: Produk / Kategori / Stok / Harga Jual / Harga Modal[canSeeCost]). Baris klik → detail.
- **Detail produk** (layar penuh ponsel / modal tablet): kartu ringkas (badge kategori, nama, brand·SKU, angka stok besar berwarna), grid Harga Jual / Harga Modal[canSeeCost] / Ambang Minimum / Diperbarui, tombol "Edit Info Produk" (canManage, demo → snackbar), lalu **Riwayat Transaksi** produk itu (sampai 6 baris).

### 10. Low Stock Alert / Settings
- Ambang default **5**, global, tersimpan (DataStore di Android). Produk dengan `stock < threshold` ditandai amber & dihitung di kartu "Stok Menipis" + alert dashboard.
- **Pengaturan** (canManage): kartu "Ambang Stok Minimum Global" dengan stepper besar (− angka + ), deskripsi. (Ponsel juga menampilkan versi & logout di layar ini.)

### 11. Statistics (semua peran)
- Chip rentang: Hari Ini / Minggu Ini(default aktif) / Bulan Ini / Kustom (demo → toast).
- **Bar chart** "Masuk vs Keluar / hari" (7 hari, label tanggal 12–18): dua batang per hari — hijau `#0E9F6E` (masuk) & merah-muda `#E2756F` (keluar), tinggi relatif terhadap max. Legenda. Tinggi area: 140px ponsel / 220px tablet.
- **Total Masuk** (kartu hijau) & **Total Keluar** (kartu merah).
- **Sumber Transaksi:** dua kotak — Scan (biru) & Manual (ungu) dengan hitungan.
- **Top 5 Produk Terlaris:** baris peringkat (badge nomor gelap), nama, qty, dan progress bar hijau (relatif terhadap top-1).

### 12. History / Transaction Log
- **Ponsel:** judul + tombol "↓ CSV" + search + chip filter (Semua/Masuk/Keluar/Scan/Manual) + **list kartu**: ikon panah berwarna, nama, pill sumber (Scan biru / Manual ungu) + "tanggal · user", qty `+n`/`−n`. Klik → detail.
- **Tablet:** judul + "↓ Ekspor CSV" + search + chip filter sebaris + **tabel** (Produk / Sumber / Tanggal / User / Qty).
- **Ekspor CSV:** untuk Owner/Admin menyertakan kolom Harga Modal & Harga Jual; Kasir tidak (demo → snackbar). Implementasi nyata: generate file CSV.
- **Detail transaksi** (layar/modal): ikon besar + qty unit + nama + pill Sumber & Tipe; lalu daftar baris: SKU, Produk, Tanggal, Jumlah, Sumber, User, Harga Modal & Harga Jual (canSeeCost), Keterangan.

## Interactions & Behavior
- **Navigasi:** ponsel via bottom-nav + sheets + push layar penuh; tablet via sidebar + modal overlay (klik backdrop menutup detail; gunakan `stopPropagation` di konten modal).
- **Stepper qty:** minimal 1; pada OUT/kasir dibatasi **maks = stok**. Menurunkan ke 0 di keranjang menghapus baris.
- **Keranjang:** tambah item yang sama menaikkan qty (sampai stok); checkout mengosongkan keranjang.
- **Pembayaran:** input uang difilter angka saja; kembalian live; konfirmasi diblokir jika `uang < total`.
- **Toast/snackbar:** muncul 2.2 detik lalu hilang.
- **Role switch (demo):** mengganti peran kembali ke Dashboard dan menerapkan ulang semua aturan visibilitas. Di produksi, peran berasal dari sesi login (DataStore) — hilangkan switcher demo.
- **Animasi:** lihat bagian Animations di Design Tokens.

## State Management
State yang relevan untuk produksi (di prototipe semua di satu komponen; di Android pecah ke ViewModel per layar + Repository):
- `user { id, role, name }` — sesi (persist via DataStore). `role ∈ {owner, admin, kasir}`.
- `products[]` — sumber data (Room: tabel `products`).
- `transactions[]` — log (Room: `transactions`), terurut terbaru dulu.
- `price_histories` — dicatat saat harga berubah pada restock.
- `threshold` — ambang global (DataStore).
- **Stok:** UI form/search/chip/sort, keranjang (`cart[{sku, qty, via}]`), `payCash`, `kasirDoneInfo {total, items, lines, cash, change, trxId, cashier, time}`, state flow (`flowMode, flowStage, flowFound, flowQty, fCost, fSell, fNotes, fSku, fBrand, fName, fCat`).
- **Turunan (computed):** total/low-stock count/today's txn, filter & sort daftar, data chart, top-5, total keranjang, kembalian, daftar kategori.
- **Aturan transaksi:** setiap IN/OUT membuat record dengan **snapshot** `cost_price_snapshot` & `selling_price_snapshot` dan `source` (SCAN/MANUAL), `user_id`, `created_at`. Stok produk di-update atomik bersama insert transaksi.

## Database Schema
Schema Room final ada di spec asli (entities `users`, `products`, `transactions`, `price_histories`). Patuhi nama kolom `@ColumnInfo` di sana. Harga dalam **IDR sebagai Long** (tanpa desimal). Format tampilan: `Rp ` + `toLocaleString('id-ID')` (pemisah ribuan titik).

## Assets
- **Tidak ada gambar produk yang disertakan.** Kartu katalog kasir memakai komponen `image-slot` (placeholder drag-drop di prototipe). Di produksi, ganti dengan field gambar produk (URL/asset) — tambahkan kolom gambar pada produk bila diperlukan; tampilkan placeholder bila kosong.
- **Ikon:** semua ikon adalah glyph Unicode/teks (↓ ↑ ⌂ ▦ ▤ ↺ ⊡ ⌕ ✎ ⚙ ＋ − × ✓ ‹ ›). Ganti dengan set ikon codebase target (mis. Material Symbols di Android). Tidak ada SVG kustom.
- **Font:** Manrope (Google Fonts). Struk: monospace sistem.
- **Tidak ada aset brand pihak ketiga.** "StokVape" adalah nama fiktif untuk prototipe.

## Screenshots
Folder `screenshots/` berisi tangkapan layar tiap tampilan sebagai referensi visual cepat:
- `screenshots/phone/` — 14 layar ponsel: 01 Login, 02 Dashboard, 03 Daftar Stok, 04 Detail Produk, 05 Statistik, 06 Riwayat, 07 Detail Transaksi, 08 Scan (kamera), 09 Hasil Scan (form qty), 10 Mode Kasir (katalog), 11 Pembayaran, 12 Struk, 13 Input Manual, 14 Pengaturan.
- `screenshots/tablet/` — 13 layar tablet: 01 Login (split), 02 Dashboard (sidebar), 03 Kasir (dua-panel + keranjang), 04 Pembayaran (modal), 05 Struk (modal), 06 Daftar Stok (tabel), 07 Statistik, 08 Riwayat (tabel), 09 Detail Transaksi (modal), 10 Detail Produk (modal), 11 Scan (modal), 12 Input Manual, 13 Pengaturan.

Semua screenshot diambil sebagai peran **Owner** (semua kolom harga terlihat). Untuk peran Kasir, sembunyikan Harga Modal & batasi menu sesuai aturan di bagian Roles & Access.

## Files
- `Inventory App.dc.html` — prototipe **ponsel** (semua layar + logika + data).
- `Inventory App Tablet.dc.html` — prototipe **tablet** (layout sidebar + dua-panel + modal; logika & data identik).
- `image-slot.js` — komponen placeholder gambar (hanya untuk prototipe; abaikan di produksi).
- `support.js` — runtime Design Component (hanya untuk menjalankan `.dc.html` di browser; **abaikan untuk produksi**).

Cara melihat: buka file `.dc.html` di browser. Logika lengkap (rumus, aturan peran, alur) ada di blok `class Component extends DCLogic` — method `renderVals()` dan handler-nya adalah dokumentasi perilaku paling akurat.
