package deepple.deepple.common.infra.s3.exception;

import software.amazon.awssdk.awscore.exception.AwsServiceException;

public class S3AmazonException extends RuntimeException {
    private final AwsServiceException exception;

    public S3AmazonException(AwsServiceException awsServiceException) {
        super("S3 서비스에 문제가 발생하였습니다.");
        this.exception = awsServiceException;
    }

    public AwsServiceException getException() {
        return exception;
    }
}
