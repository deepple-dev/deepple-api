package kr.co.deepple.member.presentation.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import kr.co.deepple.like.command.domain.LikeLevel;
import kr.co.deepple.member.command.domain.member.City;
import kr.co.deepple.member.command.domain.member.District;
import kr.co.deepple.member.command.domain.member.DrinkingStatus;
import kr.co.deepple.member.command.domain.member.Gender;
import kr.co.deepple.member.command.domain.member.HighestEducation;
import kr.co.deepple.member.command.domain.member.Hobby;
import kr.co.deepple.member.command.domain.member.Job;
import kr.co.deepple.member.command.domain.member.Mbti;
import kr.co.deepple.member.command.domain.member.Religion;
import kr.co.deepple.member.command.domain.member.SmokingStatus;

public record MemberInfo(
    Long id,
    String nickname,
    String profileImageUrl,
    Integer age,
    @Schema(implementation = Gender.class) String gender,
    Integer height,
    @Schema(implementation = Job.class) String job,
    @Schema(implementation = Hobby.class) Set<String> hobbies,
    @Schema(implementation = Mbti.class) String mbti,
    @Schema(implementation = City.class) String city,
    @Schema(implementation = District.class) String district,
    @Schema(implementation = SmokingStatus.class) String smokingStatus,
    @Schema(implementation = DrinkingStatus.class) String drinkingStatus,
    @Schema(implementation = HighestEducation.class) String highestEducation,
    @Schema(implementation = Religion.class) String religion,
    @Schema(implementation = LikeLevel.class) String likeLevel,
    LocalDateTime lastAccessedAt,
    @Schema(description = "메인 이미지를 제외한 추가 프로필 이미지 목록 (order 오름차순)")
        List<ProfileImageInfo> additionalProfileImages) {}
