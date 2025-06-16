package com.aws.textrecognition;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectTextRequest;
import software.amazon.awssdk.services.rekognition.model.DetectTextResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.S3Object;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class App {

    private static final String BUCKET_NAME = "njit-cs-643"; // S3 bucket name
    private static final String SQS_QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/907978749360/CS643PA1_ImageQueue"; // SQS URL

    public static void main(String[] args) {
        try (SqsClient sqsClient = createSqsClient()) {
            while (true) {
                List<Message> messages = receiveMessages(sqsClient);
                if (messages.isEmpty()) {
                    System.out.println("Waiting for new images...");
                    continue;
                }
                messages.forEach(App::processMessage);
            }
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

    private static List<Message> receiveMessages(SqsClient sqsClient) {
        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(SQS_QUEUE_URL)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(10)
                .build();

        ReceiveMessageResponse response = sqsClient.receiveMessage(receiveRequest);
        return response.messages();
    }

    private static void processMessage(Message message) {
        String imageName = message.body();
        if ("-1".equals(imageName)) {
            System.out.println("Termination signal received.");
            System.exit(0);
        }

        System.out.println("Processing image: " + imageName);
        handleImage(imageName);
        deleteMessage(message);
    }

    private static void handleImage(String imageName) {
        File imageFile = downloadImage(imageName);
        detectTextInImage(imageFile);
    }

    private static File downloadImage(String imageName) {
        File imageFile = new File("/tmp/" + imageName);
        try (S3Client s3Client = createS3Client()) {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(imageName)
                    .build();

            try (ResponseInputStream<?> objectData = s3Client.getObject(getObjectRequest);
                 FileOutputStream fos = new FileOutputStream(imageFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = objectData.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
                System.out.println("Downloaded image: " + imageName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageFile;
    }

    private static S3Client createS3Client() {
        return S3Client.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    private static void detectTextInImage(File imageFile) {
        try (RekognitionClient rekognitionClient = createRekognitionClient()) {
            Image image = Image.builder()
                    .s3Object(S3Object.builder()
                            .bucket(BUCKET_NAME)
                            .name(imageFile.getName())
                            .build())
                    .build();

            DetectTextRequest request = DetectTextRequest.builder()
                    .image(image)
                    .build();

            DetectTextResponse response = rekognitionClient.detectText(request);
            response.textDetections().forEach(detectedText ->
                    System.out.println("Detected Text: " + detectedText.detectedText()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void deleteMessage(Message message) {
        try (SqsClient sqsClient = createSqsClient()) {
            sqsClient.deleteMessage(builder -> builder.queueUrl(SQS_QUEUE_URL).receiptHandle(message.receiptHandle()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
