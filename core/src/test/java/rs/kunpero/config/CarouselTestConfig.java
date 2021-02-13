package rs.kunpero.config;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static rs.kunpero.dutycarousel.scheduler.DutyCarouselTask.IS_DAY_OFF_SERVICE_URL;

@Configuration
@EnableAutoConfiguration
@EnableJpaRepositories({"rs.kunpero.dutycarousel.repository", "rs.kunpero.vacation.repository"})
@EntityScan({"rs.kunpero.dutycarousel.entity", "rs.kunpero.vacation.entity"})
public class CarouselTestConfig {
    @Bean
    public OkHttpClient okHttpClient() throws IOException {
        OkHttpClient client = mock(OkHttpClient.class);
        Call call = mock(Call.class);
        Request mockRequest = new Request.Builder()
                .url(IS_DAY_OFF_SERVICE_URL)
                .build();
        Response response = new Response.Builder()
                .request(mockRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("0")
                .body(ResponseBody.create(MediaType.get("application/json; charset=utf-8"), "0"))
                .build();

        when(call.execute()).thenReturn(response);
        when(client.newCall(any())).thenReturn(call);
        return client;
    }

    @Bean
    public MethodsClient methodsClient(Slack slack) {
        return slack.methods();
    }
}
