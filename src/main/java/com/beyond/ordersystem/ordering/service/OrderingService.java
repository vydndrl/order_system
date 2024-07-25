package com.beyond.ordersystem.ordering.service;

import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.repository.MemberRepository;
import com.beyond.ordersystem.ordering.domain.OrderDetail;
import com.beyond.ordersystem.ordering.domain.Ordering;
import com.beyond.ordersystem.ordering.dto.OrderSaveReqDto;
import com.beyond.ordersystem.ordering.repository.OrderingRepository;
import com.beyond.ordersystem.product.domain.Product;
import com.beyond.ordersystem.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrderingService {

    private final OrderingRepository orderingRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;


    @Autowired
    public OrderingService(OrderingRepository orderingRepository, ProductRepository productRepository, MemberRepository memberRepository) {
        this.orderingRepository = orderingRepository;
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
    }

    public Ordering orderCreate(OrderSaveReqDto dto) {
        Member member = memberRepository.findById(dto.getMemberId()).orElseThrow(() -> new EntityNotFoundException("Member not found"));

        Ordering ordering = Ordering.builder()
                .member(member)
                .build();

        for(OrderSaveReqDto.OrderDetailDto orderDetailDto : dto.getOrderDetailDtoList()) {
            Product product = productRepository.findById(orderDetailDto.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Member not found"));
            OrderDetail orderDetail = OrderDetail.builder()
                    .product(product)
                    .quantity(orderDetailDto.getProductCount())
                    .build();
            ordering.getOrderDetails().add(orderDetail);
        }


        Ordering savedOrdering = orderingRepository.save(ordering);
        return savedOrdering;
    }
}
