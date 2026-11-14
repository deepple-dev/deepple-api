package kr.co.deepple.member.adapter.out.persistence;

import static kr.co.deepple.member.domain.QProfileImage.profileImage;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import kr.co.deepple.member.application.dto.ProfileImageView;
import kr.co.deepple.member.application.dto.QProfileImageView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProfileImageQueryRepository {

  private final JPAQueryFactory queryFactory;

  public List<ProfileImageView> findByMemberId(long memberId) {
    return queryFactory
        .select(
            new QProfileImageView(
                profileImage.id,
                profileImage.imageUrl.value,
                profileImage.isPrimary,
                profileImage.order))
        .from(profileImage)
        .where(profileImage.memberId.eq(memberId))
        .orderBy(profileImage.order.asc())
        .fetch();
  }
}
