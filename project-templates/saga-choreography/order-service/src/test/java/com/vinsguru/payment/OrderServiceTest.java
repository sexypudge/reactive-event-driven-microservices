package com.vinsguru.payment;

import com.vinsguru.common.events.inventory.InventoryEvent;
import com.vinsguru.common.events.inventory.InventoryStatus;
import com.vinsguru.common.events.order.OrderStatus;
import com.vinsguru.common.events.payment.PaymentEvent;
import com.vinsguru.common.events.payment.PaymentStatus;
import com.vinsguru.common.events.shipping.ShippingEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Instant;

public class OrderServiceTest extends AbstractIntegrationTest {

//    @RepeatedTest(10)
    @Test
    public void orderCompletedWorkflowTest() throws InterruptedException {
        var request = TestDataUtil.toRequest(1, 1, 2, 3);
        var orderId = this.initiateOrder(request);
        // Lúc này table PurchaseOrder có record với status là PENDING

        this.verifyOrderCreatedEvent(orderId, 6);

        // mô phỏng payment-service gửi 1 event vào payment-events và order-service sẽ lắng nghe và xử lý event này (insert vào db table OrderPayment)
        this.emitEvent(PaymentEvent.Deducted.builder().orderId(orderId).build());

        // mô phỏng inventory-service gửi 1 event đc gửi vào inventory-events và order-service sẽ lắng nghe và xử lý event này (insert vào db table OrderInventory)
        this.emitEvent(InventoryEvent.Deducted.builder().orderId(orderId).build());

        // Lúc này order-service nhận đc 2 event payment-events hoặc inventory-events thì table OrderPayment và table OrderInventory có record tương ứng với orderId và status là Deducted
        // Tiếp đến OrderFulfillmentService sẽ update record tương ứng ở table PurchaseOrder thành COMPLETED
        this.verifyOrderCompletedEvent(orderId);


        this.emitEvent(ShippingEvent.Scheduled.builder().orderId(orderId).expectedDelivery(Instant.now()).build());

        this.expectNoEvent();

        this.verifyOrderDetails(orderId, r -> {
            Assertions.assertNotNull(r.order().deliveryDate());
            Assertions.assertEquals(OrderStatus.COMPLETED, r.order().status());
            Assertions.assertEquals(PaymentStatus.DEDUCTED, r.payment().status());
            Assertions.assertEquals(InventoryStatus.DEDUCTED, r.inventory().status());
        });
    }


    @Test
    public void orderCancelledWhenInventoryDeclinedTest() throws InterruptedException {
        // order create request
        var request = TestDataUtil.toRequest(1, 1, 2, 3);

        // validate order in pending state
        var orderId = this.initiateOrder(request);

        // check for order created event
        this.verifyOrderCreatedEvent(orderId, 6);

        // emit payment deducted event
        this.emitEvent(PaymentEvent.Deducted.builder().orderId(orderId).build());

        // emit inventory declined event
        this.emitEvent(InventoryEvent.Declined.builder().orderId(orderId).build());

        // check for order cancelled event
        this.verifyOrderCancelledEvent(orderId);

        // emit shipping scheduled event
        this.emitEvent(ShippingEvent.Scheduled.builder().orderId(orderId).expectedDelivery(Instant.now()).build());

        // expect no event
        Thread.sleep(2500);

        this.verifyOrderDetails(orderId, r -> {
            Assertions.assertNull(r.order().deliveryDate());
            Assertions.assertEquals(OrderStatus.CANCELLED, r.order().status());
            Assertions.assertEquals(PaymentStatus.DEDUCTED, r.payment().status());
            Assertions.assertEquals(InventoryStatus.DECLINED, r.inventory().status());
        });

    }

    @Test
    public void verifyCompensatingTransaction() throws InterruptedException {
        // order create request
        var request = TestDataUtil.toRequest(1, 1, 2, 3);

        // validate order in pending state
        var orderId = this.initiateOrder(request);

        // check for order created event
        this.verifyOrderCreatedEvent(orderId, 6);

        // emit payment declined event
        this.emitEvent(PaymentEvent.Declined.builder().orderId(orderId).build());

        // emit inventory deducted event
        this.emitEvent(InventoryEvent.Deducted.builder().orderId(orderId).build());

        // check for order cancelled event
        this.verifyOrderCancelledEvent(orderId);

        // emit inventory restored event
        this.emitEvent(InventoryEvent.Restored.builder().orderId(orderId).build());

        // expect no event
        this.expectNoEvent();

        this.verifyOrderDetails(orderId, r -> {
            Assertions.assertNull(r.order().deliveryDate());
            Assertions.assertEquals(OrderStatus.CANCELLED, r.order().status());
            Assertions.assertEquals(PaymentStatus.DECLINED, r.payment().status());
            Assertions.assertEquals(InventoryStatus.RESTORED, r.inventory().status());
        });

    }


    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void getAllOrdersTest(){
        // order create request
        var request = TestDataUtil.toRequest(1, 1, 2, 3);

        // validate order in pending state
        var orderId1 = this.initiateOrder(request);

        // check for order created event
        this.verifyOrderCreatedEvent(orderId1, 6);

        // verify if GET all orders API returns one item in the response
        this.verifyAllOrders(orderId1);

        // validate order in pending state
        var orderId2 = this.initiateOrder(request);

        // check for order created event
        this.verifyOrderCreatedEvent(orderId2, 6);

        // verify if GET all orders API returns 2 items in the response
        this.verifyAllOrders(orderId1, orderId2);

    }

}
