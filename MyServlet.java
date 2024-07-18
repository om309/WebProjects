package MyPackage;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/MyServlet")
public class MyServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String apiKey = "eb907c5732a33f2979033f7be030eb5d";

    public MyServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect("index.html");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String city = request.getParameter("city");

        // URL encode the city name
        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + encodedCity + "&appid=" + apiKey;

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Read response
            StringBuilder responseContent;
            try (InputStream inputStream = connection.getInputStream();
                 InputStreamReader reader = new InputStreamReader(inputStream);
                 Scanner scanner = new Scanner(reader)) {

                responseContent = new StringBuilder();
                while (scanner.hasNext()) {
                    responseContent.append(scanner.nextLine());
                }
            }

            // Parse JSON response
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(responseContent.toString(), JsonObject.class);

            // Extract weather data...
            long dateTimestamp = jsonObject.get("dt").getAsLong() * 1000;
            String date = new Date(dateTimestamp).toString();

            double temperatureKelvin = jsonObject.getAsJsonObject("main").get("temp").getAsDouble();
            int temperatureCelsius = (int) (temperatureKelvin - 273.15);

            int humidity = jsonObject.getAsJsonObject("main").get("humidity").getAsInt();

            double windSpeed = jsonObject.getAsJsonObject("wind").get("speed").getAsDouble();

            String weatherCondition = jsonObject.getAsJsonArray("weather").get(0).getAsJsonObject().get("main").getAsString();

            // Set request attributes
            request.setAttribute("date", date);
            request.setAttribute("city", city);
            request.setAttribute("temperature", temperatureCelsius);
            request.setAttribute("weatherCondition", weatherCondition);
            request.setAttribute("humidity", humidity);
            request.setAttribute("windSpeed", windSpeed);
            request.setAttribute("weatherData", responseContent.toString());

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle error response to the client or log it for debugging
        }

        // Forward the request to the weather.jsp page for rendering
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }
}
