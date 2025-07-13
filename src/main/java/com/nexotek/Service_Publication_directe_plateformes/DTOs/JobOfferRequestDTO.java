package com.nexotek.Service_Publication_directe_plateformes.DTOs;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobOfferRequestDTO {
    private String title;
    private String description;
    private String location;
    private String company;
}

