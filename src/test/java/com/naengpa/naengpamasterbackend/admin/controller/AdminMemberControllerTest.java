package com.naengpa.naengpamasterbackend.admin.controller;

import com.naengpa.naengpamasterbackend.admin.dto.response.AdminMemberDetailResponse;
import com.naengpa.naengpamasterbackend.admin.service.AdminMemberService;
import com.naengpa.naengpamasterbackend.global.exception.MemberNotFoundException;
import com.naengpa.naengpamasterbackend.member.entity.HouseholdType;
import com.naengpa.naengpamasterbackend.member.entity.MemberRole;
import com.naengpa.naengpamasterbackend.member.entity.MemberStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminMemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminMemberService adminMemberService;

    @Test
    void getMemberDetailReturnsMemberInformation() throws Exception {
        given(adminMemberService.getMemberDetail(1L)).willReturn(
                new AdminMemberDetailResponse(
                        1L,
                        "member@example.com",
                        "회원",
                        HouseholdType.ONE_PERSON,
                        LocalDateTime.of(2026, 8, 1, 10, 0),
                        MemberStatus.ACTIVE,
                        MemberRole.USER,
                        72
                )
        );

        mockMvc.perform(get("/api/v1/admin/members/{memberId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memberId").value(1))
                .andExpect(jsonPath("$.data.email").value("member@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("회원"))
                .andExpect(jsonPath("$.data.householdType").value("ONE_PERSON"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.naengpaScore").value(72));
    }

    @Test
    void getMemberDetailReturns404ForUnknownMember() throws Exception {
        given(adminMemberService.getMemberDetail(999L))
                .willThrow(new MemberNotFoundException());

        mockMvc.perform(get("/api/v1/admin/members/{memberId}", 999L))
                .andExpect(status().isNotFound());
    }
}
