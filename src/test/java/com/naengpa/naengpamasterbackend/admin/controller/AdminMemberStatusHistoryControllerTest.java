package com.naengpa.naengpamasterbackend.admin.controller;

import com.naengpa.naengpamasterbackend.admin.dto.response.AdminMemberStatusHistoryResponse;
import com.naengpa.naengpamasterbackend.admin.service.AdminMemberService;
import com.naengpa.naengpamasterbackend.admin.statistics.StatisticsPeriod;
import com.naengpa.naengpamasterbackend.member.entity.MemberStatus;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminMemberStatusHistoryControllerTest {

    @Test
    void getMemberStatusHistoriesReturnsPagedHistory() throws Exception {
        AdminMemberService service = mock(AdminMemberService.class);
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminMemberStatusHistoryController(service))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        StatisticsPeriod period = StatisticsPeriod.of(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 3)
        );
        PageRequest pageable = PageRequest.of(0, 10);
        AdminMemberStatusHistoryResponse history = new AdminMemberStatusHistoryResponse(
                10L,
                1L,
                "회원",
                "member@example.com",
                MemberStatus.ACTIVE,
                MemberStatus.INACTIVE,
                MemberStatus.INACTIVE,
                LocalDateTime.of(2026, 8, 2, 15, 0)
        );
        given(service.getMemberStatusHistories(period, pageable))
                .willReturn(new PageImpl<>(List.of(history), pageable, 1));

        mockMvc.perform(get("/api/v1/admin/member-status-histories")
                        .param("startDate", "2026-08-01")
                        .param("endDate", "2026-08-03")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].memberStatusHistoryId").value(10))
                .andExpect(jsonPath("$.data.content[0].memberId").value(1))
                .andExpect(jsonPath("$.data.content[0].nickname").value("회원"))
                .andExpect(jsonPath("$.data.content[0].email").value("member@example.com"))
                .andExpect(jsonPath("$.data.content[0].previousStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.data.content[0].changedStatus").value("INACTIVE"))
                .andExpect(jsonPath("$.data.content[0].currentStatus").value("INACTIVE"));
    }
}
