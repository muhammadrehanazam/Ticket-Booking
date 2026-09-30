package com.ticketbooking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void loginReturnsToken_andProtectedBookingEndpointRequiresAuthentication() throws Exception {
        String phone = "0300" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String cnic = "42101-" + UUID.randomUUID().toString().replace("-", "").substring(0, 7) + "-1";

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Security User\",\"phone\":\"" + phone + "\",\"cnic\":\"" + cnic + "\",\"email\":\"user@example.com\",\"password\":\"TestPass123\"}"))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"" + phone + "\",\"password\":\"TestPass123\"}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginBody = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = loginBody.get("token").asText();
        Long userId = loginBody.get("userId").asLong();

        mockMvc.perform(get("/api/bookings/user/{userId}", userId))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/bookings/user/{userId}", userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void adminEndpointsRequireAdminRole() throws Exception {
        String phone = "0300" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String cnic = "42101-" + UUID.randomUUID().toString().replace("-", "").substring(0, 7) + "-2";

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Regular User\",\"phone\":\"" + phone + "\",\"cnic\":\"" + cnic + "\",\"email\":\"regular@example.com\",\"password\":\"TestPass123\"}"))
                .andExpect(status().isCreated());

        MvcResult customerLoginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"" + phone + "\",\"password\":\"TestPass123\"}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode customerLoginBody = objectMapper.readTree(customerLoginResult.getResponse().getContentAsString());
        String customerToken = customerLoginBody.get("token").asText();

        mockMvc.perform(get("/api/admin/trains")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());

        MvcResult adminLoginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"03001234567\",\"password\":\"Admin@123\"}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode adminLoginBody = objectMapper.readTree(adminLoginResult.getResponse().getContentAsString());
        String adminToken = adminLoginBody.get("token").asText();

        mockMvc.perform(get("/api/admin/trains")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }
}
