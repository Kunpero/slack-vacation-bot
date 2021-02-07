package rs.kunpero.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableAutoConfiguration
@EnableJpaRepositories({"rs.kunpero.dutycarousel.repository", "rs.kunpero.vacation.repository"})
@EntityScan({"rs.kunpero.dutycarousel.entity", "rs.kunpero.vacation.entity"})
public class CarouselTestConfig {
}
