package kr.co.deepple.member.application.dto;

import jakarta.validation.Valid;
import java.util.List;

public record ProfileImageUploadRequestWrapper(@Valid List<ProfileImageUploadRequest> requests) {}
