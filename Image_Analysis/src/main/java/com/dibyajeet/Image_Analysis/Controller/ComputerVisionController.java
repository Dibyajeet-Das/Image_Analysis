package com.dibyajeet.Image_Analysis.Controller;

import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionClient;
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionManager;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ImageAnalysis;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.VisualFeatureTypes;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ImageCaption;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ImageTag;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.DetectedObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/computer-vision")
public class ComputerVisionController {

    @Value("${azure.cognitive-services.vision.subscription-key}")
    private String subscriptionKey;

    @Value("${azure.cognitive-services.vision.endpoint}")
    private String endpoint;

    private ComputerVisionClient getClient() {
        return ComputerVisionManager.authenticate(subscriptionKey).withEndpoint(endpoint);
    }

    @PostMapping("/analyze")
    public ResponseEntity<byte[]> analyzeImage(@RequestParam("file") MultipartFile file) {
        try {
            // Save the uploaded file to a temporary location
            File tempFile = File.createTempFile("upload-", file.getOriginalFilename());
            file.transferTo(tempFile);

            // Analyze the image
            ComputerVisionClient client = getClient();
            byte[] imgBytes = Files.readAllBytes(tempFile.toPath());

            List<VisualFeatureTypes> features = new ArrayList<>();
            features.add(VisualFeatureTypes.DESCRIPTION);
            features.add(VisualFeatureTypes.TAGS);
            features.add(VisualFeatureTypes.OBJECTS);

            // Detect features in the image
            ImageAnalysis analysis = client.computerVision().analyzeImageInStream()
                    .withImage(imgBytes)
                    .withVisualFeatures(features)
                    .execute();

            // Delete the temporary file
            tempFile.delete();

            // Prepare the response headers
            HttpHeaders headers = new HttpHeaders();
            if (analysis.description() != null && !analysis.description().captions().isEmpty()) {
                ImageCaption caption = analysis.description().captions().getFirst();
                headers.add("X-Analysis-Caption", caption.text() + " (confidence: " + caption.confidence() + ")");
            }

            if (analysis.tags() != null && !analysis.tags().isEmpty()) {
                StringBuilder tags = new StringBuilder();
                for (ImageTag tag : analysis.tags()) {
                    tags.append(tag.name()).append(" (confidence: ").append(tag.confidence()).append("), ");
                }
                headers.add("X-Analysis-Tags", tags.toString());
            }

            if (analysis.objects() != null && !analysis.objects().isEmpty()) {
                StringBuilder objects = new StringBuilder();
                for (DetectedObject detectedObject : analysis.objects()) {
                    objects.append(detectedObject.objectProperty()).append(" (confidence: ").append(detectedObject.confidence()).append("), ");
                }
                headers.add("X-Analysis-Objects", objects.toString());
            }

            return ResponseEntity.ok().headers(headers).body(imgBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}