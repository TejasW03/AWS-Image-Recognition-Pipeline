package com.aws.imagerecognition;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.S3Object;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.Arrays;
import java.util.List;

public class App {

    private static final String BUCKET_NAME = "njit-cs-643";
    private static final List<String> IMAGE_FILES = Arrays.asList("1.jpg", "2.jpg", "3.jpg", "4.jpg", "5.jpg", 
                                                                  "6.jpg", "7.jpg", "8.jpg", "9.jpg", "10.jpg");
    private static final String SQS_QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/907978749360/CS643PA1_ImageQueue";

    public static void main(String[] args) {
        try (RekognitionClient rekognitionClient = createRekognitionClient()) {
            IMAGE_FILES.forEach(App::processImage);
            notifyCompletion(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static RekognitionClient createRekognitionClient() {
        return RekognitionClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    private static void processImage(String imageName) {
        DetectLabelsResponse response = detectLabels(imageName);
        if (response.labels().stream().anyMatch(label -> "Car".equals(label.name()))) {
            sendMessageToSQS(imageName);
        }
    }

    private static DetectLabelsResponse detectLabels(String imageName) {
        Image image = Image.builder()
                .s3Object(S3Object.builder()
                        .bucket(BUCKET_NAME)
                        .name(imageName)
                        .build())
                .build();

        DetectLabelsRequest request = DetectLabelsRequest.builder()
                .image(image)
                .maxLabels(10)
                .minConfidence(90F)
                .build();

        return RekognitionClient.create().detectLabels(request);
    }

    private static void sendMessageToSQS(String message) {
        try (SqsClient sqsClient = createSqsClient()) {
            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                    .queueUrl(SQS_QUEUE_URL)
                    .messageBody(message)
                    .build();
            sqsClient.sendMessage(sendMsgRequest);
            System.out.println("Message sent to SQS: " + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SqsClient createSqsClient() {
        return SqsClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    private static void notifyCompletion() {
        sendMessageToSQS("-1");
    }
}
