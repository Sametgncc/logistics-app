# Frontend entegrasyon notu: canlı takip

## Ekran durumları

| Yük durumu | Müşteride göster | Sürücüde göster |
| --- | --- | --- |
| `OPEN` / `MATCHED` | Takip kapalı | Takip kapalı |
| `IN_TRANSIT` | Harita, son konum ve güncelleme zamanı | “Konum paylaşımı açık” |
| `DELIVERED` / `CANCELLED` | Takip kapalı, son teslimat durumu | Arka plan konum servisini kapat |

Konumu haritada göstermeden önce yükün `IN_TRANSIT` olmasını bekleyin. `stale: true` ise araç konumu yerine “Konum güncellenemiyor / son güncelleme …” bilgisi gösterin.

## Taşıma başlatma ve bitirme

Teklif kabul edildikten sonra pending eşleşme listesi bu kaydı döndürmez. Sürücü, teklif cevabındaki `loadId` ile kabul edilen eşleşmeyi alır:

`GET /load-matches/loads/{loadId}/accepted`

Bu endpoint yalnızca ilgili müşteri veya sürücüye açıktır. Dönen `id`, `matchId`'dir.

```text
PATCH /load-matches/{matchId}/start-transit
PATCH /load-matches/{matchId}/complete-delivery
```

`start-transit` başarılı olduğunda sürücü uygulamasında arka plan konum servisini başlatın. `complete-delivery` başarılı olduğunda derhal durdurun ve yerel konum kuyruğunu temizleyin.

## Sürücü: konum gönderme

```http
POST /route-plans/{routePlanId}/location
Authorization: Bearer <jwt>
Content-Type: application/json
```

```json
{
  "latitude": 40.9876543,
  "longitude": 29.1234567,
  "accuracyMeters": 8.0,
  "speedKph": 65.0,
  "headingDegrees": 135.0,
  "capturedAt": "2026-06-21T08:40:00Z"
}
```

Çağrı yalnızca sürücünün kendi `IN_PROGRESS` rotası ve en az bir `IN_TRANSIT` yükü varken kabul edilir. Uygulama bunu 15–30 saniyede bir veya anlamlı konum değişiminde göndermeli; çevrimdışıyken güvenli bir yerel kuyrukta tutup bağlantı döndüğünde yeniden denemelidir.

## Müşteri: ilk konum ve canlı güncelleme

Sayfa açılırken son konumu alın:

```http
GET /loads/{loadId}/tracking
Authorization: Bearer <jwt>
```

Müşteri sadece kendi yükünü çağırabilir. Başarılı cevap örneği:

```json
{
  "loadId": 42,
  "routePlanId": 18,
  "latitude": 40.9876543,
  "longitude": 29.1234567,
  "accuracyMeters": 8.0,
  "speedKph": 65.0,
  "headingDegrees": 135.0,
  "capturedAt": "2026-06-21T08:40:00Z",
  "receivedAt": "2026-06-21T08:40:01Z",
  "stale": false
}
```

Ardından mevcut SockJS/STOMP bağlantısında `CONNECT` native header'ına `Authorization: Bearer <jwt>` ekleyip şu kullanıcıya özel kanala abone olun:

```text
/user/queue/tracking
```

Mesaj gövdesi, yukarıdaki takip cevabıyla aynıdır. Aynı müşteri birden fazla yük takip edebileceği için güncellemeyi mutlaka `loadId` ile doğru haritaya uygulayın.

## Hata yönetimi

- `400`: yük/rota takip için aktif değil veya konum verisi geçersiz. Takip ekranını kapatın ya da yeniden deneyin.
- `403`: kullanıcı rota sürücüsü ya da yük müşterisi değil. Konumu asla göstermeyin.
- `404`: sürücü henüz konum göndermedi. Haritada boş durum ve bekleme mesajı gösterin.
- Ağ hatasında: sürücünün konum gönderimini kuyruklayın; müşterinin WebSocket bağlantısını üstel gecikmeyle yeniden bağlayın.
