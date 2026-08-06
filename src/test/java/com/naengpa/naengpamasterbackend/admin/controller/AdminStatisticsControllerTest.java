package com.naengpa.naengpamasterbackend.admin.controller;

import com.naengpa.naengpamasterbackend.admin.dto.response.AdminMemberStatisticsResponse;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminMemberUsageStatisticsResponse;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminMaterialStatisticsResponse;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminRecipeStatisticsResponse;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminStatisticsSummaryResponse;
import com.naengpa.naengpamasterbackend.admin.service.AdminStatisticsService;
import com.naengpa.naengpamasterbackend.admin.statistics.StatisticsPeriod;
import com.naengpa.naengpamasterbackend.admin.statistics.StatisticsGranularity;
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
    void getRecipeStatisticsReturnsRequestedPeriodData() throws Exception {
        AdminStatisticsService service = mock(AdminStatisticsService.class);
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminStatisticsController(service))
                .build();
        StatisticsPeriod period = StatisticsPeriod.of(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 2)
        );
        given(service.getRecipeStatistics(period)).willReturn(
                new AdminRecipeStatisticsResponse(
                        period.startDate(), period.endDate(),
                        100L, 60L, 30L, 10L,
                        List.of(new AdminRecipeStatisticsResponse.CategoryStatistics(
                                "한식", 3L, 1L, 1L, 1L
                        ))
                )
        );

        mockMvc.perform(get("/api/v1/admin/statistics/recipes")
                        .param("startDate", "2026-08-01")
                        .param("endDate", "2026-08-02")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalRecipeCount").value(100))
                .andExpect(jsonPath("$.data.baseRecipeCount").value(60))
                .andExpect(jsonPath("$.data.memberRecipeCount").value(30))
                .andExpect(jsonPath("$.data.adminRecipeCount").value(10))
                .andExpect(jsonPath("$.data.categoryStatistics[0].categoryName").value("한식"))
                .andExpect(jsonPath("$.data.categoryStatistics[0].recipeCount").value(3))
                .andExpect(jsonPath("$.data.categoryStatistics[0].baseRecipeCount").value(1))
                .andExpect(jsonPath("$.data.categoryStatistics[0].memberRecipeCount").value(1))
                .andExpect(jsonPath("$.data.categoryStatistics[0].adminRecipeCount").value(1));
    }

    @Test
    void getMaterialStatisticsReturnsRequestedPeriodData() throws Exception {
        AdminStatisticsService service = mock(AdminStatisticsService.class);
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminStatisticsController(service))
                .build();
        StatisticsPeriod period = StatisticsPeriod.of(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 2)
        );
        given(service.getMaterialStatistics(period)).willReturn(
                new AdminMaterialStatisticsResponse(
                        period.startDate(), period.endDate(), StatisticsGranularity.DAY,
                        List.of(new AdminMaterialStatisticsResponse.DailyStatistics(
                                LocalDate.of(2026, 8, 1), 2L, 1L
                        )),
                        List.of()
                )
        );

        mockMvc.perform(get("/api/v1/admin/statistics/materials")
                        .param("startDate", "2026-08-01")
                        .param("endDate", "2026-08-02")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.averageScore").doesNotExist())
                .andExpect(jsonPath("$.data.dailyStatistics[0].registeredCount").value(2))
                .andExpect(jsonPath("$.data.dailyStatistics[0].expiredCount").value(1))
                .andExpect(jsonPath("$.data.dailyStatistics[0].activityCount").doesNotExist());
    }

    @Test
    void getStatisticsSummaryReturnsRequestedPeriodSummary() throws Exception {
        AdminStatisticsService service = mock(AdminStatisticsService.class);
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminStatisticsController(service))
                .build();
        StatisticsPeriod period = StatisticsPeriod.of(
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 3)
        );
        given(service.getStatisticsSummary(period)).willReturn(
                new AdminStatisticsSummaryResponse(82.5, 30L, 40L, 10L, 8L, 25.0, 7L)
        );

        mockMvc.perform(get("/api/v1/admin/statistics/summary")
                        .param("startDate", "2026-08-01")
                        .param("endDate", "2026-08-03")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.averageScore").value(82.5))
                .andExpect(jsonPath("$.data.scoreMemberCount").value(30))
                .andExpect(jsonPath("$.data.registeredIngredientCount").value(40))
                .andExpect(jsonPath("$.data.expiredIngredientCount").value(10))
                .andExpect(jsonPath("$.data.previousExpiredIngredientCount").value(8))
                .andExpect(jsonPath("$.data.expiredIngredientChangeRate").value(25.0))
                .andExpect(jsonPath("$.data.ingredientExpirationRate").doesNotExist())
                .andExpect(jsonPath("$.data.createdRecipeCount").value(7));
    }

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
                StatisticsGranularity.DAY,
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
                StatisticsGranularity.DAY,
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
