package com.nexotek.Service_Publication_directe_plateformes.Mapper;


import com.nexotek.Service_Publication_directe_plateformes.DTOs.JobOfferRequestDTO;
import com.nexotek.Service_Publication_directe_plateformes.DTOs.JobOfferResponseDTO;
import com.nexotek.Service_Publication_directe_plateformes.Entity.JobOffer;

public class JobOfferMapper {
    public static JobOffer toEntity(JobOfferRequestDTO dto) {
        return JobOffer.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .location(dto.getLocation())
                .company(dto.getCompany())
                .published(false)
                .build();
    }

    public static JobOfferResponseDTO toResponse(JobOffer jobOffer) {
        return JobOfferResponseDTO.builder()
                .id(jobOffer.getId())
                .title(jobOffer.getTitle())
                .published(jobOffer.isPublished())
                .build();
    }
}

