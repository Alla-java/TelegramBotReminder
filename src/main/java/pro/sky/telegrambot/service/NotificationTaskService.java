package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NotificationTaskService {

    private final NotificationTaskRepository repository;
    private final TelegramBot telegramBot;

    @Autowired
    public NotificationTaskService(NotificationTaskRepository repository, TelegramBot telegramBot) {
        this.repository = repository;
        this.telegramBot = telegramBot;
    }

    public NotificationTask save(NotificationTask task) {
        return repository.save(task);
    }

    public List<NotificationTask> getAllTasks() {
        return repository.findAll();
    }

    public List<NotificationTask> getTasksByChatId(Long chatId) {
        return repository.findByChatId(chatId);
    }

    public List<NotificationTask> getTasksToNotify(LocalDateTime beforeTime) {
        return repository.findByNotificationDateTimeBefore(beforeTime);
    }

    public void deleteTask(Long id) {
        repository.deleteById(id);
    }

    @Scheduled(cron = "0 0/1 * * * *") // работает каждую минуту
    public void checkAndSendNotifications() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        List<NotificationTask> tasksToNotify = repository.findByNotificationDateTime(now);

        for (NotificationTask task : tasksToNotify) {
            // Отправка уведомления через Telegram API
            telegramBot.execute(new SendMessage(task.getChatId(), task.getNotificationText()));
        }
    }
}
