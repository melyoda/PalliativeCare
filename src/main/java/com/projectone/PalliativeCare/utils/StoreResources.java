package com.projectone.PalliativeCare.utils;

import com.projectone.PalliativeCare.exception.FileUploadException;
import com.projectone.PalliativeCare.model.ResourceType;

import com.projectone.PalliativeCare.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class StoreResources {

    private final CloudinaryService cloudinaryService;

    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10 MB in bytes
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024; // 100 MB in bytes

    public String saveFiles(MultipartFile file) {
        try {
            ResourceType type = determineResourceType(file.getOriginalFilename());
            return cloudinaryService.uploadFile(file, type);
        } catch (IOException e) {
//            e.printStackTrace();
            throw new FileUploadException("File upload to Cloudinary failed for " + file.getOriginalFilename() + " " + e.getMessage());
        }
    }

    public ResourceType determineResourceType(String filename) {
        if (filename == null) return ResourceType.TEXT;

        String lowerFilename = filename.toLowerCase();

        if (lowerFilename.endsWith(".mp4") || lowerFilename.endsWith(".mov") ||
                lowerFilename.endsWith(".avi") || lowerFilename.endsWith(".webm")) {
            return ResourceType.VIDEO;
        }
        if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg") ||
                lowerFilename.endsWith(".png") || lowerFilename.endsWith(".gif") ||
                lowerFilename.endsWith(".webp") || lowerFilename.endsWith(".bmp")) {
            return ResourceType.INFOGRAPHIC;
        }
        if (lowerFilename.endsWith(".pdf") || lowerFilename.endsWith(".doc") ||
                lowerFilename.endsWith(".docx") || lowerFilename.endsWith(".txt")) {
            return ResourceType.TEXT;
        }
        return ResourceType.TEXT; // default
    }

    private void checkFileSize(MultipartFile file, ResourceType type) {
        long size = file.getSize();
        if ((type == ResourceType.INFOGRAPHIC && size > MAX_IMAGE_SIZE) ||
                (type == ResourceType.VIDEO && size > MAX_VIDEO_SIZE)) {
            throw new FileUploadException("File exceeds maximum allowed size for " + type +
                    ". File size: " + size + " bytes");
        }
    }
}
