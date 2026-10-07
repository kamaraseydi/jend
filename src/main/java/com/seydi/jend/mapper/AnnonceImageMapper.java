package com.seydi.jend.mapper;

import com.seydi.jend.dto.request.AddAnnonceImageRequest;
import com.seydi.jend.dto.response.AnnonceImageResponse;
import com.seydi.jend.entity.AnnonceImage;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class AnnonceImageMapper {

    public AnnonceImage toEntity(AddAnnonceImageRequest request) {

        AnnonceImage image = new AnnonceImage();

        image.setUrl(request.url());
        image.setOrdre(request.ordre());
        image.setCreatedAt(OffsetDateTime.now());

        return image;
    }

    public AnnonceImageResponse toResponse(AnnonceImage image) {

        return new AnnonceImageResponse(
                image.getId(),
                image.getUrl(),
                image.getOrdre(),
                image.getCreatedAt()
        );
    }

}