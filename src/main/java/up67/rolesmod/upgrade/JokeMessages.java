package up67.rolesmod.upgrade;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Пулы шуточных фраз, которые апгрейдер кричит игроку в чат.
 * Это не оскорбление конкретного человека, это понарошку, как в казино-автоматах.
 */
public class JokeMessages {

    private static final List<String> FAIL = List.of(
            "ПРОВАЛ! Ебать ты лох, предмет сгорел.",
            "Апгрейдер плюёт тебе в лицо. Неудача.",
            "Не шмогла. Предмет приказал долго жить.",
            "99% неудачников сдаются именно тут. Ты не исключение.",
            "Станция гудит от смеха над твоей попыткой.",
            "Короче, не фартануло. Бывает у лохов.",
            "Апгрейдер: 'слабенько, братан'.",
            "Предмет испарился. Как и твоя удача.",
            "Это даже не смешно, насколько ты не повезло.",
            "РЖУНИМАГУ, предмет сгорел дотла.",
            "Казино всегда в плюсе. А ты - нет.",
            "Фиаско, братишка."
    );

    private static final List<String> SUCCESS = List.of(
            "УСПЕХ! Апгрейдер уважает тебя (немного).",
            "Получилось! Даже апгрейдер удивлён.",
            "Залетело! Повезло как не в себя.",
            "ГООООЛ! Апгрейд прошёл на ура.",
            "Станция довольна. Апгрейдер гордится тобой.",
            "Успех! На этот раз ты не лох.",
            "Сработало! Вот это поворот.",
            "Победа! Можно идти хвастаться друзьям.",
            "Апгрейдер выдал предмет и подмигнул тебе.",
            "Невероятно, но получилось!"
    );

    private static final List<String> MEME_SUCCESS = List.of(
            "ЧТО?! Это не должно было сработать! Но ты особенный.",
            "Легенды гласят, что такой шанс выпадает раз в жизни. Это твоя жизнь.",
            "Апгрейдер завис от удивления, но предмет всё же выдал."
    );

    public static Component randomFail() {
        return Component.literal(pick(FAIL)).withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
    }

    public static Component randomSuccess(double chance) {
        if (chance < 0.1) {
            return Component.literal(pick(MEME_SUCCESS)).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        }
        return Component.literal(pick(SUCCESS)).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD);
    }

    /** Итоговое сообщение, когда за раз было несколько попыток (x2/x4/x8). */
    public static Component batchSummary(int successCount, int total, double chance) {
        if (successCount == 0) {
            return Component.literal("ВСЕ " + total + " ПРОВАЛИЛИСЬ! Ебать ты лох, видимо сегодня не твой день.")
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
        }
        if (successCount == total) {
            return Component.literal("ВСЕ " + total + " ИЗ " + total + " ЗАШЛИ! Это база, читер, наверное.")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        }
        return Component.literal("Результат: " + successCount + " из " + total + " апгрейдов прошло успешно.")
                .withStyle(ChatFormatting.YELLOW);
    }

    private static String pick(List<String> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    private JokeMessages() {
    }
}
