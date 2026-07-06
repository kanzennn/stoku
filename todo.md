Here is Your ToDo, check the [] if you already finish the task and continue to next task:

1. [x] Generate scan barang masuk feature:
        1. ScanMasukScreen.kt + ScanMasukViewModel.kt
        2. CameraX + ML Kit barcode scanner composable (reusable BarcodeScanner.kt)
        3. Flow:
            - Scan → lookup SKU in products table
            - If not found: show AddNewProductForm (brand_name, product_name, category, cost_price, selling_price, quantity)
            - If found: show RestockForm (display current info, editable cost_price/selling_price/quantity)
            - On price change: insert to price_histories table
            - Save transaction with type="IN", source="SCAN", cost_price_snapshot, selling_price_snapshot
        4. Success snackbar, option to scan again
2. [x] Generate scan barang keluar feature:
        1. ScanKeluarScreen.kt + ScanKeluarViewModel.kt
        2. Reuse BarcodeScanner.kt composable from scan masuk
        3. Flow:
            - Scan → lookup SKU
            - Not found: alert dialog "Produk tidak ditemukan"
            - Stock = 0: alert "Stok habis"
            - Found: show product info + quantity input (max = current stock, validated)
            - cost_price hidden if role = kasir
            - Save transaction type="OUT", source="SCAN", snapshot prices, update stock
            - Show updated stock after save
            - Loop until user taps "Selesai"
3. [x] Generate manual input feature (owner & admin only):
        1. ManualInputScreen.kt — entry point with two options: Input Masuk / Input Keluar
        2. ManualMasukScreen.kt + ManualMasukViewModel.kt:
            - SKU text field + "Cek Produk" button
            - Not found: expand to full new product form (sku, brand_name, product_name, category, cost_price, selling_price, quantity, notes)
            - Found: show product card + restock fields (cost_price optional update, selling_price optional update, quantity, notes)
            - Save transaction source="MANUAL", type="IN"
            - Success dialog with "Input Lagi" / "Selesai"
        3. ManualKeluarScreen.kt + ManualKeluarViewModel.kt:
            - SKU text field OR search by product_name (contains match)
            - Not found / stock=0: appropriate alerts
            - Found: product info + quantity input (max validated) + notes
            - cost_price visible to owner & admin only
            - Save transaction source="MANUAL", type="OUT"
            - Success dialog with "Input Lagi" / "Selesai"
4. [x] Generate stock list feature:
        1. StockListScreen.kt + StockListViewModel.kt
        2. Searchable list (search by product_name or sku)
        3. Filter by category dropdown
        4. Sort options: name / stock / last updated
        5. Each row: sku, product_name, brand_name, category, stock, selling_price
        6. cost_price column visible to owner & admin only
        7. Low stock rows highlighted amber/red (stock < low_stock_threshold)
        8. Tap row → StockDetailScreen.kt:
            - Full product info
            - Transaction history for this SKU
            - Edit button (owner & admin only) → editable form, saves changes + price_histories entry if price changed
5. [x] Generate transaction history feature:
        1. HistoryScreen.kt + HistoryViewModel.kt
        2. Full transaction list ordered by created_at DESC
        3. Each row shows: date, sku, product_name, type (IN/OUT), source badge (Scan=blue pill / Manual=purple pill), quantity, notes
        4. cost_price_snapshot & selling_price_snapshot columns visible to owner & admin only, hidden from kasir
        5. Filters: date range picker, type (IN/OUT/ALL), source (SCAN/MANUAL/ALL), user
        6. Search by product_name or sku
        7. Tap row → HistoryDetailScreen.kt with all fields including notes
        8. Export to CSV button (owner & admin only):
            - kasir export excludes cost_price_snapshot & selling_price_snapshot columns
            - Write to Downloads folder
6. [x] Generate statistics and settings:
        1. StatisticsScreen.kt + StatisticsViewModel.kt:
            - Bar chart (Vico) daily quantity IN vs OUT
            - Date range filter: today / this week / this month / custom
            - Summary cards: total qty in, total qty out
            - Source breakdown: Scan vs Manual count
            - Top 5 most moved products (by total quantity OUT)
        2. SettingsScreen.kt + SettingsViewModel.kt (owner & admin only):
            - Global low stock threshold input (default 5, saved to DataStore)
            - App version info
            - Logout button