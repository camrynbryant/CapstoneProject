package com.capstone.service;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProfilePictureStorageService {
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads";
    private static final String BASE_URL = "http://localhost:8080";

    public String storeFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty() || originalFilename.contains("..")) {
            throw new IOException("Invalid file name: " + originalFilename);
        }
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex != -1) {
            extension = originalFilename.substring(dotIndex);
        }
        String lowerExt = extension.toLowerCase();
        if (!lowerExt.equals(".jpg") && !lowerExt.equals(".jpeg")) {
            throw new IOException("Only JPG/JPEG files are allowed.");
        }
        String filename = UUID.randomUUID().toString() + extension;
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        File dest = new File(uploadDir, filename);
        file.transferTo(dest);
        return BASE_URL + "/uploads/" + filename;
    }
}
