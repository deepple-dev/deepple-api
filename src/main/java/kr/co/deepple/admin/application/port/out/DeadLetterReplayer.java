package kr.co.deepple.admin.application.port.out;

public interface DeadLetterReplayer {

  long replay(String topic);
}
