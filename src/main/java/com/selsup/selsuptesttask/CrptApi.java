package com.selsup.selsuptesttask;

import com.google.gson.Gson;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.Data;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class CrptApi {

    public class CreateDocument {
        private final Bucket bucket;


        public CreateDocument(TimeUnit timeUnit, int requestLimit) {
            // по дефолту можем отправить requestLimit запросов в минуту
            Refill refill = Refill.intervally(requestLimit, Duration.ofMinutes(1));
            switch (timeUnit) {
                case SECONDS -> refill = Refill.intervally(requestLimit, Duration.ofSeconds(1));
                case HOURS -> refill = Refill.intervally(requestLimit, Duration.ofHours(1));
            }

            Bandwidth limit = Bandwidth.classic(requestLimit, refill);
            this.bucket = Bucket.builder()
                    .addLimit(limit)
                    .build();
        }

        public void createDocument(Document document, String mark) {
            if (bucket.tryConsume(1)) {
                HttpClient client = HttpClient.newHttpClient();
                Gson gson = new Gson();
                String json = gson.toJson(document);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create"))
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                CompletableFuture<HttpResponse<String>> futureResponse =
                        client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            }
        }
    }

    @Data
    public class Document implements Serializable {
        Description description;
        String documentId;
        String documentStatus;
        String documentType;
        Boolean importRequest;
        String ownerInn;
        String participantInn;
        String producerInn;
        LocalDate productionDate;
        String productionType;
        List<Product> products;
        LocalDate registrationDate;
        String registrationNumber;

    }

    @Data
    public class Description {
        String participantInn;
    }

    @Data
    public class Product {
        String certificateDocument;
        LocalDate certificateDocumentDate;
        String certificateDocumentNumber;
        String ownerInn;
        String producerInn;
        LocalDate productionDate;
        String tnvedCode;
        String uitCode;
        String uituCode;
    }

}
