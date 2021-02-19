# KeqingWangySupremacy

### Penjelasan singkat
Algoritma *greedy* yang diimplementasikan oleh bot adalah *nearest distance based greedy*, atau jarak terdekat.
Jarak terdekat yang dimaksud adalah:
1. Mencari jarak terdekat dengan cacing lawan lalu mendekatinya dan menyerangnya
2. Mengambil *Power up* jika *Health Point* cacing sudah di bawah suatu parameter, dengan yang diimplementasikan adalah 70 *health points*
3. Menghindar dari lava yang berada di dekatnya (yang akan mulai bermunculan saat *round* 100)

*Offense* dan *defense* cacing dapat dikatakan seimbang untuk strategi ini.

### Requirement
1. Java (minimal Java 8)
2. NodeJS
3. [Entelect Challenge 2019 game engine](https://github.com/EntelectChallenge/2019-Worms/releases/tag/2019.3.2)
4. [Entelect Challenge 2019 Visualizer](https://github.com/dlweatherhead/entelect-challenge-2019-visualiser/releases/tag/v1.0f1) (opsional)

### Cara menggunakan
Program ini adalah bagian dari _Entelect Challenge 2019_.
1. *Clone repository* ini
2. Jika ingin 'mengadu' dengan bot lainnya (selain *reference bot*), siapkan (nama-file)-jar-with-dependencies.jar dan bot.json. Buatlah folder baru dengan struktur sebagai berikut:
```sh
KeqingWangySupremacy/
  ├── src/
       ├── (nama file)/
                ├── target/
                      ├── (nama-file)-jar-with-dependencies.jar
                ├── bot.json
 ```
4. Jalankan program
* di Windows: jalankan `run.bat` yang berada di dalam directory ./src/
* di Linux/Mac OS: jalankan `make run` dengan directorynya sudah berada di dalam ./src/
5. Jika ingin menggunakan *visualizer*, *copy* folder, yang sesuai dengan waktunya, yang berada di dalam ./src/match-logs dan pindahkan ke dalam folder EC2019 Final v1.0f1/Matches, dan jalankan entelect-visualizer.exe yang berada di dalam folder 'EC2019 Final v1.of1'. (**Hanya berlaku di Windows**)
 
### Author
[Ariya Adinatha](https://github.com/ariyaadinatha) 13519048<br>
[Cynthia Rusadi](https://github.com/cyn-rus) 13519118<br>
[Fransiskus Febryan Suryawan](https://github.com/suggoitanoshi) 13519124

### Others
**[Aplikasi Permainan Worms](https://github.com/EntelectChallenge/2019-Worms/tree/2019.3.2)**<br>
**[Peraturan Aplikasi Permainan](https://github.com/EntelectChallenge/2019-Worms/blob/develop/game-engine/game-rules.md)**
