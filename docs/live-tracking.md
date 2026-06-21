# Canlı sürücü takibi

Takip yalnızca yük `IN_TRANSIT` ve rota planı `IN_PROGRESS` iken açıktır. Konum paylaşımı taşıma tamamlandığında otomatik olarak API tarafından reddedilir.

## Sürücü uygulaması

Sürücü, kendi aktif rotası için aşağıdaki çağrıyı yapar:

`POST /route-plans/{routePlanId}/location`

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

Çağrı `Authorization: Bearer <jwt>` ile yapılmalıdır. Mobil uygulama, arka plan konum servisinden bu isteği yaklaşık 15–30 saniyede bir veya anlamlı konum değişiminde göndermeli; bağlantı yoksa isteği cihazda kuyruklayıp tekrar denemelidir.

## Müşteri uygulaması

Son bilinen konum:

`GET /loads/{loadId}/tracking`

Bu endpoint sadece ilgili yükün müşterisine açıktır. Cevaptaki `stale` alanı, sürücünün cihazında konumun iki dakikadan uzun süredir güncellenmediğini belirtir.

Canlı güncellemeler için uygulama mevcut STOMP/SockJS bağlantısına JWT ile bağlanıp şuraya abone olur:

`/user/queue/tracking`

Her mesaj yük kimliğini de taşır; aynı müşteri birden fazla aktif yükü takip edebilir. Yayın, herkese açık topic yerine kullanıcıya özel kuyruğa yapıldığından başka müşteriler sürücünün konumunu dinleyemez.
