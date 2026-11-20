package kr.co.deepple.feed.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record TextPosition(
    @Column(name = "text_x") double x,
    @Column(name = "text_y") double y,
    @Column(name = "text_rotation") double rotation,
    @Column(name = "text_scale") double scale) {}
