package mainutils;

import client.ProductClient;
import domain.model.Product;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;

public class ProductCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(ProductCleanupService.class);
    private static final String LOG_FILE = "product_cleanup_errors.log";
    private final ProductClient productClient = new ProductClient();

    public void cleanUpAllProducts() {
        int page = 0;
        int size = 20;
        boolean morePages = true;

        logger.info("♻️ Запуск полной очистки продуктов через API");

        while (morePages) {
            try {
                Response response = productClient.getAllProductsResponse(page, size);
                List<Product> products = response.jsonPath().getList("content", Product.class);

                if (products == null || products.isEmpty()) {
                    logger.info("🔹 Страница {}: продуктов не найдено", page);
                    break;
                }

                logger.info("🔹 Страница {}: загружено {} продуктов", page, products.size());

                for (Product product : products) {
                    if (product == null || product.getId() == null) {
                        logError("Пропущен некорректный продукт: " + product);
                        continue;
                    }

                    Long id = product.getId();
                    double price = product.getPrice();

                    if (price > 100) {
                        if (id % 3 == 0) {
                            logger.warn("⛔ Продукт {} нельзя обновить (id % 3 == 0)", id);
                            continue;
                        }
                        logger.info("💸 Продукт {} дороже $100 ({}). Снижение цены до 99.99", id, price);
                        product.setPrice(99.99);

                        boolean updated = retry(() -> productClient.updateProduct(id, product), 3);
                        if (!updated) {
                            logError("Не удалось обновить продукт ID=" + id);
                            continue;
                        }
                    }

                    boolean deleted = retry(() -> productClient.deleteProduct(id), 3);
                    if (deleted) {
                        logger.info("✅ Удалён продукт ID={} ", id);
                    } else {
                        logError("Не удалось удалить продукт ID=" + id);
                    }
                }

                Object totalRaw = response.jsonPath().get("totalPages");
                if (totalRaw == null) {
                    logger.warn("⚠️ totalPages == null, остановка");
                    break;
                }
                int totalPages = (int) totalRaw;
                page++;
                morePages = page < totalPages;

            } catch (Exception e) {
                logError("Ошибка на странице " + page + ": " + e.getMessage());
                break;
            }
        }
    }

    private boolean retry(SupplierWithException<Response> action, int attempts) {
        for (int i = 1; i <= attempts; i++) {
            try {
                Response response = action.get();
                if (response.statusCode() == 200 || response.statusCode() == 204) {
                    return true;
                }
                Thread.sleep(200);
            } catch (Exception e) {
                logger.warn("Попытка {} не удалась: {}", i, e.getMessage());
            }
        }
        return false;
    }

    private void logError(String message) {
        logger.error(message);
        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            out.println(LocalDateTime.now() + " ERROR: " + message);
        } catch (Exception e) {
            logger.error("Ошибка записи в log-файл: {}", e.getMessage());
        }
    }

    @FunctionalInterface
    public interface SupplierWithException<T> {
        T get() throws Exception;
    }
}
