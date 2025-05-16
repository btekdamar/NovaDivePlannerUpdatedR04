# NovaDivePlannerUpdated - Gaz Ayarları ve Seçimi Özellikleri Geliştirme Yol Haritası

Bu belge, NovaDivePlannerUpdated uygulamasına Gaz Ayarları ve Seçimi özelliklerinin eklenmesi için izlenecek adımları ve önemli kararları özetlemektedir.

## 1. Genel Hedef ve Temel Kararlar

-   **Hedef:** Kullanıcının 10 adet gazı yönetebileceği, özelliklerini düzenleyebileceği ve dalışlar için bu gazların kullanılabilirliğini (ON/OFF) ayarlayabileceği bir "Gazlar" ekranı ve ilgili düzenleme diyalogunu geliştirmek.
-   **Sabit 10 Gaz Slotu:** Uygulamada başlangıçta 10 adet gaz slotu bulunacak. Kullanıcılar yeni gaz ekleyemeyecek veya mevcut slotları silemeyecek, yalnızca bu slotlardaki gazların özelliklerini düzenleyebileceklerdir.
-   **Global Gaz Havuzu:** Gaz tanımları, dalış planlarından (Dive 1, Dive 2 vb. sekmeler) bağımsızdır ve uygulama genelinde geçerlidir. Üstteki "Dive" sekmeleri gaz yönetimiyle doğrudan ilişkili değildir.
-   **ON/OFF Durumu:** Her gaz satırının başında bulunan onay kutusu (checkbox), ilgili gazın genel olarak "kullanılabilir/aktif" (ON) veya "kullanılamaz/pasif" (OFF) olduğunu belirler. Bu durum, `GasEntity` içinde `isEnabled` gibi bir alanla yönetilecektir.
-   **Gaz Düzenleme:** Her gaz satırının sonunda bulunan düzenleme ikonu (`@GasRow.png` görselindeki gibi), kullanıcının ilgili gazın FO2, FHe, Gaz Tipi, PO2 Max gibi özelliklerini `@EditGasDialog.png` görseline uygun bir diyalog aracılığıyla değiştirmesini sağlar.
-   **Çekirdekten Başlama:** Geliştirme, Veri Katmanı'ndan başlayarak Alan Katmanı ve ardından Sunum Katmanı şeklinde katmanlı mimariye uygun olarak ilerleyecektir.
-   **Referans Kod İncelemesi:** `documents/refcodes/Gas.vb` dosyası, özellikle gaz hesaplamaları (MOD, HT, END Limiti, WOB Limiti), gaz adı oluşturma mantığı (örn: "AIR", "TX 18/30") ve OC/CC gaz yönetimi konularında Java implementasyonu için bir rehber olarak kullanılacaktır.
-   **Hesaplanan Değerler (END/WOB):** Gaz listesinde gösterilecek END ve WOB değerleri, kullanıcının Ayarlar ekranında tanımladığı END ve WOB alarm değerlerine göre "Derinlik Limitleri" olarak hesaplanacaktır. Yani bu değerler, o gazla alarmın çalacağı derinliği ifade eder.

## 2. Katman Bazlı Geliştirme Planı

### I. Veri Katmanı (Data Layer)

1.  **`GasType.java` (Enum):**
    *   Değerler: `OPEN_CIRCUIT`, `CLOSED_CIRCUIT`.
2.  **`GasEntity.java` (Room Entity):**
    *   Alanlar:
        *   `slotNumber` (Int, @PrimaryKey, 1-10 arası)
        *   `isEnabled` (Boolean, gazın aktif olup olmadığını belirtir, varsayılan: slot 1 için true, diğerleri için false)
        *   `gasName` (String, otomatik oluşturulacak ve kullanıcı tarafından düzenlenebilecek)
        *   `fo2` (Double, 0.07 ile 1.0 arası Oksijen oranı)
        *   `fhe` (Double, 0.0 ile 0.93 arası Helyum oranı)
        *   `po2Max` (Double, OC gazlar için PPO2 üst limiti, örn: 1.4, 1.6; CC gazlar için kullanılmaz/null)
        *   `gasType` (GasType, OC veya CC)
        *   `tankCapacity` (Double, opsiyonel tank kapasitesi, varsayılan: 0.0)
        *   `reservePressurePercentage` (Double, opsiyonel tank rezerv yüzdesi, varsayılan: 0.0)
    *   Builder deseni ile oluşturulmalıdır.
