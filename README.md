# Final project of "Java. Основы программирования на РТФ." course on Ulearn.me.
# Подготовка.
В начале выполнения задания создадим пустой проект в среде IntelliJ, создадим Java + Maven проект, т.к фреймворк Maven облегчит нам работу с подключением сторонних библиотек, да и просто для удобства.

В процессе выполнения проекта нам понадобится так же:
1. OpenCSV - удобная и легкая в использовании библиотека-парсер CSV таблиц для языка Java. (http://opencsv.sourceforge.net/)
2. JFreeChart - библиотека для построения графика к задания № 1. (https://www.jfree.org/jfreechart/)
3. sqlite-jdbc - библиотека для  доступа к SQLite базам данным через JDBC API. (https://github.com/xerial/sqlite-jdbc)
4. SQLite - Движок баз данных SQL (https://www.sqlite.org/download.html), а так же набор sqlite-tools от туда же, для создания баз данных и удобного контроля ей из консоли.

Все эти библиотеки были подключены как зависимости в pom.xml файл проекта.

# Выполнение задания.
### Создание базы данных и пустой таблицы.
Используя скачанный нами sqlite-tools выполним набор команд для создания базы данных:
```SQLite
.open titanic.db                              // Создаем файл нашей базы данных.
CREATE TABLE titanic (                        // Создаем пустую таблицу titanic внутри.
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	sex VARCHAR(255),
	age REAL,
	fare REAL,
	embarked VARCHAR(255),
	ticket VARCHAR(255));
 ```
Наша таблица будет хранить поля id, sex, age, fare, embarked, ticket, так как для выполнения проектной работы нам понадобятся только эти данные из исходной CSV таблицы.
Причем столбец PassengerID из исходной CSV брать не будем, он представляем собой просто порядковый номер, я создаю его в таблице сам через "id INTEGER PRIMARY KEY AUTOINCREMENT", что делает его первичным ключом.

Насчет исходной таблицы с пассажирами Титаника. Она изначально находится в 3НФ так как:
1. Нет повторяющихся строк (все пассажиры - уникальные персоналии).
2. Все атрибуты простые
3. Все значения скалярные
4. У таблицы есть первичный ключ (PassengerId)
5. Все атрибуты описывают первичный ключ.
6. Нет зависимости не ключевых атрибутов от других, все зависят только от первичного ключа.

Что означает соответствие таблицы 3НФ.
### Создание класса обёртки для парсинга CSV.
1. Теперь создаем класс **Passenger**, который будет представлять каждого пассажира Титаника как объект.
Он будет представляет собой просто набор полей необходимых нам (sex, age, fare, embarked, ticket) и набор геттеров/сеттеров к ним.
Каждое поле так же будет выделено аннотацией **@CsvBindByName(column = "<Название столбца в CSV таблице>")** предоставляемой OpenCSV, которая указывает ей из какого столбца брать данные.

2. В основном классе Program:
```Java
FileReader file = new FileReader("src/main/resources/Titanic.csv");
List<Passenger> passengers = new CsvToBeanBuilder<Passenger>(file)
                .withType(Passenger.class)
                .build()
                .parse();
```
Таким образом CSVOpen создает набор объектов класса Passenger, парся исходную таблицу Titanic.csv. Каждый passenger в passengers теперь имеет поля sex, age, fare, embarked, ticket и геттер к каждому из них.
### Заполнение базы данных созданными объектами.
```Java
    public Connection conn;

    private boolean open() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.conn = DriverManager.getConnection(
                    "jdbc:sqlite:src\\main\\resources\\SQLite\\titanic.db");
            System.out.println("Successfully connected to DataBase");
            return true;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private void insert(List<Passenger> passengers) {
        try {
            for (Passenger p : passengers) {
                String query = "INSERT INTO titanic (sex, age, fare, embarked, ticket) " +
                        "VALUES ('" + p.getSex() + "', '" + p.getAge() +
                        "', '" + p.getFare() + "', '" + p.getEmbarkStation() +
                        "', '" + p.getTicket() + "')";
                Statement statement = conn.createStatement();
                statement.executeUpdate(query);
                statement.close();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
```
С помощью методов **open()** и **insert(List<Passenger> passengers)** заполним базу данных нашими созданными объектами.
open() с помощью sqlite-jdbc налаживает подключение к БД и уведомляет о корректном подлючении в консоль.
insert(List<Passenger> passengers) в цикле foreach() направляет запросы следующего типа в нашу БД:
 ```Java
 String query = "INSERT INTO titanic (sex, age, fare, embarked, ticket) " +
                "VALUES ('" + p.getSex() + "', '" + p.getAge() +
                "', '" + p.getFare() + "', '" + p.getEmbarkStation() +
                "', '" + p.getTicket() + "')";
 ```
 Т.е создает в таблице БД набор пассажира с его id, sex, age, fare, embarked, ticket данными.
 По окончании выполнения данного метода мы имеем полностью заполненную данными БД следующего вида (скриншот консоли sqlite-tools, используем команду **SELECT * FROM titanic;**):
 ![sqltools1](https://user-images.githubusercontent.com/61516790/145345347-319b45a9-de77-4b8b-87d2-562959902aaa.png)
 ![sqltools2](https://user-images.githubusercontent.com/61516790/145345393-d378a922-fe07-4d43-8ffc-de26dc45aea3.png)
 в которой у каждого пассажира соответствующие ему поля, всего их 891, как в исходной CSV-таблице.

**База данных готова для дальнейшего использования**
## Выполнение задания № 1. Постройте график цены билетов у пассажиров объеденив их по полу и колонке Embarked.
```Java
 private void firstTask() {
        try {
            Statement statement = conn.createStatement();
            String query =
                    "SELECT id, sex, fare, embarked " +
                    " FROM titanic ";
            ResultSet rs = statement.executeQuery(query);
            XYSeriesCollection dataset = new XYSeriesCollection();
            XYSeries MC = new XYSeries("Male Cherbourg"); XYSeries MQ = new XYSeries("Male Queenstown");
            XYSeries MS = new XYSeries("Male Southampton"); XYSeries FC = new XYSeries("Female Cherbourg");
            XYSeries FQ = new XYSeries("Female Queenstown"); XYSeries FS = new XYSeries("Female Southampton");
            while (rs.next()){
                int id = rs.getInt("id");
                String sex = rs.getString("sex");
                double fare = rs.getDouble("fare");
                String embarkStation = rs.getString("embarked");
                // Формируем датасет, соотнося по ID на оси oX и полу + станции на оси oY
                if (sex.equals("male") && fare != 0.0) {
                    switch (embarkStation) {
                        case "C" -> MC.add(id, fare);
                        case "Q" -> MQ.add(id, fare);
                        case "S" -> MS.add(id, fare);
                    }
                } else if (sex.equals("female") && fare != 0.0){
                    switch (embarkStation) {
                        case "C" -> FC.add(id, fare);
                        case "Q" -> FQ.add(id, fare);
                        case "S" -> FS.add(id, fare);
                    }
                }
            }
            rs.close();
            statement.close();
            // Добавляем в датасет наши серии.
            dataset.addSeries(MC); dataset.addSeries(MS);
            dataset.addSeries(MQ); dataset.addSeries(FC);
            dataset.addSeries(FS); dataset.addSeries(FQ);
            // Формируем график
            JFreeChart chart =  ChartFactory.createXYLineChart(
                    "Цена билетов пассажиров", null, "Цена", dataset,
                    PlotOrientation.VERTICAL, true, false, false);
            XYPlot plot = chart.getXYPlot();
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
            renderer.setSeriesLinesVisible (0, true);
            plot.setRenderer(renderer);
            ChartFrame chartFrame = new ChartFrame("Цены билетов пассажиров", chart, false);
            chartFrame.setVisible(true);
            chartFrame.setSize(1200,1200);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
```
1. Необходимо получить с помощью запроса к БД следующие поля id, sex, fare, embarked, что делаем с помощью SELECT ... FROM ...
2. Создаем ResultSet, который будет идти в цикле while() до тех пор, пока не пройдет все элементы таблицы в БД.
3. Далее создаем набор из 6 серий (пол + станция) MC,MQ,MS (мужчина + станции соответственно) и FC,FQ,FS (женщина + станции соответственно),
 с помощью switch() оператора распределяем людей в соответствующую им серию, добавляя в график по оси OY - цену билета, а по OX - его порядковый номер.
 в результате наш график будет иметь 6 кусочно-линейных функций, каждая из которых покажет как распределены цены билетов по портам у мужчин и женщин соответственно.
4. Результат выполнения firstTask():
 ![graph](https://user-images.githubusercontent.com/61516790/145347003-b39bedeb-1c0f-41fa-b738-c3e8313d528a.png)
 из которого мы можем например определить, что самый дорогой билет (около 512 *вероятно долларов*) был у двух мужчин и женщины, севших в Cherbourg'е.

## Выполнение задания № 2. Найдите разницу между максимальным и минимальным ценой билета у женщин от 15 до 30 лет.
```Java
     private void secondTask() {
        try {
            Statement statement = conn.createStatement();
            String query =
                    "SELECT sex, age, fare " +
                    " FROM titanic ";
            ResultSet rs = statement.executeQuery(query);
            double minFare = Double.MAX_VALUE;
            double maxFare = Double.MIN_VALUE;
            while (rs.next()){
                double fare = rs.getDouble("fare");
                double age = rs.getDouble("age");
                String sex = rs.getString("sex");
                if (((age >= 15.0 && age <= 30.0) && sex.equals("female"))
                        && fare < minFare) {
                    minFare = fare;
                }
                else if (((age >= 15 && age <= 30) && sex.equals("female"))
                        && fare > maxFare) {
                    maxFare = fare;
                }
            }
            rs.close();
            statement.close();
            System.out.println("Разница между максимальной и минимальной" +
                    " ценой билета у женщин в возрасте от 15 до 30 лет = "
            + (maxFare - minFare));
            System.out.println("Максимальная цена билета = " + maxFare);
            System.out.println("Минимальная цена билета = " + minFare);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
```
1. Необходимо получить с помощью запроса к БД следующие поля sex, age, fare, что делаем с помощью SELECT ... FROM ...
2. Создаем ResultSet, который будет идти в цикле while() до тех пор, пока не пройдет все элементы таблицы в БД.
3. С помощью оператора if() проверяем на соответствие условиям нашей задачи и находит максимальную и минимальную цену билета.
4. В консоль выводим разницу и значения билетов.
5. Результат выполнения secondTask():
 ![secondTaskConsole](https://user-images.githubusercontent.com/61516790/145348748-ff1dad56-dd75-48cb-9cda-54452bc9d8ce.png)

## Выполнение задания № 3. Найдите список билетов, мужчин в возрасте от 45 до 60 и женщин от 20 до 25.
```Java
    private void thirdTask() {
        try {
            Statement statement = conn.createStatement();
            String query =
                    "SELECT sex, age, ticket " +
                    " FROM titanic ";
            ResultSet rs = statement.executeQuery(query);
            ArrayList<String> tickets = new ArrayList<>();
            while (rs.next()){
                String sex = rs.getString("sex");
                double age = rs.getDouble("age");
                String ticket = rs.getString("ticket");
                if (sex.equals("male") && (age >= 45 && age <= 60)) {
                    tickets.add(ticket);
                }
                else if (sex.equals("female") && (age >= 20 && age <= 25)) {
                    tickets.add(ticket);
                }
            }
            rs.close();
            statement.close();
            for (String ticket : tickets) {
                System.out.println(ticket);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    } 
```
1. Необходимо получить с помощью запроса к БД следующие поля sex, age, ticket, что делаем с помощью SELECT ... FROM ...
2. Создаем ResultSet, который будет идти в цикле while() до тех пор, пока не пройдет все элементы таблицы в БД.
3. С помощью оператора if() проверяем на соответствие условиям нашей задачи и добавляем корректный номер билета в итоговый ArrayList<String> tickets.
4. С помощью цикла foreach выводим на консоль все найденные билеты.
5. Результат выполнения thirdTask():
 ![ThirdTask](https://user-images.githubusercontent.com/61516790/145349403-4df1dc49-a633-4da3-bd2f-5f3207cc3715.gif)
## Общее строение программы.
```Java
 Program program = new Program();
 if (program.open()) {
     program.insert(passengers);
     program.firstTask();
     program.secondTask();
     program.thirdTask();
     program.close();
 }
 ```
 1. Создаем экземпляр Program
 2. Последовательно выполняем open() -> insert() -> tasks_methods -> close()
 
 Метод close() выполняет функцию закрытия подлючения conn к нашей базе данных.
 ```Java
 private void close() {
        try {
            conn.close();
            System.out.println("Connection to DataBase closed");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
 ```
 
