package com.perkins.awss3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UploadObjectMPULowLevelAPI {
    static Logger logger = LoggerFactory.getLogger(UploadObjectMPULowLevelAPI.class);

    public static void testUploadFile(AmazonS3 s3Client, String keyName, String existingBucketName, String filePath) throws IOException {

        // Create a list of UploadPartResponse objects. You get one of these
        // for each part upload.
        List<PartETag> partETags = new ArrayList<PartETag>();

        // Step 1: Initialize.

        Path path = Paths.get(filePath);
        String contentType = Files.probeContentType(path);
        ObjectMetadata data = new ObjectMetadata();
        data.addUserMetadata("contenttype", contentType);
        logger.info("contetype--->" + data.getUserMetadata().get("contenttype"));
        InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(existingBucketName, keyName);
        initRequest.withObjectMetadata(data);
        InitiateMultipartUploadResult initResponse = s3Client.initiateMultipartUpload(initRequest);

        File file = new File(filePath);
        long contentLength = file.length();
        long partSize = 5242880; // Set part size to 5 MB.
//        long partSize = 1024 *1024* 2 + 100; // Set part size to 5 MB.

        try {
            // Step 2: Upload parts.
            long filePosition = 0;
            for (int i = 1; filePosition < contentLength; i++) {
                // Last part can be less than 5 MB. Adjust part size.
                partSize = Math.min(partSize, (contentLength - filePosition));

                // Create request to upload a part.
                UploadPartRequest uploadRequest = new UploadPartRequest()
                        .withBucketName(existingBucketName)
                        .withKey(keyName)
                        .withUploadId(initResponse.getUploadId())
                        .withPartNumber(i)
                        .withFileOffset(filePosition)
                        .withFile(file)
                        .withPartSize(partSize);

                // Upload part and add response to our list.
                partETags.add(s3Client.uploadPart(uploadRequest).getPartETag());

                filePosition += partSize;
            }

            // Step 3: Complete.
            CompleteMultipartUploadRequest compRequest =
                    new CompleteMultipartUploadRequest(existingBucketName, keyName, initResponse.getUploadId(), partETags);
            s3Client.completeMultipartUpload(compRequest);
            logger.info("文件上传结束"+keyName);
        } catch (Exception e) {
            e.printStackTrace();
            s3Client.abortMultipartUpload(new AbortMultipartUploadRequest(existingBucketName, keyName, initResponse.getUploadId()));
        }
    }

    // 列出正在分段上传的任务，这些任任务可能是分段上传失败忘记终止的任务
    public static List<MultipartUpload> listMultipartUploads(AmazonS3 s3Client, String bucketName) {
        try {
            // Retrieve a list of all in-progress multipart uploads.
            ListMultipartUploadsRequest allMultipartUploadsRequest = new ListMultipartUploadsRequest(bucketName);
            MultipartUploadListing multipartUploadListing = s3Client.listMultipartUploads(allMultipartUploadsRequest);
            List<MultipartUpload> uploads = multipartUploadListing.getMultipartUploads();

            // Display information about all in-progress multipart uploads.
            System.out.println(uploads.size() + " multipart upload(s) in progress.");
            for (MultipartUpload u : uploads) {
                System.out.println("Upload in progress: Key = \"" + u.getKey() + "\", id = " + u.getUploadId());
            }
            return uploads;
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace();
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }
        return new ArrayList<>(0);
    }
}