3.  **`GasDao.java` (Data Access Object Arayüzü):**
    *   `@Insert(onConflict = OnConflictStrategy.REPLACE) Completable insertOrUpdateGas(GasEntity gas);`
    *   `@Query("SELECT * FROM gases ORDER BY slotNumber ASC") Flowable<List<GasEntity>> getAllGasesSorted();`
    *   `@Query("SELECT * FROM gases WHERE slotNumber = :slotNumber") Maybe<GasEntity> getGasBySlotNumber(int slotNumber);`
    *   `@Query("UPDATE gases SET isEnabled = :isEnabled WHERE slotNumber = :slotNumber") Completable updateGasEnabledState(int slotNumber, boolean isEnabled);`
4.  **Veritabanı Ön Yüklemesi (Database Pre-population):**
    *   Room veritabanı ilk oluşturulduğunda, 10 adet varsayılan `GasEntity` örneği `GasDao` aracılığıyla tabloya eklenecektir.
        *   Örnek: Slot 1: "AIR" (FO2=0.21, FHe=0.0, OC, PO2Max=1.4, isEnabled=true).
        *   Diğer slotlar: Varsayılan Nitrox/Trimix değerleri veya pasif (isEnabled=false) halde.

### II. Alan (Domain) Katmanı

1.  **`Gas.java` (Domain Modeli):**
    *   `GasEntity`'ye benzer alanları içerir.
    *   Builder deseni kullanılmalıdır.
2.  **`GasProperties.java` (Value Object):**
    *   Hesaplanan değerleri tutar: `calculatedGasName` (String), `mod` (Double), `ht` (Double), `endLimit` (Double), `wobLimit` (Double).
3.  **`GasRepository.java` (Repository Arayüzü):**
    *   `Flowable<List<Gas>> getGasesStream();`
    *   `Maybe<Gas> getGasBySlotNumber(int slotNumber);`
    *   `Completable saveGas(Gas gas);`
    *   `Completable updateGasEnabledState(int slotNumber, boolean isEnabled);`
