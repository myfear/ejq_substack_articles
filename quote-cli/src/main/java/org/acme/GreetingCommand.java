package org.acme;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain()
public class GreetingCommand implements QuarkusApplication {

    @Override
    public int run(String... args) throws Exception {
        List<String> quotes = readQuotesFromCSV("quotes.csv");
        if (!quotes.isEmpty()) {
            Random random = new Random();
            int randomIndex = random.nextInt(quotes.size());
            System.out.println("\n💬 " + quotes.get(randomIndex) + "\n");
        } else {
            System.out.println("No quotes found.");
        }
        return 0;
    }

    private List<String> readQuotesFromCSV(String filename) {
        List<String> quotes = new ArrayList<>();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                quotes.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return quotes;
    }

}
