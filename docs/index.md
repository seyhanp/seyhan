## seyhan projesine hoşgeldiniz

seyhan açık kaynak kodlu, kurulumu ve kullanımı kolay ücretsiz bir ön muhasebe programıdır. Birçok işletim sistemi, veritabanı, tarayıcı ve yazıcı ile sorunsuz bir şekilde çalışabilir.

seyhan projesini ücretsiz olarak indirebilirsiniz. Herhangi bir süre veya kullanım sınırlaması yoktur.

### Basit kurulum

Kurulum işlemi için öncelikle sisteminiz Java JDK 8 ya da sonrası bir sürümün kurulu olması gerekiyor. Kurulu değilse [Kurulum Adımları](http://www.seyhanproject.com/docs/#/others/install) nasıl kuracağınızı öğrenebilirsiniz.

İndirdiğiniz seyhan.zip dosyasını bilgisayarınızın herhangi bir dizinine açın. Bu dizinde bulunan (windows için) start.bat (diğer sistemler için) start.sh dosyasını çalıştırın. Tarayıcınızdan http://localhost:9000 yazdıktan sonra gelen login formunda kullanıcı : `super`, şifre : `1234` yazın, hepsi bu.

seyhan-pserver projesi hakkında detaylı bilgi için [şuraya](http://seyhanproject.com/docs/#/printing/pservice) bakabilirsiniz. Ayrıca özel kurulum ve daha fazlası için de [belgeler](http://seyhanproject.com/docs) kısmına bakabilirsiniz.

Kurulum zip dosyasıdır ve içerisinde herhangi bir exe dosya bulunmamaktadır, virüs uyarısı alırsanız dikkate almayınız. Ayrıca, programı kaldırmak için yapmanız gereken tek şey, açtığınız dizini silemektir. 

### Maliyetlerinizi düşürün

* Kullanım kısıtlaması olmadan ve herhangi bir kurulum, kullanım ve yeni yıl ücreti ödemeden seyhan'ı indirip kullanabilirsiniz.
* Genel olarak muhasebe projeleri Windows işletim sistemleri üzerine geliştiriliyor. seyhan ile artık bilgilerinizi Mac ve Linux işletim sistemlerinde de tutabilirsiniz.
* Veritabanı lisansları işletmeler için ciddi maliyet kalemleri olabiliyor. MySQL ve Postgresql gibi açık kaynak ve güçlü veritabanlarını kullanarak maliyetlerinizi azaltabilirsiniz.

### Sıkça Sorulan Sorular

* Neden masaüstü uygulaması değil?
* Uygulama web tabanlı olduğu için verilerimiz İnternet'e mi açılacak?
* Herhangi bir kısıtlaması var mı, ilerde olabilir mi?
* Ücretli servis desteği alınabilir mi?

gibi sık sorulan sorulara [şuradan bakabilirsiniz](http://seyhanproject.com/#/faq).

### Özellikleri

#### Açık kaynak
Ücretsiz olarak kullanabilirsiniz aynı zamanda projenin kaynak kodlarına da erişebilirsiniz.

#### İşletim sistemi bağımsız
Java JDK 1.8+ platformunun çalışabildiği tüm sistemlerde çalışır; linux, unix, mac, windows...

#### Veritabanı bağımsız
Popüler ilişkisel veritabanları ile çalışır; mysql, postgresql, ms-sqlserver, h2...

#### Tarayıcı bağımsız
Web tabanlıdır ve popüler tarayıcılarla uyumludur; firefox, internet explorer, chrome...

#### Kısıtlama yok
Herhangi bir kullanım kısıtlaması yoktur. Sınırlar, kullandığınız veritabanı, ağ ve bilgisayar kapasitesi kadardır.

#### Farklı yazıcı desteği
Rapor ve belgelerinizi pdf, excel ve metin dosyası olarak kaydedebilir ya da nokta vuruşlu, lazer... yazıcılardan çıktı alabilirsiniz.

#### Çok kullanıcılı
Programı aynı anda birden çok kullanıcı haklarına göre kullanabilirler.

#### Çoklu dil desteği
Arayüz ve raporlarınızda farklı dil seçneğini kullanabilirsiniz. Şuan için desteklenen diller Türkçe ve İngilizce'dir.

#### Kolay kurulum ve kullanım
Java JDK 8+ kurulu olan bir bilgisayarda sadece zip dosyasını herhangi bir dizine açıp hemen kullanmaya başlayabilirsiniz. Sahip olduğu tema ve form tasarımlarıyla kullanıcı dostu arayüzlere sahiptir.

### Modülleri

| | |
|-|-|
| _**Stok**_      | İşletmenizde alım-satımını yaptığınız ticari mallarınızın dönemsel olarak giriş, çıkış ve bakiyeleri ile bu işlemlerden doğan kar veya zararınızı bu modül yardımı ile izleyebilirsiniz. |
| _**Cari**_      | Alıcı ve Satıcılara ait devam eden borç, alacak ve bakiyelerin para birimi bazında izlendiği modüldür. Diğer modüller için ana modül konumundadır. |
| _**Kasa**_      | Nakit para giriş ve çıkışların para birimi bazında izlendiği modüldür. Farklı para birimleri ve kıymetli madenler farklı kasa tanımları altında tutulabilir. |
| _**Banka**_     | Banka hesap hareketlerinin izlendiği modüldür. |
| _**Çek/Senet**_ | Firma ve Müşterilere ait Çek ve Senet hareketlerinin izlendiği modüldür. Raporlarla çek ve senetlerinizin pozisyonu, vade yılı, ayı, günü, cari hesabı... bazında hareket ve dağılımlarını izleyebilirsiniz. |
| _**Siparş**_    | Satış öncesi alınan ve verilen siparişlerin izlendiği modüldür. Alınan, Teslim Edilen ve Bekleyen siparişlerinizi Cari hesaplar bazında alabilirsiniz. |
| _**İrsaliye**_  | Satışı kesinleşmiş olan ticari mallarınızın adrese teslimini yaparken zorunlu olan sevk irsaliyeleri bu modülde izlenir. Günü gelen irsaliyelerinizi otomatik olarak fauralaştırabilirsiniz. |
| _**Fatura**_  | Alınan ve Kesilen faturaların izlendildiği modüldür. Kullanmadan önce Stok ve Cari modüllerine ait tanımların hazırlanmış olması gerekir. |
| _**Genel**_     | Döviz tanımları ve kurları, profiller, özel ve işlem kodları gibi program genelinde kullanılan ortak tanımların yapıldığı modüldür. |
| _**Admin**_     | Kullanıcı tanımları ve yetkileri, firma tanımları ve işlemleri, belge tasarımları... gibi kullanıcı ve modüllerden bağımsız işlemlerin yapıldığı modülüdür. |

### Destekleyin

seyhan' ın devamlılığı için desteğinize ihityacı var. Diğer tüm açık kaynak projelerde olduğu gibi seyhan da topluluk desteği ile ayakta duruyor. 

#### Yazılımcıysanız
Kodları geliştirerek, bug fix yaparak destek sağlayabilirsiniz.

#### Teknoloji meraklısıysanız
seyhan hakkında blog girdileri yapabilirsiniz. Sosyal medyada seyhan hakkında olumlu yorumlar yaparak tanıtımını ve daha çok insana ulaşmasını sağlayabilirsiniz.

#### Kullanıcıysanız
Tüm özelliklerini kullanarak tam anlamıyla test edilmesini sağlayabilirsiniz.

#### Diğer
ERP konusunda bilgiliyseniz tavsiyelerde bulunabilirsiniz. Forum kısmını kullanarak topluluğa destek olabilirsiniz. Belgelendirmeye yardımcı olabilirsiniz. Bağışta bulunabilirsiniz.

### Ayrıca
seyhan hakkında daha detaylı bilgi için [belgeler kısmına](http://seyhanproject.com/docs) bakabilirsiniz.

Geliştiricilere (ve seyhan kullanıcılarına) doğrudan iletmek istediğiniz tüm soru, hata bildirimleri ve talepleriniz için [topluluk sayfalarını](https://groups.google.com/d/forum/seyhanp) kullanabilirsiniz.

