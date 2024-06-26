package jpabook.jpashop.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.QMember;
import jpabook.jpashop.domain.QOrder;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

import static jpabook.jpashop.domain.QMember.member;
import static jpabook.jpashop.domain.QOrder.order;

@Repository
//@RequiredArgsConstructor
public class OrderRepository {
    private final EntityManager em;
    private final JPAQueryFactory query;

    public OrderRepository(EntityManager em) {
        this.em = em;
        this.query = new JPAQueryFactory(em);
    }

    public void save(Order order){
        em.persist(order);
    }

    public Order findOne(Long id){
        return em.find(Order.class, id);
    }

//    public List<Order> findAll(OrderSearch orderSearch){
//        String jpql = "select o from Order o join o.member m where 1=1";
//        if(orderSearch.getOrderStatus() != null)
//            jpql += " and o.status = :status ";
//        if(StringUtils.hasText(orderSearch.getMemberName()))
//            jpql += " and m.name like :name ";
//
//        TypedQuery<Order> query = em.createQuery(jpql, Order.class);
//        if(orderSearch.getOrderStatus() != null)
//            query.setParameter("status", orderSearch.getOrderStatus());
//        if(StringUtils.hasText(orderSearch.getMemberName()))
//            query.setParameter("name", orderSearch.getMemberName());
//
//        return query
//                .setMaxResults(1000)
//                .getResultList();
//    }

    public List<Order> findAll(OrderSearch orderSearch){
//        QOrder order = QOrder.order;
//        QMember member = QMember.member;

        return query.select(order)
                .from(order)
                .join(order.member, member)
                .where(statusEq(orderSearch.getOrderStatus()), nameLike(orderSearch.getMemberName()))
                .limit(1000)
                .fetch();
    }

    private BooleanExpression nameLike(String name){
        if (!StringUtils.hasText(name))
            return null;
        return member.name.like(name);
    }

    private BooleanExpression statusEq(OrderStatus status){
        if(status == null){
            return null;
        }
        return order.status.eq(status);
    }

    // fetch join
    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                "select o" +
                        " from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        ).getResultList();
    }

    public List<Order> findAllWithItem() {
        // distinct 가 있으면 JPA에서 Order Id를 기준으로 중복 제거를 해줌
        return em.createQuery(
                "select distinct o" +
                        " from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d" +
                        " join fetch o.orderItems oi" +
                        " join fetch oi.item i", Order.class)
                .setFirstResult(1)      // 컬렉션 fetch join 과 페이징처리를 같이하면 DB에서 페이징안되고
                .setMaxResults(100)     // 조회 된 값을 모두 가져와서 메모리에서 처리함 ==> 바로 메모리 아웃 발생 가능성 올라감
                .getResultList();
    }

    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery(
                "select o" +
                        " from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
}