4.  **Kullanım Durumları (Use Cases):**
    *   `GetGasesUseCase(gasRepository)`: `Flowable<List<Gas>>` döner.
    *   `GetGasBySlotNumberUseCase(gasRepository)`: `Maybe<Gas>` döner.
    *   `SaveGasUseCase(gasRepository, calculateGasPropertiesUseCase)`: Parametre olarak `Gas` alır. `calculateGasPropertiesUseCase` ile gaz adını güncelleyip `gasRepository.saveGas()` çağırır, `Completable` döner.
    *   `UpdateGasEnabledStateUseCase(gasRepository)`: `Completable` döner.
    *   `CalculateGasPropertiesUseCase(settingsRepository)`:
        *   Girdi: `Gas` (veya `fo2`, `fhe`, `po2Max`, `gasType`), `ambientPressure` (Ayarlardan `SettingsRepository` aracılığıyla alınır), `isOxygenNarcotic` (Ayarlardan), `endAlarmValue` (Ayarlardan), `wobAlarmValue` (Ayarlardan).
        *   Çıktı: `GasProperties` nesnesi.
        *   İşlevler:
            *   Otomatik gaz adı oluşturma (`Gas.vb` referans alınarak: "AIR", "NX 32", "TX 18/45", "HX 30/70", "OXYGEN").
            *   MOD (Maksimum Operasyon Derinliği) hesaplama (OC gazlar için).
            *   HT (Hipoksik Eşik) hesaplama ("Low PO2" alanı için).
            *   END Limiti hesaplama (Ayarlardaki `endAlarmValue`'ya göre).
            *   WOB Limiti hesaplama (Ayarlardaki `wobAlarmValue`'ya göre).

### III. Sunum Katmanı (Presentation Layer - `gases` paketi)

1.  **`GasRowUiState.java` (Satır UI Durum Modeli):**
    *   Alanlar: `slotNumber` (Int), `gasTypeString` (String, "(OC)" veya "(CC)"), `isEnabled` (Boolean), `gasNameText` (String), `modText` (String), `htText` (String), `endLimitText` (String), `wobLimitText` (String).
    *   Builder deseni kullanılmalıdır.
2.  **`GasesViewModel.java`:**
    *   Bağımlılıklar: `GetGasesUseCase`, `UpdateGasEnabledStateUseCase`, `CalculateGasPropertiesUseCase`, `SettingsRepository` (veya ilgili ayar use case'leri).
    *   `MutableLiveData<List<GasRowUiState>> gasesUiState`.
    *   `MutableLiveData<Integer> navigateToEditGasEvent` (Düzenleme ekranına geçiş için slot numarasını taşır).
    *   `loadGases()`: `GetGasesUseCase`'i çağırır. Gelen `List<Gas>` ve güncel ayarlar (ortam basıncı, O2 narkotik durumu, END/WOB alarm değerleri) kullanılarak her bir `Gas` için `CalculateGasPropertiesUseCase` çağrılır ve sonuçlar `List<GasRowUiState>`'e dönüştürülerek `gasesUiState` güncellenir.
    *   `onEditGasClicked(int slotNumber)`: `navigateToEditGasEvent`'i tetikler.
    *   `onGasEnabledChanged(int slotNumber, boolean isEnabled)`: `UpdateGasEnabledStateUseCase`'i çağırır.
3.  **`GasesFragment.java`:**
    *   `GasesViewModel`'den `gasesUiState` ve `navigateToEditGasEvent`'i observe eder.
    *   `RecyclerView`'ı günceller ve düzenleme diyaloguna yönlendirme yapar.
4.  **`GasesAdapter.java` (`RecyclerView.Adapter`):**
    *   `GasRowUiState` listesini görüntüler. Onay kutusu ve düzenleme ikonu tıklamalarını yönetir.
5.  **Layout Dosyaları:**
    *   `fragment_gases.xml`: `RecyclerView` içerir.
    *   `item_gas_row.xml`: `@GasRow.png` görseline ve END/WOB düzeltmelerine uygun olarak tek bir gaz satırını tanımlar.

### IV. Gaz Düzenleme Ekranı (`EditGasDialogFragment.java`)

1.  **Tasarım:** `@EditGasDialog.png` görseline uygun olarak tasarlanır.
2.  **İşlevsellik:** Kullanıcının seçilen gazın FO2, FHe, Gaz Tipi (OC/CC), PO2 Max (OC ise), Tank Kapasitesi, Rezerv Yüzdesi gibi değerlerini düzenlemesine olanak tanır.
3.  **`EditGasViewModel.java`:**
    *   Bağımlılıklar: `GetGasBySlotNumberUseCase`, `SaveGasUseCase`.
    *   Diyalog açıldığında ilgili gazı yükler, değişiklikleri alır, giriş doğrulaması yapar ve `SaveGasUseCase` ile kaydeder.
    *   Gaz adı, FO2/FHe değiştikçe anlık olarak güncellenip gösterilebilir (opsiyonel).
4.  **Giriş Doğrulamaları:** FO2/FHe aralıkları, PO2 Max geçerliliği vb. için doğrulamalar yapılmalıdır.

## 3. Uygulanacak En İyi Pratikler

-   Katmanlı Mimari (Clean Architecture): Presentation -> Domain <- Data.
-   MVVM (Model-View-ViewModel) deseni.
-   RxJava ile asenkron işlemler ve uygun thread yönetimi.
-   Hilt ile bağımlılık enjeksiyonu.
-   Model sınıfları için Builder deseni.
-   Gereksiz karmaşıklıktan kaçınarak (No Overengineering) sadece ihtiyaç duyulan özelliklerin geliştirilmesi.
-   `dive-planner.mdc` belgesindeki genel kurallara uyum. 