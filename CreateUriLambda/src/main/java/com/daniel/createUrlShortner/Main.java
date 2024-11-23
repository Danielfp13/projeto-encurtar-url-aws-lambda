package com.daniel.createUrlShortner;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main implements RequestHandler<Map<String, Object>, Map<String, String>> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final S3Client s3Client = S3Client.builder().build();

    private static final String BUCKET_NAME = System.getenv("BUCKET_NAME");
    private static final String BASE_URL = System.getenv("BASE_URL");
    private static final int EXPIRATION_TIME_SECONDS = Integer.parseInt(System.getenv("EXPIRATION_TIME_SECONDS"));

    @Override
    public Map<String, String> handleRequest(Map<String, Object> input, Context context) {
        Map<String, String> response = new HashMap<>();

        try {
            String body = input.get("body").toString();
            Map<String, String> bodyMap = parseRequestBody(body);

            String originalUrl = bodyMap.get("originalUrl");
            if (originalUrl == null || originalUrl.isEmpty()) {
                throw new IllegalArgumentException("Missing or empty 'originalUrl' field.");
            }

            long expirationTimeInSeconds = (System.currentTimeMillis() / 1000) + EXPIRATION_TIME_SECONDS;
            String shortUrlCode = generateShortUrlCode();

            saveUrlDataToS3(shortUrlCode, new UrlData(originalUrl, expirationTimeInSeconds));

            response.put("shortened-url", BASE_URL.concat(shortUrlCode));
        } catch (Exception e) {
            response.put("error", "Error processing request: " + e.getMessage());
            context.getLogger().log("Error: " + e.getMessage());
        }

        return response;
    }

    private Map<String, String> parseRequestBody(String body) throws JsonProcessingException {
        return objectMapper.readValue(body, Map.class);
    }

    private String generateShortUrlCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private void saveUrlDataToS3(String shortUrlCode, UrlData urlData) {
        try {
            String urlDataJson = objectMapper.writeValueAsString(urlData);

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(shortUrlCode + ".json")
                    .build();

            s3Client.putObject(request, RequestBody.fromString(urlDataJson));
        } catch (Exception e) {
            throw new RuntimeException("Error saving URL data to S3: " + e.getMessage(), e);
        }
    }
}
