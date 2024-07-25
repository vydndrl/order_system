package com.beyond.ordersystem.member.domain;

import com.beyond.ordersystem.common.domain.Address;
import com.beyond.ordersystem.common.domain.BaseTimeEntity;
import com.beyond.ordersystem.member.dto.MemberResDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(nullable = false, unique = true)
    private String email;
    private String password;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    private Role role;

    public MemberResDto fromEntity() {
        return MemberResDto.builder()
                .id(this.id)
                .name(this.name)
                .email(this.email)
                .address(this.address)
                .build();
    }
}
