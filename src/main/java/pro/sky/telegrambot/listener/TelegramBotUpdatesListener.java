package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.service.NotificationTaskService;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;
    private final NotificationTaskService notificationTaskService;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationTaskService notificationTaskService) {
        this.telegramBot = telegramBot;
        this.notificationTaskService = notificationTaskService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            //проверка, что пришло текстовое сообщение
            if (update.message() != null && update.message().text() != null) {
                //получаем текстовое сообщение
                String messageText = update.message().text().trim();
                Long chatId = update.message().chat().id();

                if (messageText.equals("/start")) {
                    String welcomeText = "Привет! Я бот-напоминалка 🤖 Чтобы создать напоминание, отправь сообщение в формате: 01.01.2025 12:00 Напомнить что-то важное";
                    telegramBot.execute(new SendMessage(chatId, welcomeText));
                    return;
                }

                //\d первая группа: дата и время; \s вторая группа: пробелы; (.+) третья группа: текст сообщения
                Pattern pattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)");
                Matcher matcher = pattern.matcher(messageText);

                if (matcher.matches()) {
                    String dateTimeString = matcher.group(1);
                    String text = matcher.group(3);

                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
                        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatter);

                        NotificationTask task = new NotificationTask();
                        task.setChatId(chatId);
                        task.setNotificationText(text);
                        task.setNotificationDateTime(dateTime);
                        notificationTaskService.save(task);

                        telegramBot.execute(new SendMessage(chatId, "✅ Напоминание сохранено!"));
                    } catch (DateTimeParseException e) {
                        telegramBot.execute(new SendMessage(chatId, "❌ Неверный формат даты и времени. Используй: dd.MM.yyyy HH:mm"));
                    }
                } else {
                    telegramBot.execute(new SendMessage(chatId, "❗ Неверный формат. Пример:\n01.01.2025 12:00 Напомнить что-то"));
                }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

}
