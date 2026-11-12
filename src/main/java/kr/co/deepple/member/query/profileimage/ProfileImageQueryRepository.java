package kr.co.deepple.member.query.profileimage;

import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.deepple.member.query.profileimage.view.ProfileImageView;
import kr.co.deepple.member.query.profileimage.view.QProfileImageView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static kr.co.deepple.member.command.domain.profileImage.QProfileImage.profileImage;

@Repository
@RequiredArgsConstructor
public class ProfileImageQueryRepository {
    private final JPAQueryFactory queryFactory;

    public List<ProfileImageView> findByMemberId(Long memberId) {
        return queryFactory
            .select(new QProfileImageView(
                profileImage.id,
                profileImage.imageUrl.value,
                profileImage.isPrimary,
                profileImage.order
            ))
            .from(profileImage)
            .where(profileImage.memberId.eq(memberId))
            .orderBy(profileImage.order.asc())
            .fetch();
    }
}
