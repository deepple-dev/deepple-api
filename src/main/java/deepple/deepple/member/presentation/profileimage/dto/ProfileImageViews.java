package deepple.deepple.member.presentation.profileimage.dto;

import deepple.deepple.member.query.profileimage.view.ProfileImageView;

import java.util.List;

public record ProfileImageViews(
    List<ProfileImageView> images
) {
}
