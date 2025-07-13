package com.nexotek.Service_Publication_directe_plateformes.DTOs;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobOfferResponseDTO {
    private Long id;
    private String title;
    private boolean published;
}

