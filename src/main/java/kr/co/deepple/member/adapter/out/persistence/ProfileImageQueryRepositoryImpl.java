package kr.co.deepple.member.adapter.out.persistence;

import static kr.co.deepple.member.domain.QProfileImage.profileImage;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import kr.co.deepple.common.infra.s3.S3Uploader;
import kr.co.deepple.member.application.dto.ProfileImageView;
import kr.co.deepple.member.application.port.out.ProfileImageQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProfileImageQueryRepositoryImpl implements ProfileImageQueryRepository {

  private final JPAQueryFactory queryFactory;
  private final S3Uploader s3Uploader;

  @Override
  public List<ProfileImageView> findByMemberId(long memberId) {
    return queryFactory
        .select(
            profileImage.id,
            profileImage.imageId,
            profileImage.imageKey,
            profileImage.isPrimary,
            profileImage.order)
        .from(profileImage)
        .where(profileImage.memberId.eq(memberId))
        .orderBy(profileImage.order.asc())
        .fetch()
        .stream()
        .map(row ->
            new ProfileImageView(
                row.get(profileImage.id),
                row.get(profileImage.imageId),
                s3Uploader.getObjectUrl(row.get(profileImage.imageKey)),
                Boolean.TRUE.equals(row.get(profileImage.isPrimary)),
                row.get(profileImage.order)))
        .toList();
  }
}
