package com.nexotek.Service_Publication_directe_plateformes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexotek.Service_Publication_directe_plateformes.DTOs.JobOfferRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
class ServicePublicationDirectePlateformesApplicationTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void publishOffer_LinkedInErrorButStored() throws Exception {
        JobOfferRequestDTO dto = JobOfferRequestDTO.builder()
                .title("Développeur Java")
                .description("CDI, 3 ans d'expérience")
                .location("Paris, France")
                .company("Nexotek RH")
                .build();
        mockMvc.perform(post("/api/job-offers/linkedin/publish")
                .header("Authorization", "Bearer dummy-token")
                .header("X-Linkedin-User-Urn", "urn:li:person:1234")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Erreur LinkedIn")));
    }

    @Test
    void publishOffer_TokenMissing() throws Exception {
        JobOfferRequestDTO dto = JobOfferRequestDTO.builder()
                .title("Développeur Java")
                .description("CDI, 3 ans d'expérience")
                .location("Paris, France")
                .company("Nexotek RH")
                .build();
        mockMvc.perform(post("/api/job-offers/linkedin/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Token or person URN missing")));
    }

    @Test
    void publishOffer_UserUrnMissing() throws Exception {
        JobOfferRequestDTO dto = JobOfferRequestDTO.builder()
                .title("Développeur Java")
                .description("CDI, 3 ans d'expérience")
                .location("Paris, France")
                .company("Nexotek RH")
                .build();
        mockMvc.perform(post("/api/job-offers/linkedin/publish")
                .header("Authorization", "Bearer dummy-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Token or person URN missing")));
    }

    @Test
    void publishOffer_ValidData() throws Exception {
        JobOfferRequestDTO dto = JobOfferRequestDTO.builder()
                .title("Développeur Java")
                .description("CDI, 3 ans d'expérience")
                .location("Paris, France")
                .company("Nexotek RH")
                .build();
        mockMvc.perform(post("/api/job-offers/linkedin/publish")
                .header("Authorization", "Bearer dummy-token")
                .header("X-Linkedin-User-Urn", "urn:li:person:1234")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest()) // LinkedIn API échoue avec token factice
                .andExpect(content().string(org.hamcrest.Matchers.containsString("offre stockée en base")));
    }
}
