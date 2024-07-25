package com.beyond.ordersystem.member.dto;

import com.beyond.ordersystem.common.domain.Address;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberSaveReqDto {
    private String name;
    @NotEmpty(message = "email is essential")
    private String email;
    @NotEmpty(message = "password is essential")
    @Size(min = 8, message = "password minimum length is 8")
    private String password;
    private Address address;
    @Enumerated(EnumType.STRING)
    private Role role;
//    private PasswordEncoder passwordEncoder;

    public Member toEntity() {
        return Member.builder()
                .name(this.name)
                .email(this.email)
                .address(this.address)
                .password(this.password)
                .role(Role.USER)
                .build();
    }
}
