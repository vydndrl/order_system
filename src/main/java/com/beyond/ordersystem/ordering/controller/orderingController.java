package com.beyond.ordersystem.ordering.controller;

import com.beyond.ordersystem.member.dto.CommonResDto;
import com.beyond.ordersystem.ordering.domain.Ordering;
import com.beyond.ordersystem.ordering.dto.OrderListResDto;
import com.beyond.ordersystem.ordering.dto.OrderSaveReqDto;
import com.beyond.ordersystem.ordering.service.OrderingService;
import org.springframework.beans.factory.annotation.Autowired;;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class orderingController {
    private final OrderingService orderingService;

    @Autowired
    public orderingController(OrderingService orderingService) {
        this.orderingService = orderingService;
    }

    @PostMapping("/order/create")
    public ResponseEntity<Object> createOrder(@RequestBody List<OrderSaveReqDto> dto) {
        Ordering ordering = orderingService.orderCreate(dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "product is successfully created", ordering.getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("order/list")
    public ResponseEntity<Object> orderList() {
        List<OrderListResDto> orderList = orderingService.orderList();
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "product is successfully created", orderList);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

//    내 주문만 볼 수 있는 myOrders : order/myorders
    @GetMapping("order/myorders")
    public ResponseEntity<Object> myOrders() {
        List<OrderListResDto> orderList = orderingService.myOrders();
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "My Orders", orderList);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

//    admin 사용자가 주문 취소 : /order/{id}/cancel -> orderstatus 만 변경
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("order/{id}/cancel")
    public ResponseEntity<Object> cacnelOrder(@PathVariable Long id) {
        orderingService.orderCancel(id);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "My Info", null);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }
}
