import com.opencsv.bean.CsvBindByName;

public class Passenger {
    @CsvBindByName(column = "Sex")
    private String sex;

    @CsvBindByName(column = "Age")
    private double age;

    @CsvBindByName(column = "Fare")
    private double fare;

    @CsvBindByName(column = "Embarked")
    private String embarkStation;

    @CsvBindByName(column = "Ticket")
    private String ticket;

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getSex() {
        return sex;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getAge() {
        return age;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }

    public double getFare() {
        return fare;
    }

    public void setEmbarkStation(String embarkStation) {
        this.embarkStation = embarkStation;
    }

    public String getEmbarkStation() {
        return embarkStation;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public String getTicket() {
        return ticket;
    }
}