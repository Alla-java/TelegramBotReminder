package pro.sky.telegrambot.serviceTests;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;
import pro.sky.telegrambot.service.NotificationTaskService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static org.mockito.Mockito.*;

public class NotificationTaskServiceTests {

    private NotificationTaskRepository repository;
    private TelegramBot bot;
    private NotificationTaskService service;

    @BeforeEach
    public void setUp() {
        repository = mock(NotificationTaskRepository.class);
        bot = mock(TelegramBot.class);
        service = new NotificationTaskService(repository, bot);
    }

    //Тест с успешной отправкой уведомления, когда есть задача на текущую минуту
    @Test
    public void testCheckAndSendNotifications_withTasks() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        NotificationTask task = new NotificationTask();
        task.setChatId(123456789L);
        task.setNotificationText("Напоминание!");
        task.setNotificationDateTime(now);

        when(repository.findByNotificationDateTime(now)).thenReturn(Collections.singletonList(task));

        service.checkAndSendNotifications();

        verify(repository, times(1)).findByNotificationDateTime(now);
        verify(bot, times(1)).execute(any(SendMessage.class));
    }

    //Тест с неуспешной отправкой уведомления, когда нет задач на текущую минуту
    @Test
    public void testCheckAndSendNotifications_noTasks() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        when(repository.findByNotificationDateTime(now)).thenReturn(Collections.emptyList());

        service.checkAndSendNotifications();

        verify(repository, times(1)).findByNotificationDateTime(now);
        verify(bot, never()).execute(any(SendMessage.class));
    }
}









