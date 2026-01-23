package com.guibsantos.shorterURL.controller;

import com.guibsantos.shorterURL.controller.docs.UserControllerDocs;
import com.guibsantos.shorterURL.controller.dto.response.UserResponse;
import com.guibsantos.shorterURL.service.FileStorageService;
import com.guibsantos.shorterURL.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
public class UserController implements UserControllerDocs {

    private final UserService userService;
    private final FileStorageService fileStorageService;

    public UserController(UserService userService, FileStorageService fileStorageService) {
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        String filename = fileStorageService.storeFile(file);

        String fileDownloadUri = "http://10.0.2.2:8080/uploads/" + filename;

        UserResponse updatedUser = userService.updateAvatar(authentication.getName(), fileDownloadUri);

        return ResponseEntity.ok(updatedUser);
    }
}
