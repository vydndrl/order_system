package com.beyond.ordersystem.ordering.service;

import com.beyond.ordersystem.common.service.StockInventoryService;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.repository.MemberRepository;
import com.beyond.ordersystem.ordering.domain.OrderDetail;
import com.beyond.ordersystem.ordering.domain.OrderStatus;
import com.beyond.ordersystem.ordering.domain.Ordering;
import com.beyond.ordersystem.ordering.dto.OrderListResDto;
import com.beyond.ordersystem.ordering.dto.OrderSaveReqDto;
import com.beyond.ordersystem.ordering.dto.StockDecreaseEvent;
import com.beyond.ordersystem.ordering.repository.OrderDetailRepository;
import com.beyond.ordersystem.ordering.repository.OrderingRepository;
import com.beyond.ordersystem.product.domain.Product;
import com.beyond.ordersystem.product.dto.ProductResDto;
import com.beyond.ordersystem.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderingService {

    private final OrderingRepository orderingRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final StockInventoryService stockInventoryService;
    private final StockDecreaseEventHandler stockDecreaseEventHandler;


    @Autowired
    public OrderingService(OrderingRepository orderingRepository, ProductRepository productRepository, MemberRepository memberRepository, OrderDetailRepository orderDetailRepository, StockInventoryService stockInventoryService, StockDecreaseEventHandler stockDecreaseEventHandler) {
        this.orderingRepository = orderingRepository;
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.stockInventoryService = stockInventoryService;
        this.stockDecreaseEventHandler = stockDecreaseEventHandler;
    }
    
//    syncronized 를 설정한다 하더라도, 재고 감소가 DB에 반영되는 시점은 트랜잭션이 커밋 되고 종료되는 시점
    public Ordering orderCreate(List<OrderSaveReqDto> dtos) {
//    방법2. JPA에 최적화 된 방식
//        Member member = memberRepository.findById(dto.getMemberId())
//                .orElseThrow(() -> new EntityNotFoundException("없음"));
        String memberEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));
        Ordering ordering = Ordering.builder()
                .member(member)
                .build();
        for (OrderSaveReqDto dto : dtos) {
            Product product = productRepository.findById(dto.getProductId()).orElse(null);
            int quantity = dto.getProductCount();

            if (product.getName().contains("sale")) {
//                redis를 통한 재고 관리 및 재고 잔량 확인
                int newQuantity =  stockInventoryService.decreaseStock(dto.getProductId(), dto.getProductCount()).intValue();
                if (newQuantity < 0) {
                    throw new IllegalArgumentException("재고 부족");
                }
//                rdb에 재고를 업데이트. rabbitmq를 통해 비동기적으로 이벤트 처리.
                stockDecreaseEventHandler.publish(new StockDecreaseEvent((product.getId()), dto.getProductCount()));
            } else {
                if (product.getStockQuantity() < quantity) {
                    throw new IllegalArgumentException("재고 부족");
                }
                product.updateStockQuantity(quantity); // 변경감지(Dirty Checking)로 인해 별도의 save 불필요
            }


            OrderDetail orderDetail = OrderDetail.builder()
                    .product(product)
                    .quantity(quantity)
                    .ordering(ordering)
                    .build();
            ordering.getOrderDetails().add(orderDetail);
        }
        Ordering savedOrdering = orderingRepository.save(ordering);
        return savedOrdering;
    }

    public List<OrderListResDto> orderList () {
        List<Ordering> orderings = orderingRepository.findAll();
        List<OrderListResDto> orderListResDtos = new ArrayList<>();
        for (Ordering ordering : orderings) {
            orderListResDtos.add(ordering.fromEntity());
        }
        return orderListResDtos;

        //        //        방법1.쉬운방식
////        Ordering생성 : member_id, status
//        Member member = memberRepository.findById(dto.getMemberId()).orElseThrow(() -> new EntityNotFoundException("없음"));
//        Ordering ordering = orderingRepository.save(dto.toEntity(member));
//
////        OrderDetail생성 : order_id, product_id, quantity
//        for (OrderSaveReqDto.OrderDto orderDto : dto.getOrderDtos()) {
//            Product product = productRepository.findById(orderDto.getProductId()).orElse(null);
//            int quantity = orderDto.getProductCount();
//            OrderDetail orderDetail = OrderDetail.builder()
//                    .product(product)
//                    .quantity(quantity)
//                    .ordering(ordering)
//                    .build();
//
//            orderDetailRepository.save(orderDetail);
//        }

    }

    public List<OrderListResDto> myOrders() {
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 이메일"));
        List<OrderListResDto> orderListResDtos = new ArrayList<>();
        List<Ordering> orderings = orderingRepository.findByMember(member);
        for (Ordering ordering : orderings) {
            orderListResDtos.add(ordering.fromEntity());
        }
        return orderListResDtos;
    }

    public void orderCancel(Long id) {
        Ordering order = orderingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("order not found"));
        if (order.getOrderStatus().equals(OrderStatus.ORDERED)) {
            order.delete();
            orderingRepository.save(order);
        }
    }
}
