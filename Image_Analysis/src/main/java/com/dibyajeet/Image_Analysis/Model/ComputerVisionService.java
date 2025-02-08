package com.dibyajeet.Image_Analysis.Model;

import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionClient;
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionManager;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ImageAnalysis;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.VisualFeatureTypes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Service
public class ComputerVisionService {

    @Value("${azure.cognitive-services.vision.subscription-key}")
    private String subscriptionKey;

    @Value("${azure.cognitive-services.vision.endpoint}")
    private String endpoint;

    public ComputerVisionClient getClient() {
        return ComputerVisionManager.authenticate(subscriptionKey).withEndpoint(endpoint);
    }

    public ImageAnalysis detectImageType(String localImagePath) throws Exception {
        ComputerVisionClient client = getClient();
        File localImage = new File(localImagePath);
        byte[] imgBytes = Files.readAllBytes(localImage.toPath());

        List<VisualFeatureTypes> features = new ArrayList<>();
        features.add(VisualFeatureTypes.IMAGE_TYPE);

        return client.computerVision().analyzeImageInStream()
                .withImage(imgBytes)
                .withVisualFeatures(features)
                .execute();
    }
}