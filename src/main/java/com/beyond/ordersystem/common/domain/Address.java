package com.beyond.ordersystem.common.domain;

import lombok.*;

import javax.persistence.Embeddable;

// 타 엔티티에서 사용 가능한 형태로 만드는 어노테이션
@Embeddable
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Address {
    private String city;
    private String street;
    private String zipcode;
}