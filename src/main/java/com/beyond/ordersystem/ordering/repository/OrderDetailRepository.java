package com.beyond.ordersystem.ordering.repository;

import com.beyond.ordersystem.ordering.domain.OrderDetail;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailRepository extends CrudRepository<OrderDetail, Long> {

}
