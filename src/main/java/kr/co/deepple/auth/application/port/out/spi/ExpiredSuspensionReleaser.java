package kr.co.deepple.auth.application.port.out.spi;

/** 만료된 일시 정지를 해제한다. 정지 기록은 admin 모듈이 가지고 있어 admin이 구현한다. */
public interface ExpiredSuspensionReleaser {

  /** 일시 정지가 만료됐으면 해제한다. 이미 해제됐거나 영구 정지면 아무것도 하지 않는다. */
  void releaseIfExpired(long memberId);
}
