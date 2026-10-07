package com.seydi.jend.controller;

import com.seydi.jend.dto.request.UpdateProfileRequest;
import com.seydi.jend.dto.response.MyProfileResponse;
import com.seydi.jend.dto.response.UserProfileResponse;
import com.seydi.jend.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/me")
    public MyProfileResponse getMyProfile() {
        return userProfileService.getMyProfile();
    }

    @PutMapping("/me")
    public MyProfileResponse updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return userProfileService.updateMyProfile(request);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMyAccount() {
        userProfileService.deleteMyAccount();
    }

    @GetMapping("/{id}")
    public UserProfileResponse getPublicProfile(
            @PathVariable Long id
    ) {
        return userProfileService.getPublicProfile(id);
    }

    @PatchMapping("/{id}/suspend")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void suspendUser(
            @PathVariable Long id
    ) {
        userProfileService.suspendUser(id);
    }

    @PatchMapping("/{id}/reactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reactivateUser(
            @PathVariable Long id
    ) {
        userProfileService.reactivateUser(id);
    }
}