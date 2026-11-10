# Posts App

NGAMING Case Study: offline-first stratejisi, Room tabanlı paging ve Jetpack Compose ile MVVM/UDF mimarisine sahip bir uygulama.

## Mimari
Clean Architecture ile üç katman; bağımlılıklar içe doğru: `ui → domain ← data`.

* **Data:** Retrofit API ve Room arasındaki veri akışını yönetir. Read'ler offline-first, write'lar local-only (sadece Room).
* **Domain:** Android'den bağımsız saf Kotlin: modeller, Repository interface'i, `DataResult` / `DataError`.
* **UI:** MVVM + UDF. Her ekranın bir Contract'ı var: bir `UiState`, tek bir `onAction` entry point'i ve one-off `Event`'ler. Liste ekranında post'lar ayrıca `Flow<PagingData>` olarak sunulur.

## Teknik Detaylar

### Network & Sync
* **Offline-first:** UI sadece Room'dan okur, network sadece Room'a yazar. Sync UI'a veri döndürmez, sadece durum bildirir; Room değişince liste kendiliğinden güncellenir.
* **ETag:** API client'ı OkHttp disk cache kullanır. İstek `Cache-Control: max-age=0` ile gönderilir, böylece her refresh `If-None-Match` ile conditional request olur. `304 Not Modified` gelirse Room'a yazma atlanır.
* **Local-only write:** Update ve delete doğrudan Room'a yazılır; bağlantı olmadan da çalışır ve UI anında güncellenir.
* **Local flag'ler:** Refresh tüm listeyi sunucudan yeniden uygular. `isLocallyModified` / `isDeleted` flag'leri, refresh'in kullanıcının düzenlemelerini ezmesini ve sildiği post'ları geri getirmesini engeller.
* **Duplicate request yok:** Açılıştaki sync sürerken pull-to-refresh yapılırsa yeni istek atılmaz, mevcut işe katılır.

### Room, Paging & Search
* **Single source of truth:** Tüm ekranlar Room'dan okur. Sunucu verisi tek bir transaction ile uygulanır; düzenlenmiş/silinmiş post'lar korunur.
* **Paging 3:** Liste Room `PagingSource` üzerinden 20'şerli sayfalarla yüklenir. API tüm veriyi (~27 KB) tek response'ta döndürdüğü için `RemoteMediator` kullanılmadı.
* **Search:** Query normalize edilir, 300 ms debounce uygulanır ve `flatMapLatest` ile önceki arama iptal edilir.

### Image Loading
* **Coil 3:** Memory (%25) ve disk (50 MB) cache, loading sırasında `Modifier.Node` tabanlı shimmer.
* **Stabil görseller:** URL, post `id` ile üretilir; silme/arama sonrası görseller kaymaz ve detay ekranı aynı görseli cache'ten gösterir.

### UI & State
* **Root / Screen:** `XxxRoot` ViewModel'i alır, state'i lifecycle-aware collect eder ve event'leri işler; `XxxScreen` stateless'tır.
* **One-off event'ler:** Snackbar ve navigation `Channel` ile gönderilir, sadece ekran görünürken (`repeatOnLifecycle(STARTED)`) collect edilir.
* **Ekran içeriği:** Listenin ne göstereceği (posts / loading / empty / no results / error) pure bir fonksiyonla belirlenir; cache'te veri varsa her zaman liste gösterilir. Loading göstergesi 250 ms gecikmeyle çıkar, hızlı yüklemelerde ekran yanıp sönmez.
* **Draft'lar ve process death:** Detay ekranındaki alanlar `TextFieldState` kullanır ve `SavedStateHandle` ile saklanır; kaydedilmemiş değişiklikler rotation'da ve process death'te korunur.
* **Error handling:** `DataError` kullanıcı mesajına sadece UI katmanında, `UiText` ile çevrilir; ViewModel `Context` tutmaz.
* **Type-safe navigation:** Route'lar `@Serializable` sınıflar (`PostListRoute`, `PostDetailRoute(id)`).

## Tech Stack
Jetpack Compose (Material 3) · Coroutines & Flow · Hilt (KSP) · Room · Retrofit + OkHttp + kotlinx.serialization · Paging 3 · Coil 3 · Navigation Compose · JUnit4, Turbine, MockWebServer

## Trade-off'lar ve Future Improvements
* **Local-only write, sunucuya istek yok:** Write'lar Room'a yazılır, böylece uygulama okumada olduğu gibi yazmada da offline çalışır. Gerçek bir backend'de bu yaklaşım **WorkManager** ile bekleyen değişiklikleri bağlantı geldiğinde sunucuya gönderecek şekilde implemente edilebilir; UI katmanı değişmez.
* **Network paging yok:** Veri büyürse API'nin desteklediği `_start` / `_limit` ile `RemoteMediator` sadece data katmanı değiştirilerek eklenebilir.
* **UseCase sadece kural olan yerde:** Sadece repository'ye yönlendiren pass-through UseCase'ler eklenmedi.

## Gereksinimler ve Çalıştırma
* AGP 9.3'ü destekleyen güncel bir Android Studio, JDK 17+, compileSdk 37, minSdk 24.


