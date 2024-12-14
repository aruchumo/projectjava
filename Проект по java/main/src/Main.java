import java.sql.*;
import java.util.*;
import java.io.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.*;

// Главный класс для парсинга CSV и работы с SQLite
public class Main {
    public static void main(String[] args) {
        List<Earthquake> earthquakes = new ArrayList<>();

        // Парсинг CSV файла
        try (BufferedReader br = new BufferedReader(new FileReader("Землетрясения.csv"))) {
            String line;
            br.readLine(); // Пропустить заголовок
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");

                // Обработка возможных ошибок при парсинге данных
                try {
                    String id = values[0];
                    int depth = Integer.parseInt(values[1]);
                    String magnitudeType = values[2];
                    double magnitude = Double.parseDouble(values[3]);
                    String state = values[4];
                    String time = values[5];

                    Earthquake earthquake = new Earthquake(id, depth, magnitudeType, magnitude, state, time);
                    earthquakes.add(earthquake);
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка при парсинге данных: " + Arrays.toString(values));
                    continue;  // Пропустить строку, если произошла ошибка парсинга
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка чтения CSV файла: " + e.getMessage());
        }

        // Выводим распарсенные данные
        for (Earthquake earthquake : earthquakes) {
            System.out.println(earthquake);
        }

        // Подключение к SQLite и создание таблиц
        String url = "jdbc:sqlite:earthquakes.db";

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                String createTableSQL = "CREATE TABLE IF NOT EXISTS Earthquakes ("
                        + "id TEXT PRIMARY KEY,"
                        + "depth INTEGER,"
                        + "magnitudeType TEXT,"
                        + "magnitude REAL,"
                        + "state TEXT,"
                        + "time TEXT"
                        + ");";

                Statement stmt = conn.createStatement();
                stmt.execute(createTableSQL);

                // Вставка данных в таблицу
                String insertSQL = "INSERT INTO Earthquakes (id, depth, magnitudeType, magnitude, state, time) VALUES (?, ?, ?, ?, ?, ?);";
                PreparedStatement pstmt = conn.prepareStatement(insertSQL);

                for (Earthquake earthquake : earthquakes) {
                    pstmt.setString(1, earthquake.getId());
                    pstmt.setInt(2, earthquake.getDepth());
                    pstmt.setString(3, earthquake.getMagnitudeType());
                    pstmt.setDouble(4, earthquake.getMagnitude());
                    pstmt.setString(5, earthquake.getState());
                    pstmt.setString(6, earthquake.getTime());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();

                System.out.println("Данные успешно вставлены в базу данных.");

                // SQL запросы для анализа данных
                analyzeData(conn);

                // Построение графика
                plotEarthquakeData(conn);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка базы данных: " + e.getMessage());
        }
    }

    // Метод для анализа данных и вывода результатов
    private static void analyzeData(Connection conn) {
        try {
            // Среднее количество землетрясений по годам
            String avgQuakesPerYearSQL = "SELECT strftime('%Y', time) AS Year, COUNT(*) AS QuakesPerYear FROM Earthquakes GROUP BY Year;";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(avgQuakesPerYearSQL);
            System.out.println("Среднее количество землетрясений по годам:");
            while (rs.next()) {
                System.out.println("Год: " + rs.getString("Year") + ", Землетрясений: " + rs.getInt("QuakesPerYear"));
            }

            // Средняя магнитуда для "West Virginia"
            String avgMagnitudeSQL = "SELECT AVG(magnitude) AS AvgMagnitude FROM Earthquakes WHERE state = 'West Virginia';";
            rs = stmt.executeQuery(avgMagnitudeSQL);
            if (rs.next()) {
                System.out.println("\nСредняя магнитуда для штата 'West Virginia': " + rs.getDouble("AvgMagnitude"));
            }

            // Штат с самым глубоким землетрясением в 2013 году
            String deepestQuake2013SQL = "SELECT state, MAX(depth) AS MaxDepth FROM Earthquakes WHERE strftime('%Y', time) = '2013';";
            rs = stmt.executeQuery(deepestQuake2013SQL);
            if (rs.next()) {
                System.out.println("\nШтат с самым глубоким землетрясением в 2013 году: " + rs.getString("state") + " (Глубина: " + rs.getInt("MaxDepth") + ")");
            }

        } catch (SQLException e) {
            System.out.println("Ошибка при анализе данных: " + e.getMessage());
        }
    }

    // Метод для построения графика
    private static void plotEarthquakeData(Connection conn) {
        try {
            String avgQuakesPerYearSQL = "SELECT strftime('%Y', time) AS Year, COUNT(*) AS QuakesPerYear FROM Earthquakes GROUP BY Year;";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(avgQuakesPerYearSQL);

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            // Проверка на null и добавление значений в dataset
            while (rs.next()) {
                String year = rs.getString("Year");
                int quakes = rs.getInt("QuakesPerYear");

                // Проверяем, что значения не равны null
                if (year != null && !year.isEmpty()) {
                    dataset.addValue(quakes, "Землетрясения", year);
                }
            }

            // Проверка, если данных не найдено
            if (dataset.getRowCount() == 0) {
                System.out.println("Нет данных о землетрясениях для построения графика.");
                return;
            }

            JFreeChart chart = ChartFactory.createBarChart(
                    "Среднее количество землетрясений по годам", // Заголовок графика
                    "Год", // Подпись оси X
                    "Количество землетрясений", // Подпись оси Y
                    dataset);

            // Создание и отображение графика в панель (Swing)
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(chartPanel);
            frame.pack();
            frame.setVisible(true);
        } catch (SQLException e) {
            System.out.println("Ошибка при построении графика: " + e.getMessage());
        }
    }
}
