package kr.co.deepple.member.application.port.in;

import java.util.Set;

public record ProfileOptions(
    Set<String> cities,
    Set<String> hobbies,
    String religion,
    String smokingStatus,
    String drinkingStatus) {}
