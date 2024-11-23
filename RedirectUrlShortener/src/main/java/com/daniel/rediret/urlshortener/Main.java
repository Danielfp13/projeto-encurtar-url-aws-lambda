package com.daniel.rediret.urlshortener;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Main implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final S3Client s3Client = S3Client.builder().build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String BUCKET_NAME = System.getenv("BUCKET_NAME"); // Variável adicionada


    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        context.getLogger().log("Received input: " + input);

        String pathParameters = (String) input.get("rawPath");
        String shortUrlCode = pathParameters.replace("/", "");

        context.getLogger().log("Short URL code: " + shortUrlCode);

        if (shortUrlCode == null || shortUrlCode.isEmpty()) {
            throw new IllegalArgumentException("Invalid input: 'shortUrlCode' is required.");
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(shortUrlCode + ".json")
                .build();

        InputStream s3ObjectStream;
        try {
            s3ObjectStream = s3Client.getObject(getObjectRequest);
            context.getLogger().log("S3 object fetched successfully.");
        } catch (Exception e) {
            throw new RuntimeException("Error fetching URL data from S3: " + e.getMessage(), e);
        }

        UrlData urlData;

        try {
            urlData = objectMapper.readValue(s3ObjectStream, UrlData.class);
            context.getLogger().log("Deserialized URL data: " + urlData);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing URL data: " + e.getMessage(), e);
        }

        Long currentTimeInSeconds = System.currentTimeMillis() / 1000;
        System.out.println("Current Time IN Seconds: " + currentTimeInSeconds);
        Map<String, Object> response = new HashMap<>();

        if (urlData.getExpirationTime() < currentTimeInSeconds) {
            response.put("statusCode", 410);
            response.put("body", "This URL has expired.");
            context.getLogger().log("URL has expired.");
            return response;
        }

        response.put("statusCode", 302);
        Map<String, String> headers = new HashMap<>();
        headers.put("Location", urlData.getOriginalUrl());
        response.put("headers", headers);

        context.getLogger().log("Redirecting to: " + urlData.getOriginalUrl());
        return response;
    }
}
