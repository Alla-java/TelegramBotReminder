package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.model.NotificationTask;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {

    // Получить все задачи на конкретную дату и время
    // Метод который ищет записи у которых время совпадает с текущим
    List<NotificationTask> findByNotificationDateTime(LocalDateTime dateTime);

    // Получить все задачи до определённого момента (например, для отправки уведомлений)
    List<NotificationTask> findByNotificationDateTimeBefore(LocalDateTime dateTime);

    // Пример: Получить все задачи по chatId
    List<NotificationTask> findByChatId(Long chatId);
}
