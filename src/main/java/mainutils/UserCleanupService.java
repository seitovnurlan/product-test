package mainutils;

import client.UserClient;
import domain.model.User;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Удаляет всех пользователей через API.
 * Адаптирован для случая, если сервер возвращает весь список сразу (без пагинации).
 */
public class UserCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(UserCleanupService.class);
    private final UserClient userClient = new UserClient();

    /**
     * Полностью очищает пользователей в системе.
     */
    public void cleanUpAllUsers() {
        logger.info("♻️ Загружаем всех пользователей для удаления");

        try {
            List<User> users = userClient.getAllUsers(); // Получаем всех пользователей сразу

            if (users == null || users.isEmpty()) {
                logger.info("✅ Пользователи отсутствуют — удалять нечего");
                return;
            }

            logger.info("🔹 Найдено {} пользователей для удаления", users.size());

            for (User user : users) {
                if (user == null || user.getId() == null) {
                    logger.warn("⛔ Пропущен пустой пользователь или без ID");
                    continue;
                }

                Response deleteResponse = userClient.deleteUser(user.getId());

                if (deleteResponse.statusCode() == 200 || deleteResponse.statusCode() == 204) {
                    logger.info("✅ Удалён пользователь ID={}", user.getId());
                } else {
                    logger.error("❌ Ошибка удаления пользователя ID={}: {}", user.getId(), deleteResponse.statusLine());
                }
            }

            logger.info("✅ Очистка пользователей завершена");

        } catch (Exception e) {
            logger.error("🔥 Ошибка при удалении пользователей: {}", e.getMessage(), e);
        }
    }
}
