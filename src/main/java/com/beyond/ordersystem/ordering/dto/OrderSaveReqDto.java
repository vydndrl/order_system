package com.beyond.ordersystem.ordering.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class OrderSaveReqDto {
    private Long memberId;
    private List<OrderDetailDto> orderDetailDtoList;

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class OrderDetailDto {
        private Long productId;
        private Integer productCount;
    }




}
