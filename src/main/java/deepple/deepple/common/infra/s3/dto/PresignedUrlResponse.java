package deepple.deepple.common.infra.s3.dto;

public record PresignedUrlResponse(
    String presignedUrl,
    String objectUrl
) {
}
