package kr.co.deepple.recommendation.adapter.out.persistence;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.set;
import static com.querydsl.core.types.dsl.Expressions.enumPath;
import static kr.co.deepple.recommendation.domain.QMemberIdeal.memberIdeal;

import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import kr.co.deepple.member.command.domain.member.City;
import kr.co.deepple.member.command.domain.member.Hobby;
import kr.co.deepple.recommendation.application.dto.MemberIdealView;
import kr.co.deepple.recommendation.application.dto.QMemberIdealView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberIdealQueryRepository {

  private final JPAQueryFactory queryFactory;

  public Optional<MemberIdealView> findMemberIdealByMemberId(long memberId) {
    EnumPath<City> city = enumPath(City.class, "cityAlias");
    EnumPath<Hobby> hobby = enumPath(Hobby.class, "hobbyAlias");

    MemberIdealView memberIdealView =
        queryFactory
            .from(memberIdeal)
            .where(memberIdeal.memberId.eq(memberId))
            .leftJoin(memberIdeal.hobbies, hobby)
            .leftJoin(memberIdeal.cities, city)
            .transform(
                groupBy(memberIdeal.memberId)
                    .as(
                        new QMemberIdealView(
                            memberIdeal.ageRange.minAge,
                            memberIdeal.ageRange.maxAge,
                            set(hobby.stringValue()),
                            set(city.stringValue()),
                            memberIdeal.religion.stringValue(),
                            memberIdeal.smokingStatus.stringValue(),
                            memberIdeal.drinkingStatus.stringValue())))
            .get(memberId);

    return Optional.ofNullable(memberIdealView);
  }
}
