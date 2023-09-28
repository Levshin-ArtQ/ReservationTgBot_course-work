package notifier.NotificationManager.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@PropertySource("config.properties")
public class NManagerConfig {
    @Value("${bot.mastersChatId}") Long mastersChatId;
}
