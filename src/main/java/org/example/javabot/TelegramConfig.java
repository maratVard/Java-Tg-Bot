package org.example.javabot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Configuration
public class TelegramConfig {

    @Bean
    public TelegramClient telegramClient() {
        return new OkHttpTelegramClient("7332075289:AAHJL1J4o5nlBs82OihRJbs8dN48w_6K28Q");
    }
}
