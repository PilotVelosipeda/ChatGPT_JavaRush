package com.javarush.telegram;

import com.javarush.telegram.ChatGPTService;
import com.javarush.telegram.DialogMode;
import com.javarush.telegram.MultiSessionTelegramBot;
import com.javarush.telegram.UserInfo;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "test_topa_ai_bot"; //TODO: добавь имя бота в кавычках
    public static final String TELEGRAM_BOT_TOKEN = "7907726416:AAElnD9QPV9YaTrSdlYeffwGrbn_eqxEgSE"; //TODO: добавь токен бота в кавычках
    public static final String OPEN_AI_TOKEN = "gpt:y8HQgXidYWEPQ52jBuwnJFkblB3T8AScAHOTiNP46pG97Qae"; //TODO: добавь токен ChatGPT в кавычках

    private ChatGPTService chatGpt = new ChatGPTService(OPEN_AI_TOKEN);
    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }
    private DialogMode currentMode = null;
    @Override
    public void onUpdateEventReceived(Update update) {
        String massage = getMessageText();

        if (massage.equals("/start")) {
            currentMode = DialogMode.MAIN;
            sendPhotoMessage("main");
            String text = loadMessage("main");
            sendTextMessage(text);

            showMainMenu("Главное меню бота", "/start",
                    "Генерация Tinder-профля \uD83D\uDE0E", "/profile",
                    "Сообщение для знакомства \uD83E\uDD70", "/opener",
                    "Переписка от вашего имени \uD83D\uDE08", "/message",
                    "Переписка со звездами \uD83D\uDD25", "/date",
                    "задать вопрос чату GPT \uD83E\uDDE0", "/gpt");
            return;
        }

        if (massage.equals("/gpt")) {
            currentMode = DialogMode.GPT;
            sendPhotoMessage("gpt");
            String text = loadMessage("gpt");
            sendTextMessage(text);
            return;
        }

        if (currentMode == DialogMode.GPT) {
            String prompt = loadPrompt("gpt");
            String answer = chatGpt.sendMessage("Ответ на вопрос: ", massage);
            sendTextMessage(answer);
            return;
        }

        sendTextMessage("*Привет!*");
        sendTextMessage("_Привет!_");

        sendTextMessage("Вы написали: " + massage);

        sendTextButtonsMessage("Выберите режим работы: ",
                "Старт", "старт",
                "Стоп", "стоп");

        //TODO: основной функционал бота будем писать здесь

    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
