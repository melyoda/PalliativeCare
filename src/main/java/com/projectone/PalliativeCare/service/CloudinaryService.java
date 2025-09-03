package com.projectone.PalliativeCare.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import com.projectone.PalliativeCare.model.ResourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private final Uploader uploader;

    public String uploadFile(MultipartFile file, ResourceType resourceType) throws IOException {
        Map<String, Object> uploadOptions = new HashMap<>();

        // Set resource type based on file type
        if (resourceType == ResourceType.VIDEO) {
            uploadOptions.put("resource_type", "video");
        } else if (resourceType == ResourceType.INFOGRAPHIC) {
            uploadOptions.put("resource_type", "image");
        } else {
            uploadOptions.put("resource_type", "auto"); // Cloudinary will auto-detect
        }

        // Upload the file
        Map<?, ?> uploadResult = uploader.upload(
                file.getBytes(),
                uploadOptions
        );

        // Return the secure URL
        return uploadResult.get("secure_url").toString();
    }

    public String uploadFile(MultipartFile file) throws IOException {
        return uploadFile(file, ResourceType.TEXT); // Default to auto-detect
    }

    public void deleteFile(String publicId) throws IOException {
        // Extract public ID from URL if needed, or use the provided public ID
        uploader.destroy(publicId, ObjectUtils.emptyMap());
    }

    public void deleteFileByUrl(String fileUrl) throws IOException {
        // Extract public ID from Cloudinary URL
        String publicId = extractPublicIdFromUrl(fileUrl);
        if (publicId != null) {
            deleteFile(publicId);
        }
    }

    private String extractPublicIdFromUrl(String url) {
        try {
            // This is a simple extraction - you might need to adjust based on your URL format
            String[] parts = url.split("/");
            String lastPart = parts[parts.length - 1];
            return lastPart.split("\\.")[0]; // Remove file extension
        } catch (Exception e) {
            return null;
        }
    }
}