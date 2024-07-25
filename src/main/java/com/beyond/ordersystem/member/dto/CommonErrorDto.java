package com.beyond.ordersystem.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonErrorDto {
    private int status_code;
    private String error_message;
}
