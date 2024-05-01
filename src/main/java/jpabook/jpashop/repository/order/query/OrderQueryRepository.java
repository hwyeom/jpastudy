package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {
    private final EntityManager em;


    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto" +
                        "(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                        " from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    public List<OrderQueryDto> findOrders(){
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderQueryDto" +
                        "(o.id, m.name, o.orderDate, o.status, m.address)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }

    // 쿼리 1 + N 번에 가져오기
    public List<OrderQueryDto> findOrderQueryDtos() {
        List<OrderQueryDto> result = findOrders();
        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
            o.setOrderItems(orderItems);
        });
        return result;
    }

    // 쿼리 두번만에 가져오기
    public List<OrderQueryDto> findAllByDto_optimization() {
        // Order 조회 쿼리 1번
        List<OrderQueryDto> result = findOrders();

        List<Long> orderIds = result.stream().map(OrderQueryDto::getOrderId).collect(Collectors.toList());
        // OrderItems 조회 쿼리 1번
        List<OrderItemQueryDto> orderItemQueryDtos = em.createQuery(
                        "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto" +
                                "(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                                " from OrderItem oi" +
                                " join oi.item i" +
                                " where oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        // OrderId를 키로 맵으로 변경할 수 있음
        Map<Long, List<OrderItemQueryDto>> orderItemQueryDtoMap = orderItemQueryDtos.stream()
                .collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId));

        result.forEach(o -> {o.setOrderItems(orderItemQueryDtoMap.get(o.getOrderId()));});
        return result;
    }

    // 쿼리 1번에 가져오기
    public List<OrderFlatDto> findAllByDto_flat() {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderFlatDto" +
                        "(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d" +
                        " join o.orderItems oi" +
                        " join oi.item i", OrderFlatDto.class)
                .getResultList();
    }
}
