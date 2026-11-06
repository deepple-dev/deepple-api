package deepple.deepple.block.adapter.listener;

import deepple.deepple.block.application.provided.BlockCommander;
import deepple.deepple.report.command.domain.event.ReportCreatedEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BlockEventHandler {
    private final BlockCommander blockCommander;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ReportCreatedEvent event) {
        blockCommander.createBlockIfAbsent(event.getReporterId(), event.getReporteeId());
    }
}
