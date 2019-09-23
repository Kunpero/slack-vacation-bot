package rs.kunpero.vacation.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import rs.kunpero.vacation.api.dto.AddVacationInfoRequest;
import rs.kunpero.vacation.service.VacationService;
import rs.kunpero.vacation.service.dto.AddVacationInfoResponseDto;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
public class VacationControllerTest {
    private static final String ADD_VACATION_METHOD = "/vacation/addVacation.do";
    private static final String USER_0 = "USER0";
    private static final LocalDate DATE_FROM = LocalDate.of(2018, Month.JUNE, 16);
    private static final LocalDate DATE_TO = LocalDate.of(2018, Month.JUNE, 17);
    private static final String SUB_USER_1 = "USER1";
    private static final String SUB_USER_2 = "USER2";
    private static final List<String> SUBSTITUTION_USER_IDS = List.of(SUB_USER_1, SUB_USER_2);

    @MockBean
    private VacationService vacationService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void successfulAddOperationTestWithSubstitution() throws Exception {

        AddVacationInfoRequest request = new AddVacationInfoRequest()
                .setUserId(USER_0)
                .setDateFrom(DATE_FROM)
                .setDateTo(DATE_TO)
                .setSubstitution(SUBSTITUTION_USER_IDS);

        var errorCode = 0;
        var errorDescription = "testDescription";
        AddVacationInfoResponseDto mockServiceResponse = new AddVacationInfoResponseDto()
                .setErrorCode(errorCode)
                .setErrorDescription(errorDescription);
        when(vacationService.addVacationInfo(any())).thenReturn(mockServiceResponse);

        mockMvc.perform(post(ADD_VACATION_METHOD)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect((jsonPath("$.errorCode", is(errorCode))))
                .andExpect((jsonPath("$.errorDescription", is(errorDescription))));
    }

    @Test
    public void successfulAddOperationTestWithoutSubstitution() throws Exception {

        AddVacationInfoRequest request = new AddVacationInfoRequest()
                .setUserId(USER_0)
                .setDateFrom(DATE_FROM)
                .setDateTo(DATE_TO);

        var errorCode = 0;
        var errorDescription = "testDescription";
        AddVacationInfoResponseDto mockServiceResponse = new AddVacationInfoResponseDto()
                .setErrorCode(errorCode)
                .setErrorDescription(errorDescription);
        when(vacationService.addVacationInfo(any())).thenReturn(mockServiceResponse);

        mockMvc.perform(post(ADD_VACATION_METHOD)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect((jsonPath("$.errorCode", is(errorCode))))
                .andExpect((jsonPath("$.errorDescription", is(errorDescription))));
    }

    @Test
    public void emptyUserIdParamInRequestTest() throws Exception {
        AddVacationInfoRequest request = new AddVacationInfoRequest()
                .setDateFrom(DATE_FROM)
                .setDateTo(DATE_TO)
                .setSubstitution(SUBSTITUTION_USER_IDS);
        mockMvc.perform(post(ADD_VACATION_METHOD)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void nullDateFromParamInRequestTest() throws Exception {
        AddVacationInfoRequest request = new AddVacationInfoRequest()
                .setUserId(USER_0)
                .setDateFrom(null)
                .setDateTo(DATE_TO)
                .setSubstitution(SUBSTITUTION_USER_IDS);
        mockMvc.perform(post(ADD_VACATION_METHOD)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void emptyDateToParamInRequestTest() throws Exception {
        AddVacationInfoRequest request = new AddVacationInfoRequest()
                .setUserId(USER_0)
                .setDateFrom(DATE_FROM)
                .setSubstitution(SUBSTITUTION_USER_IDS);
        mockMvc.perform(post(ADD_VACATION_METHOD)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

}
