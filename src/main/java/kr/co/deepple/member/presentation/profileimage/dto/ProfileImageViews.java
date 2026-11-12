package kr.co.deepple.member.presentation.profileimage.dto;

import kr.co.deepple.member.query.profileimage.view.ProfileImageView;

import java.util.List;

public record ProfileImageViews(
    List<ProfileImageView> images
) {
}
