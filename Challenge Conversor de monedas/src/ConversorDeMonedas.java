import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.InputMismatchException;
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ConversorDeMonedas {

    public String API_URL = "https://v6.exchangerate-api.com/v6/4980cbc8536ae18e596a8e18/latest/USD";
    private JsonObject tasasDeCambio;

    public enum Divisas {
        USD, ARS, BRL, COP
    }

    public void obtenerTasasDeCambio() throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        conn.disconnect();

        Gson gson = new Gson();
        tasasDeCambio = gson.fromJson(content.toString(), JsonObject.class);
    }

    public double obtenerTasaDeCambio(Divisas from, Divisas to) {
        if (from == Divisas.USD) {
            return tasasDeCambio.getAsJsonObject("conversion_rates").get(to.name()).getAsDouble();
        } else {
            double rateToUSD = tasasDeCambio.getAsJsonObject("conversion_rates").get(from.name()).getAsDouble();
            double rateFromUSD = tasasDeCambio.getAsJsonObject("conversion_rates").get(to.name()).getAsDouble();
            return rateFromUSD / rateToUSD;
        }
    }

    public double convertirDivisa(double amount, Divisas from, Divisas to) {
        double rate = obtenerTasaDeCambio(from, to);
        return amount * rate;
    }

    public static void main(String[] args) {
        ConversorDeMonedas converter = new ConversorDeMonedas();
        try {
            converter.obtenerTasasDeCambio();

            Scanner scanner = new Scanner(System.in);
            boolean continueConversion = true;

            while (continueConversion) {
                try {

                    System.out.println("Seleccione el tipo de divisa que desea convertir:");
                    for (int i = 0; i < Divisas.values().length; i++) {
                        System.out.printf("%d. %s%n", i + 1, Divisas.values()[i]);
                    }
                    int fromIndex = scanner.nextInt() - 1;
                    if (fromIndex < 0 || fromIndex >= Divisas.values().length) {
                        System.out.println("Divisa de origen no válido.");
                        continue;
                    }
                    Divisas from = Divisas.values()[fromIndex];


                    System.out.println("Seleccione la divisa en la que desea obtener la conversion:");
                    for (int i = 0; i < Divisas.values().length; i++) {
                        System.out.printf("%d. %s%n", i + 1, Divisas.values()[i]);
                    }
                    int toIndex = scanner.nextInt() - 1;
                    if (toIndex < 0 || toIndex >= Divisas.values().length) {
                        System.out.println("Divisa de destino no válido.");
                        continue;
                    }
                    Divisas to = Divisas.values()[toIndex];


                    System.out.println("Ingrese la cantidad a convertir:");
                    double amount = scanner.nextDouble();


                    double result = converter.convertirDivisa(amount, from, to);
                    System.out.printf("%.2f %s son %.2f %s%n", amount, from, result, to);

                } catch (InputMismatchException e) {
                    System.out.println("Entrada no válida. Por favor, ingrese un número.");
                    scanner.next(); // limpiar el buffer del scanner
                    continue;
                }


                System.out.println("¿Desea realizar otra conversión? (sí = 1, no = 0):");
                int continueChoice = scanner.nextInt();
                if (continueChoice != 1) {
                    continueConversion = false;
                }
            }

            System.out.println("Gracias por usar este conversor de monedas.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}