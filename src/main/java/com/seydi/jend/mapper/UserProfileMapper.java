package com.seydi.jend.mapper;

import com.seydi.jend.dto.request.UpdateProfileRequest;
import com.seydi.jend.dto.response.AdminUserResponse;
import com.seydi.jend.dto.response.MyProfileResponse;
import com.seydi.jend.dto.response.UserProfileResponse;
import com.seydi.jend.entity.UserProfile;
import org.springframework.stereotype.Component;

@Component
public class UserProfileMapper {

    public UserProfileResponse toPublicResponse(UserProfile user) {

        return new UserProfileResponse(
                user.getId(),
                user.getNom(),
                user.getTelephone(),
                user.getVille(),
                user.isEstProfessionnel(),
                user.getRole(),
                user.getCreatedAt()
        );
    }

    public MyProfileResponse toMyProfileResponse(UserProfile user) {

        return new MyProfileResponse(
                user.getId(),
                user.getNom(),
                user.getEmail(),
                user.getTelephone(),
                user.getVille(),
                user.isEstProfessionnel(),
                user.isSuspendu(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public void updateEntity(
            UserProfile user,
            UpdateProfileRequest request
    ) {

        if (request.nom() != null) {
            user.setNom(request.nom());
        }

        if (request.telephone() != null) {
            user.setTelephone(request.telephone());
        }

        if (request.ville() != null) {
            user.setVille(request.ville());
        }
    }

    public AdminUserResponse toAdminResponse(UserProfile user) {

        return new AdminUserResponse(
                user.getId(),
                user.getNom(),
                user.getEmail(),
                user.getTelephone(),
                user.getVille(),
                user.isEstProfessionnel(),
                user.isSuspendu(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}