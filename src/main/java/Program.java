import com.opencsv.bean.CsvToBeanBuilder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;

public class Program {
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

    private void close() {
        try {
            conn.close();
            System.out.println("Connection to DataBase closed");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

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

    public static void main(String[] args) throws FileNotFoundException {
        FileReader file = new FileReader("src/main/resources/Titanic.csv");
        List<Passenger> passengers = new CsvToBeanBuilder<Passenger>(file)
                .withType(Passenger.class)
                .build()
                .parse();

        Program program = new Program();
        if (program.open()) {
            program.insert(passengers);
            program.firstTask();
            program.secondTask();
            program.thirdTask();
            program.close();
        }
    }
}
