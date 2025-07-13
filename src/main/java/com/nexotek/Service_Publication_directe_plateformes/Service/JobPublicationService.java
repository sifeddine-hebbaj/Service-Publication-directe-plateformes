package com.nexotek.Service_Publication_directe_plateformes.Service;


import com.nexotek.Service_Publication_directe_plateformes.DTOs.JobOfferRequestDTO;
import com.nexotek.Service_Publication_directe_plateformes.DTOs.JobOfferResponseDTO;
import com.nexotek.Service_Publication_directe_plateformes.Entity.JobOffer;
import com.nexotek.Service_Publication_directe_plateformes.Mapper.JobOfferMapper;
import com.nexotek.Service_Publication_directe_plateformes.Repository.JobOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.MediaType;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JobPublicationService {

    private final JobOfferRepository repository;
    private final WebClient webClient = WebClient.builder().build();





    public String publishToLinkedInWithError(JobOfferRequestDTO dto, String token, String personUrn) {
        if (token == null || personUrn == null) return "Token or person URN missing";
        String url = "https://api.linkedin.com/v2/ugcPosts";
        // Build the engaging LinkedIn post text
        String commentary = "📢 " + dto.getTitle() + " - " + dto.getCompany() + " à " + dto.getLocation() + ". " + dto.getDescription();

        Map<String, Object> postData = new HashMap<>();
        postData.put("author", personUrn); // "urn:li:person:..."
        postData.put("lifecycleState", "PUBLISHED");
        postData.put("specificContent", Map.of(
            "com.linkedin.ugc.ShareContent", Map.of(
                "shareCommentary", Map.of("text", commentary),
                "shareMediaCategory", "NONE"
            )
        ));
        postData.put("visibility", Map.of("com.linkedin.ugc.MemberNetworkVisibility", "PUBLIC"));


        try {
            String response = webClient.post()
                .uri(url)
                .header("Authorization", "Bearer " + token)
                .header("X-Restli-Protocol-Version", "2.0.0")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(postData)
                .retrieve()
                .bodyToMono(String.class)
                .block();
            return null; // Success
        } catch (Exception e) {
            return "LinkedIn API error: " + e.getMessage();
        }
    }





    public void saveOfferWithLinkedinText(JobOfferRequestDTO request) {
        JobOffer offer = JobOfferMapper.toEntity(request);
        String linkedinPostText = "📢 " + request.getTitle() + " - " + request.getCompany() + " à " + request.getLocation() + ". " + request.getDescription();
        offer.setLinkedinPostText(linkedinPostText);
        repository.save(offer);
    }
}

