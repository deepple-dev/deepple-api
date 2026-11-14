package kr.co.deepple.common.config;

import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApiTags {

  public static final String AUTH = "인증";
  public static final String MEMBER = "회원";
  public static final String RECOMMENDATION = "이성 소개";
  public static final String MATCH = "매칭";
  public static final String LIKE = "좋아요";
  public static final String HEART = "하트";
  public static final String PAYMENT = "결제";
  public static final String NOTIFICATION = "알림";
  public static final String COMMUNITY = "커뮤니티";
  public static final String INTERVIEW = "인터뷰";
  public static final String DATING_EXAM = "연애 모의고사";
  public static final String BLOCK = "차단";
  public static final String REPORT = "신고";
  public static final String SCREENING = "심사·제재";
  public static final String FEED = "피드";

  static List<Tag> definitions() {
    return List.of(
        tag(AUTH, "SMS 인증, 로그인·가입, 토큰 재발급, 관리자 계정 승인"),
        tag(MEMBER, "내 정보, 상대 프로필, 프로필 이미지, 관리자 회원 목록·상세"),
        tag(RECOMMENDATION, "이상형, 소개 목록, 오늘의 카드, 소울메이트·성격 유형 소개, 프로필 블러 해제"),
        tag(MATCH, "매칭 요청 메시지, 수락·거절, 메시지 내역"),
        tag(LIKE, "좋아요 전송, 보낸·받은 목록"),
        tag(HEART, "하트 보유량, 하트 거래 내역"),
        tag(PAYMENT, "App Store·Google Play 영수증 검증, 하트 구매 옵션, 환불"),
        tag(NOTIFICATION, "알림 조회·읽음·삭제, 수신 설정, 기기 등록"),
        tag(COMMUNITY, "셀프 소개, 프로필 교환 신청·수락·거절"),
        tag(INTERVIEW, "인터뷰 질문, 사용자 답변"),
        tag(DATING_EXAM, "연애 모의고사 문항, 답안 제출, 결과 기반 소개"),
        tag(BLOCK, "사용자 차단"),
        tag(REPORT, "사용자 신고, 신고 처리"),
        tag(SCREENING, "프로필 심사, 재심사 요청, 정지·경고"),
        tag(FEED, "피드, 좋아요, 투표, 댓글, 이미지 업로드. 구현 전이라 고정된 예시 값을 응답한다"));
  }

  private static Tag tag(String name, String description) {
    return new Tag().name(name).description(description);
  }
}
