package kr.co.deepple.member.presentation.member;

import kr.co.deepple.member.command.application.member.dto.MemberLoginServiceDto;
import kr.co.deepple.member.presentation.member.dto.MemberLoginResponse;

public class MemberDtoMapper {
    public static MemberLoginResponse toMemberLoginResponse(MemberLoginServiceDto dto) {
        return new MemberLoginResponse(dto.accessToken(), dto.isProfileSettingNeeded(), dto.activityStatus());
    }
}
