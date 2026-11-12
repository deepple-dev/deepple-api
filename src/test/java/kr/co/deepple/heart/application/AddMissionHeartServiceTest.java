package kr.co.deepple.heart.application;

import static org.mockito.Mockito.verify;

import kr.co.deepple.heart.application.port.in.AddMissionHeartCommand;
import kr.co.deepple.heart.domain.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AddMissionHeartServiceTest {

  @Mock private HeartTransactor heartTransactor;

  @InjectMocks private AddMissionHeartService service;

  @Test
  @DisplayName("미션 종류와 미션 이름으로 멱등 키 없이 거래한다.")
  void transact() {
    // when
    service.addMissionHeart(new AddMissionHeartCommand(1L, 10L, "Daily Login"));

    // then
    verify(heartTransactor).transact(1L, TransactionType.MISSION, "Daily Login", 10L, null);
  }
}
