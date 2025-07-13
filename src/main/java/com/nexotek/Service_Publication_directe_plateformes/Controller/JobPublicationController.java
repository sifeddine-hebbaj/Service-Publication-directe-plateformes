package com.nexotek.Service_Publication_directe_plateformes.Controller;

import com.nexotek.Service_Publication_directe_plateformes.DTOs.JobOfferRequestDTO;
import com.nexotek.Service_Publication_directe_plateformes.Service.JobPublicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/job-offers")
@RequiredArgsConstructor
public class JobPublicationController {

    private final JobPublicationService service;

    // 4. Publication d'une offre sur LinkedIn (utilise access_token stocké)
    @PostMapping("/linkedin/publish")
    public ResponseEntity<String> publishOnLinkedIn(
        @RequestBody JobOfferRequestDTO dto,
        @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
        @RequestHeader(value = "X-Linkedin-User-Urn", required = false) String personUrn
    ) {
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }
        String error = service.publishToLinkedInWithError(dto, token, personUrn);
        // Always store the offer in the database with the LinkedIn post text
        service.saveOfferWithLinkedinText(dto);
        if (error == null) {
            return ResponseEntity.ok("Offre publiée sur LinkedIn et stockée en base de données !");
        } else {
            return ResponseEntity.status(400).body("Erreur LinkedIn: " + error + " (offre stockée en base)");
        }
    }
}
