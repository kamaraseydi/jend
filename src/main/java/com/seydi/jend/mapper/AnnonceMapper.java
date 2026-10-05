package com.seydi.jend.mapper;

import com.seydi.jend.dto.request.CreateAnnonceRequest;
import com.seydi.jend.dto.response.AnnonceResponse;
import com.seydi.jend.entity.Annonce;
import org.springframework.stereotype.Component;

@Component
public class AnnonceMapper {

    public Annonce toEntity(CreateAnnonceRequest request) {
        Annonce annonce = new Annonce();

        annonce.setTitre(request.titre());
        annonce.setDescription(request.description());
        annonce.setPrix(request.prix());
        annonce.setEtat(request.etat());
        annonce.setVille(request.ville());
        annonce.setQuartier(request.quartier());

        return annonce;
    }

    public AnnonceResponse toResponse(Annonce annonce) {

        return new AnnonceResponse(
                annonce.getId(),
                annonce.getTitre(),
                annonce.getDescription(),
                annonce.getPrix(),
                annonce.getEtat(),
                annonce.getStatut(),
                annonce.getVille(),
                annonce.getQuartier(),
                annonce.getVendeur().getId(),
                annonce.getVendeur().getNom(),
                annonce.getCategory().getId(),
                annonce.getCategory().getNom(),
                annonce.getCreatedAt(),
                annonce.getUpdatedAt()
        );
    }
}