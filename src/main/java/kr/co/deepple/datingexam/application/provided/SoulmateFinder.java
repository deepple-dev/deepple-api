package kr.co.deepple.datingexam.application.provided;

import java.util.Set;

public interface SoulmateFinder {
    Set<Long> findSoulmateIds(Long memberId);
}
