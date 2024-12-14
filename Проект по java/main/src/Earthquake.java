// Класс, представляющий запись о землетрясении
public class Earthquake {
    private String id;
    private int depth;
    private String magnitudeType;
    private double magnitude;
    private String state;
    private String time;

    // Конструктор
    public Earthquake(String id, int depth, String magnitudeType, double magnitude, String state, String time) {
        this.id = id;
        this.depth = depth;
        this.magnitudeType = magnitudeType;
        this.magnitude = magnitude;
        this.state = state;
        this.time = time;
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getDepth() { return depth; }
    public void setDepth(int depth) { this.depth = depth; }

    public String getMagnitudeType() { return magnitudeType; }
    public void setMagnitudeType(String magnitudeType) { this.magnitudeType = magnitudeType; }

    public double getMagnitude() { return magnitude; }
    public void setMagnitude(double magnitude) { this.magnitude = magnitude; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    @Override
    public String toString() {
        return "Землетрясение{" +
                "id='" + id + '\'' +
                ", глубина=" + depth +
                ", тип магнитуды='" + magnitudeType + '\'' +
                ", магнитуда=" + magnitude +
                ", штат='" + state + '\'' +
                ", время='" + time + '\'' +
                '}';
    }
}
