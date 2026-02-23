package org.example.javabot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;

public class YandexDiskService {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    // Получить случайную ссылку на файл из папки
    public static String getRandomFileUrl(String publicKey) throws IOException, InterruptedException {
        // 1. Получаем список файлов в папке
        String listUrl = "https://cloud-api.yandex.net/v1/disk/public/resources?public_key=" + publicKey;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(listUrl))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode json = mapper.readTree(response.body());

        JsonNode items = json.get("_embedded").get("items");
        if (items == null || !items.isArray() || items.size() == 0) {
            throw new RuntimeException("В папке нет файлов!");
        }

        // 2. Выбираем случайный файл
        Random random = new Random();
        JsonNode file = items.get(random.nextInt(items.size()));

        String filePath = file.get("path").asText();

        // 3. Получаем прямую ссылку на скачивание
        String downloadUrl = "https://cloud-api.yandex.net/v1/disk/public/resources/download?public_key="
                + publicKey + "&path=" + filePath;

        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(downloadUrl))
                .GET()
                .build();

        HttpResponse<String> resp2 = client.send(req2, HttpResponse.BodyHandlers.ofString());
        JsonNode json2 = mapper.readTree(resp2.body());

        return json2.get("href").asText(); // рабочая ссылка на файл
    }

    // Отправляем в Telegram
    public static void sendRandomImage(Long chatId, String publicKey, TelegramClient telegramClient) {
        try {
            String fileUrl = getRandomFileUrl(publicKey);

            SendPhoto sendPhoto = SendPhoto.builder()
                    .chatId(chatId)
                    .photo(new InputFile(fileUrl))
                    .caption("Случайная картинка 📷")
                    .build();

            telegramClient.execute(sendPhoto);


        } catch (IOException | InterruptedException | TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
