package com.example.video;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.videointelligence.v1.AnnotateVideoProgress;
import com.google.cloud.videointelligence.v1.AnnotateVideoRequest;
import com.google.cloud.videointelligence.v1.AnnotateVideoResponse;
import com.google.cloud.videointelligence.v1.Entity;
import com.google.cloud.videointelligence.v1.ExplicitContentFrame;
import com.google.cloud.videointelligence.v1.Feature;
import com.google.cloud.videointelligence.v1.LabelAnnotation;
import com.google.cloud.videointelligence.v1.LabelSegment;
import com.google.cloud.videointelligence.v1.VideoAnnotationResults;
import com.google.cloud.videointelligence.v1.VideoIntelligenceServiceClient;
import com.google.cloud.videointelligence.v1.VideoSegment;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.codec.binary.Base64;


public class FindPeople {
  public static void main(String[] args){

    System.out.println("Hello World");

  }
  /**
   * Detects labels, shots, and explicit content in a video using the Video Intelligence API
   * @param args specifies features to detect and the path to the video on Google Cloud Storage.
   */
  public static void main(String[] args) {
    try {
      argsHelper(args);
    } catch (Exception e) {
      System.out.println("Exception while running:\n" + e.getMessage() + "\n");
      e.printStackTrace(System.out);
    }
  }

  /**
   * Helper that handles the input passed to the program.
   * @param args specifies features to detect and the path to the video on Google Cloud Storage.
   *
   * @throws IOException on Input/Output errors.
   */
  public static void argsHelper(String[] args) throws Exception {
    if (args.length < 1) {
      System.out.println("Usage:");
      System.out.printf(
          "\tjava %s \"<command>\" \"<path-to-video>\"\n"
              + "Commands:\n"
              + "\tlabels | shots\n"
              + "Path:\n\tA URI for a Cloud Storage resource (gs://...)\n"
              + "Examples: ",
          Detect.class.getCanonicalName());
      return;
    }
    String command = args[0];
    String path = args.length > 1 ? args[1] : "";

    if (command.equals("labels")) {
      analyzeLabels(path);
    }
    if (command.equals("labels-file")) {
      analyzeLabelsFile(path);
    }
    if (command.equals("shots")) {
      analyzeShots(path);
    }
    if (command.equals("explicit-content")) {
      analyzeExplicitContent(path);
    }
  }

  /**
   * Performs label analysis on the video at the provided file path.
   *
   * @param filePath the path to the video file to analyze.
   */
  public static void analyzeLabelsFile(String filePath) throws Exception {
    // [START video_analyze_labels]
    // Instantiate a com.google.cloud.videointelligence.v1.VideoIntelligenceServiceClient
    try (VideoIntelligenceServiceClient client = VideoIntelligenceServiceClient.create()) {
      // Read file and encode into Base64
      Path path = Paths.get(filePath);
      byte[] data = Files.readAllBytes(path);
      byte[] encodedBytes = Base64.encodeBase64(data);

      AnnotateVideoRequest request = AnnotateVideoRequest.newBuilder()
          .setInputContent(ByteString.copyFrom(encodedBytes))
          .addFeatures(Feature.LABEL_DETECTION)
          .build();

      // Create an operation that will contain the response when the operation completes.
      OperationFuture<AnnotateVideoResponse, AnnotateVideoProgress> response =
          client.annotateVideoAsync(request);

      System.out.println("Waiting for operation to complete...");
      for (VideoAnnotationResults results : response.get().getAnnotationResultsList()) {
        // process video / segment level label annotations
        System.out.println("Locations: ");
        for (LabelAnnotation labelAnnotation : results.getSegmentLabelAnnotationsList()) {
          System.out
              .println("Video label: " + labelAnnotation.getEntity().getDescription());
          // categories
          for (Entity categoryEntity : labelAnnotation.getCategoryEntitiesList()) {
            System.out.println("Video label category: " + categoryEntity.getDescription());
          }
          // segments
          for (LabelSegment segment : labelAnnotation.getSegmentsList()) {
            double startTime = segment.getSegment().getStartTimeOffset().getSeconds()
                + segment.getSegment().getStartTimeOffset().getNanos() / 1e9;
            double endTime = segment.getSegment().getEndTimeOffset().getSeconds()
                + segment.getSegment().getEndTimeOffset().getNanos() / 1e9;
            System.out.printf("Segment location: %.3f:%.2f\n", startTime, endTime);
            System.out.println("Confidence: " + segment.getConfidence());
          }
        }

        // process shot label annotations
        for (LabelAnnotation labelAnnotation : results.getShotLabelAnnotationsList()) {
          System.out
              .println("Shot label: " + labelAnnotation.getEntity().getDescription());
          // categories
          for (Entity categoryEntity : labelAnnotation.getCategoryEntitiesList()) {
            System.out.println("Shot label category: " + categoryEntity.getDescription());
          }
          // segments
          for (LabelSegment segment : labelAnnotation.getSegmentsList()) {
            double startTime = segment.getSegment().getStartTimeOffset().getSeconds()
                + segment.getSegment().getStartTimeOffset().getNanos() / 1e9;
            double endTime = segment.getSegment().getEndTimeOffset().getSeconds()
                + segment.getSegment().getEndTimeOffset().getNanos() / 1e9;
            System.out.printf("Segment location: %.3f:%.2f\n", startTime, endTime);
            System.out.println("Confidence: " + segment.getConfidence());
          }
        }

        // process frame label annotations
        for (LabelAnnotation labelAnnotation : results.getFrameLabelAnnotationsList()) {
          System.out
              .println("Frame label: " + labelAnnotation.getEntity().getDescription());
          // categories
          for (Entity categoryEntity : labelAnnotation.getCategoryEntitiesList()) {
            System.out.println("Frame label category: " + categoryEntity.getDescription());
          }
          // segments
          for (LabelSegment segment : labelAnnotation.getSegmentsList()) {
            double startTime = segment.getSegment().getStartTimeOffset().getSeconds()
                + segment.getSegment().getStartTimeOffset().getNanos() / 1e9;
            double endTime = segment.getSegment().getEndTimeOffset().getSeconds()
                + segment.getSegment().getEndTimeOffset().getNanos() / 1e9;
            System.out.printf("Segment location: %.3f:%.2f\n", startTime, endTime);
            System.out.println("Confidence: " + segment.getConfidence());
          }
        }
      }
    }
    // [END video_analyze_labels]
  }

}
