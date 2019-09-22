package rs.kunpero.vacation.util.converter

import rs.kunpero.vacation.api.dto.AddVacationInfoRequest
import rs.kunpero.vacation.service.dto.AddVacationInfoRequestDto

class VacationConverterUtils {
    companion object {
        @JvmStatic
        fun convert(request: AddVacationInfoRequest): AddVacationInfoRequestDto {
            return AddVacationInfoRequestDto()
                    .setUserId(request.userId)
                    .setDateFrom(request.dateFrom)
                    .setDateTo(request.dateTo)
                    .setSubstitutionIdList(request.substitution)
        }
    }
}