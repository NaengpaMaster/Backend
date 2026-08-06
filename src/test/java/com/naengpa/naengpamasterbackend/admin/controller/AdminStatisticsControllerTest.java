package com.naengpa.naengpamasterbackend.admin.controller;

import com.naengpa.naengpamasterbackend.admin.dto.response.AdminMemberStatisticsResponse;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminMemberUsageStatisticsResponse;
import com.naengpa.naengpamasterbackend.admin.service.AdminStatisticsService;
import com.naengpa.naengpamasterbackend.admin.statistics.StatisticsPeriod;
import com.naengpa.naengpamasterbackend.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminStatisticsControllerTest {

    @Test
    void getMemberStatisticsReturnsRequestedPeriodStatistics() throws Exception {
        AdminStatisticsService service = mock(AdminStatisticsService.class);
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminStatisticsController(service))
                .build();
        StatisticsPeriod period = StatisticsPeriod.of(
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 3)
        );
        AdminMemberStatisticsResponse response = AdminMemberStatisticsResponse.of(
                period.startDate(),
                period.endDate(),
                100L,
                5L,
                3L,
                1L,
                List.of(
                        new AdminMemberStatisticsResponse.DailyStatistics(
                                LocalDate.of(2026, 8, 1),
                                2L,
                                1L
                        )
                )
        );
        given(service.getMemberStatistics(period)).willReturn(response);

        mockMvc.perform(get("/api/v1/admin/statistics/members")
                        .param("startDate", "2026-08-01")
                        .param("endDate", "2026-08-03")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.startDate").value("2026-08-01"))
                .andExpect(jsonPath("$.data.endDate").value("2026-08-03"))
                .andExpect(jsonPath("$.data.newMemberCount").value(3))
                .andExpect(jsonPath("$.data.inactiveProcessedMemberCount").value(1))
                .andExpect(jsonPath("$.data.dailyStatistics[0].date").value("2026-08-01"));
    }

    @Test
    void getMemberUsageStatisticsReturnsServiceStatistics() throws Exception {
        AdminStatisticsService service = mock(AdminStatisticsService.class);
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminStatisticsController(service))
                .build();
        StatisticsPeriod period = StatisticsPeriod.of(
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 3)
        );
        AdminMemberUsageStatisticsResponse.ServiceUsage fridge =
                new AdminMemberUsageStatisticsResponse.ServiceUsage(
                        4L,
                        40.0,
                        List.of(new AdminMemberUsageStatisticsResponse.DailyUsage(
                                LocalDate.of(2026, 8, 1),
                                3L
                        ))
                );
        AdminMemberUsageStatisticsResponse response = new AdminMemberUsageStatisticsResponse(
                period.startDate(),
                period.endDate(),
                10L,
                fridge,
                new AdminMemberUsageStatisticsResponse.ServiceUsage(2L, 20.0, List.of()),
                new AdminMemberUsageStatisticsResponse.ServiceUsage(1L, 10.0, List.of())
        );
        given(service.getMemberUsageStatistics(period)).willReturn(response);

        mockMvc.perform(get("/api/v1/admin/statistics/service-usage")
                        .param("startDate", "2026-08-01")
                        .param("endDate", "2026-08-03")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activeMemberCount").value(10))
                .andExpect(jsonPath("$.data.fridge.userCount").value(4))
                .andExpect(jsonPath("$.data.fridge.usageRate").value(40.0))
                .andExpect(jsonPath("$.data.shopping.userCount").value(2))
                .andExpect(jsonPath("$.data.recipe.userCount").value(1));
    }

    @Test
    void getMemberUsageStatisticsRejectsReversedPeriod() throws Exception {
        AdminStatisticsService service = mock(AdminStatisticsService.class);
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminStatisticsController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/api/v1/admin/statistics/service-usage")
                        .param("startDate", "2026-08-03")
                        .param("endDate", "2026-08-01")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
