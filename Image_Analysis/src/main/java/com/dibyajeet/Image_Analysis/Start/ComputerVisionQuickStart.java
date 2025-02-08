package com.dibyajeet.Image_Analysis.Start;

import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionClient;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ImageAnalysis;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.VisualFeatureTypes;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ComputerVisionQuickStart {

    public static void DetectImageTypesInImage(ComputerVisionClient client, String localImagePath, String remoteImageURL) {
        System.out.println("-----------------------------------------------");
        System.out.println("DETECT TYPE OF IMAGE");
        System.out.println();
        try {
            File localImage = new File(localImagePath);
            byte[] imgBytes = Files.readAllBytes(localImage.toPath());

            List<VisualFeatureTypes> features = new ArrayList<>();
            features.add(VisualFeatureTypes.IMAGE_TYPE);

            // Detect type of local image
            ImageAnalysis analysisLocal = client.computerVision().analyzeImageInStream()
                    .withImage(imgBytes)
                    .withVisualFeatures(features)
                    .execute();

            System.out.println("Local image type: " + analysisLocal.imageType().clipArtType());

            if (remoteImageURL != null && !remoteImageURL.isEmpty()) {
                // Detect type of remote URL image
                ImageAnalysis analysisRemote = client.computerVision().analyzeImage()
                        .withUrl(remoteImageURL)
                        .withVisualFeatures(features)
                        .execute();

                System.out.println("Remote image type: " + analysisRemote.imageType().clipArtType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println();
    }
}