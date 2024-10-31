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
    private ArrayList<String> list = new ArrayList<>();
    private UserInfo me;
    private int questionCount;
    
    @Override
    public void onUpdateEventReceived(Update update) {
        String message = getMessageText();

        if (message.equals("/start")) {
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

        //command GPT
        if (message.equals("/gpt")) {
            currentMode = DialogMode.GPT;
            sendPhotoMessage("gpt");
            String text = loadMessage("gpt");
            sendTextMessage(text);
            return;
        }

        if (currentMode == DialogMode.GPT) {
            String prompt = loadPrompt("gpt");

            Message msq = sendTextMessage("Подождите девушка набирает текст...");
            String answer = chatGpt.sendMessage("Ответ на вопрос: ", message);
            updateTextMessage(msq, answer);
            return;
        }

        //command DATE
        if (message.equals("/date")) {
            currentMode = DialogMode.DATE;
            sendPhotoMessage("date");
            String text = loadMessage("date");
            sendTextButtonsMessage(text,
                    "Ариана Гранде", "date_grande",
                    "Марго Робби", "date_robbie",
                    "Зендея", "date_zendaya",
                    "Райн Гослинг", "date_gosling",
                    "Том Харли", "date_hardy");
            return;
        }

        if (currentMode == DialogMode.DATE) {
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("date_")) {
                sendPhotoMessage(query);
                sendTextMessage(" Отличный выбор! \n Твоя задача пригласить девушку/парня на свидание ❤\uFE0F за 5 сообщений.");

                String prompt = loadPrompt(query);
                chatGpt.setPrompt(prompt);
                return;
            }

            Message msq = sendTextMessage("Подождите девушка набирает текст...");
            String answer = chatGpt.addMessage(message);
            updateTextMessage(msq, answer);
            return;
        }

        //command MESSAGE
        if (message.equals("/message")) {
            currentMode = DialogMode.MESSAGE;
            sendPhotoMessage("message");
            sendTextButtonsMessage("Пришлите в чат вашу переписку",
                    "Следующее сообщение", "message_next",
                    "Пригласить на свидание", "message_date");
            return;
        }

        if (currentMode == DialogMode.MESSAGE) {
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("message_")) {
                String prompt = loadPrompt(query);
                String userChatHistory = String.join("\n\n", list);

                Message msq = sendTextMessage("Подождите пару секунд - ChatGPT думает...");
                String answer = chatGpt.sendMessage(prompt, userChatHistory);
                updateTextMessage(msq, answer);
                return;
            }

            list.add(message);
            return;
        }

        //command PROFILE
        if (message.equals("/profile")) {
            currentMode = DialogMode.PROFILE;
            sendPhotoMessage("profile");

            me = new UserInfo();
            questionCount =1;
            sendTextMessage("Сколько вам лет?");
            return;
        }

        if (currentMode == DialogMode.PROFILE) {
            if (questionCount == 1) {
                me.age = message;

                questionCount=2;
                sendTextMessage("Кем вы работаете ?");
                return;
            }

            if (questionCount == 2) {
                me.occupation = message;

                questionCount = 3;
                sendTextMessage("У вас есть хобби ?");
                return;
            }

            if (questionCount == 3) {
                 me.hobby = message;

                 
            }

            String aboutMyself = me.toString();
            String prompt = loadPrompt("profile");
            String answer = chatGpt.sendMessage(prompt, aboutMyself);
            sendTextMessage(answer);
            return;
        }


        sendTextMessage("*Привет!*");
        sendTextMessage("_Привет!_");

        sendTextMessage("Вы написали: " + message);

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
