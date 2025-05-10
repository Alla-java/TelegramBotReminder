package pro.sky.telegrambot.listenerTests;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.sky.telegrambot.listener.TelegramBotUpdatesListener;
import pro.sky.telegrambot.service.NotificationTaskService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TelegramBotUpdatesListenerTests {

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private NotificationTaskService notificationTaskService;

    @InjectMocks
    private TelegramBotUpdatesListener listener;

    @Test
    void testProcess_startCommand_sendsWelcomeMessage() {
        long chatId = 1L;

        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("/start");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);

        listener.process(List.of(update));

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot, times(1)).execute(captor.capture());

        SendMessage sentMessage = captor.getValue();
        assertEquals(chatId, sentMessage.getParameters().get("chat_id"));
        assertTrue(sentMessage.getParameters().get("text").toString().contains("Привет"));

        verifyNoInteractions(notificationTaskService);
    }

    @Test
    void testProcess_validReminder_savesTask() {
        long chatId = 2L;
        String text = "01.01.2025 12:00 Позвонить маме";

        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn(text);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);

        listener.process(List.of(update));

        verify(notificationTaskService, times(1)).save(argThat(task ->
                task.getChatId().equals(chatId) &&
                        task.getNotificationText().equals("Позвонить маме") &&
                        task.getNotificationDateTime().equals(LocalDateTime.of(2025, 1, 1, 12, 0))
        ));
        verify(telegramBot, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void testProcess_invalidReminderFormat_sendsError() {
        long chatId = 3L;
        String text = "Напоминание без даты";

        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn(text);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);

        listener.process(List.of(update));

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot, times(1)).execute(captor.capture());

        SendMessage sentMessage = captor.getValue();
        assertEquals(chatId, sentMessage.getParameters().get("chat_id"));
        assertTrue(sentMessage.getParameters().get("text").toString().contains("Неверный формат"));

        verifyNoInteractions(notificationTaskService);
    }






}
