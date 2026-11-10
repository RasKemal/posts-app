# Posts App

NGAMING Case Study: offline-first read, server-first write stratejisi, Room tabanlı paging ve Jetpack Compose ile UDF (Unidirectional Data Flow) mimarisine sahip bir uygulama.

## Mimari
Clean Architecture ile üç katman; bağımlılıklar içe doğru: `ui → domain ← data`.

* **Data:** Retrofit API ve Room arasındaki veri akışını yönetir. Read'ler offline-first, write'lar server-first.
* **Domain:** Android'den bağımsız saf Kotlin: modeller, Repository interface'i, `DataResult` / `DataError` ve iş kuralı içeren `UpdatePostUseCase`.
* **UI:** MVVM + UDF. Her ekranın bir Contract'ı var: bir `UiState`, tek bir `onAction` entry point'i ve one-off `Event`'ler. Liste ekranında post'lar ayrıca `Flow<PagingData>` olarak sunulur.

## Teknik Detaylar

### Network & Sync
* **Offline-first:** UI sadece Room'dan okur, network sadece Room'a yazar. Sync UI'a veri döndürmez, sadece durum bildirir; Room değişince liste kendiliğinden güncellenir.
* **ETag:** API client'ı OkHttp disk cache kullanır. İstek `Cache-Control: max-age=0` ile gönderilir, böylece her refresh `If-None-Match` ile conditional request olur. `304 Not Modified` gelirse Room'a yazma atlanır.
* **Server-first write:** PUT/DELETE önce sunucuya gider, Room sadece başarılı olursa güncellenir. Write'lar `@ApplicationScope` içinde çalışır; kullanıcı ekrandan çıksa bile tamamlanır.
* **Local flag'ler:** JSONPlaceholder write'ları gerçekte kaydetmediği için `isLocallyModified` / `isDeleted` flag'leri, refresh'in kullanıcının değişikliklerini ezmesini engeller. DELETE'te `404` "zaten silinmiş" kabul edilir.
* **Duplicate request yok:** Açılıştaki sync sürerken pull-to-refresh yapılırsa yeni istek atılmaz, mevcut işe katılır.

### Room, Paging & Search
* **Single source of truth:** Tüm ekranlar Room'dan okur. Sunucu verisi tek bir transaction ile uygulanır; düzenlenmiş/silinmiş post'lar korunur.
* **Paging 3:** Liste Room `PagingSource` üzerinden 20'şerli sayfalarla yüklenir. API tüm veriyi (~27 KB) tek response'ta döndürdüğü için `RemoteMediator` kullanılmadı.
* **Search:** Query normalize edilir, 300 ms debounce uygulanır ve `flatMapLatest` ile önceki arama iptal edilir. Lokal Room'dan filtreleme yaptığı için offline çalışır.

### Image Loading
* **Coil 3:** Memory (%25) ve disk (50 MB) cache, loading sırasında `Modifier.Node` tabanlı shimmer.
* **Stabil görseller:** URL, post `id` ile üretilir; silme/arama sonrası görseller kaymaz ve detay ekranı aynı görseli cache'ten gösterir.

### UI & State
* **Root / Screen:** `XxxRoot` ViewModel'i alır, state'i collect eder ve event'leri işler; `XxxScreen` stateless'tır.
* **One-off event'ler:** Snackbar ve navigation `Channel` ile gönderilir, sadece ekran görünürken (`repeatOnLifecycle(STARTED)`) collect edilir.
* **Ekran içeriği:** Listenin ne göstereceği (posts / loading / empty / no results / error)  pure bir fonksiyonla belirlenir; cache'te veri varsa her zaman liste gösterilir.
* **Type-safe navigation:** Route'lar `@Serializable` sınıflar (`PostListRoute`, `PostDetailRoute(id)`).

## Proje Yapısı
```
com.example.postsapplication
├── di/            Hilt modülleri
├── domain/        Modeller, Repository interface'i, DataResult/DataError, UseCase
├── data/          Retrofit API, Room (Entity, DAO, Database), mapper'lar, Repository impl.
└── ui/
    ├── common/      UiText, ObserveAsEvents, Shimmer, SearchQuery
    ├── components/  Ortak composable'lar
    ├── navigation/  Route'lar ve NavHost
    ├── theme/
    └── screens/
        ├── list/    Contract, ViewModel, Screen, Navigation (+ components/, model/)
        └── detail/  Contract, ViewModel, Screen, Navigation
```

## Testler
53 unit test:
* **Data:** In-memory Room ile DAO testleri; MockWebServer ile Repository testleri (`If-None-Match` → 304, offline/server error'lar, server-first update/delete).
* **Domain:** `UpdatePostUseCase` (boş başlık, trim).
* **UI:** ViewModel testleri (sync, refresh, delete, debounced search, draft, save, discard dialog), ekran içeriği fonksiyonu ve UI model mapping.
* **Araçlar:** Turbine, `runTest`, `MainDispatcherRule`, mock yerine `FakePostRepository`.

## Tech Stack
Jetpack Compose (Material 3) · Coroutines & Flow · Hilt (KSP) · Room · Retrofit + OkHttp + kotlinx.serialization · Paging 3 · Coil 3 · Navigation Compose · JUnit4, Turbine, Robolectric, MockWebServer

## Trade-off'lar ve Future Improvements
* **Network paging yok:** Veri büyürse API'nin desteklediği `_start` / `_limit` ile `RemoteMediator` sadece data katmanı değiştirilerek eklenebilir.
* **UseCase sadece kural olan yerde:** Sadece repository'ye yönlendiren pass-through UseCase'ler eklenmedi.

## Gereksinimler ve Çalıştırma
* AGP 9.3'ü destekleyen güncel bir Android Studio, JDK 17+, compileSdk 37, minSdk 24.


