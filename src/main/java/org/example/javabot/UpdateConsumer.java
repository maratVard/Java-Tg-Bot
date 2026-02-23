package org.example.javabot;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpClient;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String HOROSCOPE_API_URL = "https://api.api-ninjas.com/v1/horoscope";
    private static final String API_KEY = "On+uxnn2vgeCW58z/j5P9g==IOOYgkgpc2LowGan";

    public UpdateConsumer(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    @SneakyThrows
    @Override
    public void consume(Update update) {
        if (update.hasMessage()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            if (messageText.equals("/start") || messageText.equals("Привет")) {
                sendMainMenu(chatId);
            } else {
                sendMessage(chatId, "Я вас не понимаю");
            }
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuerry(update.getCallbackQuery());
        }
    }

    private void handleCallbackQuerry(CallbackQuery callbackQuery) {
        var data = callbackQuery.getData();
        var chatId = callbackQuery.getFrom().getId();
        var user = callbackQuery.getFrom();
        switch (data) {
            case "my_name"->sendMyName(chatId, user);
            case "random"->sendRandom(chatId);
            case "random_image"->sendImage(chatId);
            //case "horoscope"->sendHoroscope(chatId);
            default -> sendMessage(chatId, "Неизвестная команда");
        }
    }

    @SneakyThrows
    private void sendMessage(Long chatId, String messageText) {
        SendMessage message = SendMessage.builder()
                .text(messageText)
                .chatId(chatId)
                .build();

        telegramClient.execute(message);
    }

    private void sendImage(Long chatId) {
        sendMessage(chatId, "Присылаю картинку...");

        new Thread(() -> {
            try {
                String publicKey = "https://disk.yandex.ru/d/Sw19ToFBzN-Yww";
                YandexDiskService.sendRandomImage(chatId, publicKey, telegramClient);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void sendRandom(Long chatId)
    {
        var randomInt = ThreadLocalRandom.current().nextInt();
        sendMessage(chatId,"Ваше число: " + randomInt);
    }

    private void sendMyName(Long chatId, User user)
    {
        var text = "Привет!\n\nВас зовут: %s\nВаш ник: @%s"
                .formatted(
                        user.getFirstName(), user.getUserName()
                );
        sendMessage(chatId, text);
    }
    /*private void sendHoroscope(Long chatId) {
        String [] signs = {

        }
    }*/

    @SneakyThrows
    private void sendMainMenu(Long chatId) {
        SendMessage message = SendMessage.builder()
                .text("Добро пожаловать, выберите какое-то действие:")
                .chatId(chatId)
                .build();

        var button1=InlineKeyboardButton.builder()
                .text("Как меня зовут")
                .callbackData("my_name")
                .build();
        var button2=InlineKeyboardButton.builder()
                .text("Получение случайного числа")
                .callbackData("random")
                .build();
        var button3=InlineKeyboardButton.builder()
                .text("Прислать случайную картинку")
                .callbackData("random_image")
                .build();
        var button4=InlineKeyboardButton.builder()
                .text("Гороскоп")
                .callbackData("horoscope")
                .build();

        List<InlineKeyboardRow> keyboardRows = List.of(
                new InlineKeyboardRow(button1),
                new InlineKeyboardRow(button2),
                new InlineKeyboardRow(button3),
                new InlineKeyboardRow(button4)
        );

        InlineKeyboardMarkup markup=new InlineKeyboardMarkup(keyboardRows);
        message.setReplyMarkup(markup);
        telegramClient.execute(message);
    }
}

