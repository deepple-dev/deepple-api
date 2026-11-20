package kr.co.deepple.image.application.port.in;

public interface FindImageUseCase {

  ImageInfo getMemberImage(long imageId, long memberId);
}
