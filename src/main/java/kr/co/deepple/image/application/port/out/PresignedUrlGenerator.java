package kr.co.deepple.image.application.port.out;

public interface PresignedUrlGenerator {

  String generate(String key, String contentType, long contentLength);
}
