package com.nitro.ai;

import com.google.gson.*;
import okhttp3.*;
import java.util.Base64;

public class ClaudeClient {

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL   = "claude-sonnet-4-5";
    private static final MediaType JSON  = MediaType.get("application/json");

    private final OkHttpClient http;
    private final String apiKey;

    public ClaudeClient() {
        this.http   = new OkHttpClient();
        this.apiKey = System.getenv("ANTHROPIC_API_KEY");
        if (this.apiKey == null || this.apiKey.isBlank()) {
            throw new IllegalStateException(
                    "ANTHROPIC_API_KEY environment variable is not set."
            );
        }
    }

    public String sendTextMessage(String systemPrompt, String userMessage) throws Exception {
        JsonObject body = buildBody(systemPrompt, userMessage, null);
        return callApi(body);
    }

    public String sendMessageWithImage(String systemPrompt, String userMessage,
                                       byte[] imageBytes) throws Exception {
        JsonObject body = buildBody(systemPrompt, userMessage, imageBytes);
        return callApi(body);
    }

    private JsonObject buildBody(String systemPrompt, String userMessage,
                                 byte[] imageBytes) {
        JsonObject body = new JsonObject();
        body.addProperty("model", MODEL);
        body.addProperty("max_tokens", 1024);
        body.addProperty("system", systemPrompt);

        JsonArray content = new JsonArray();

        if (imageBytes != null) {
            JsonObject imageBlock = new JsonObject();
            imageBlock.addProperty("type", "image");
            JsonObject source = new JsonObject();
            source.addProperty("type", "base64");
            source.addProperty("media_type", "image/png");
            source.addProperty("data", Base64.getEncoder().encodeToString(imageBytes));
            imageBlock.add("source", source);
            content.add(imageBlock);
        }

        JsonObject textBlock = new JsonObject();
        textBlock.addProperty("type", "text");
        textBlock.addProperty("text", userMessage);
        content.add(textBlock);

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.add("content", content);

        JsonArray messages = new JsonArray();
        messages.add(userMsg);
        body.add("messages", messages);

        return body;
    }

    private String callApi(JsonObject body) throws Exception {
        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("content-type", "application/json")
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        try (Response response = http.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("Claude API error: " + response.code()
                        + " " + response.body().string());
            }
            JsonObject json = JsonParser.parseString(
                    response.body().string()
            ).getAsJsonObject();

            return json.getAsJsonArray("content")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
        }
    }
}