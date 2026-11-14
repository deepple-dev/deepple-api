package kr.co.deepple.admin.adapter.out.persistence;

import static kr.co.deepple.admin.domain.QScreening.screening;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import kr.co.deepple.admin.application.dto.QRawScreeningDetailView;
import kr.co.deepple.admin.application.dto.RawScreeningDetailView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ScreeningDetailQueryRepository {

  private final JPAQueryFactory queryFactory;

  public Optional<RawScreeningDetailView> findById(long screeningId) {
    return Optional.ofNullable(
        queryFactory
            .select(
                new QRawScreeningDetailView(
                    screening.id,
                    screening.version,
                    screening.memberId,
                    screening.status.stringValue(),
                    screening.rejectionReason.stringValue()))
            .from(screening)
            .where(screening.id.eq(screeningId))
            .fetchOne());
  }
}
