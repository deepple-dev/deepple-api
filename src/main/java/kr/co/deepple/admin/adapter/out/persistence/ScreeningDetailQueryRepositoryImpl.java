package kr.co.deepple.admin.adapter.out.persistence;

import static kr.co.deepple.admin.domain.QScreening.screening;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import kr.co.deepple.admin.application.dto.QRawScreeningDetailView;
import kr.co.deepple.admin.application.dto.RawScreeningDetailView;
import kr.co.deepple.admin.application.port.out.ScreeningDetailQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ScreeningDetailQueryRepositoryImpl implements ScreeningDetailQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
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
