package com.vinsguru.payment.application.service;

import com.vinsguru.common.events.payment.PaymentStatus;
import com.vinsguru.common.util.DuplicateEventValidator;
import com.vinsguru.payment.application.entity.Customer;
import com.vinsguru.payment.application.entity.CustomerPayment;
import com.vinsguru.payment.application.mapper.EntityDtoMapper;
import com.vinsguru.payment.application.repository.CustomerRepository;
import com.vinsguru.payment.application.repository.PaymentRepository;
import com.vinsguru.payment.common.dto.PaymentDto;
import com.vinsguru.payment.common.dto.PaymentProcessRequest;
import com.vinsguru.payment.common.exception.CustomerNotFoundException;
import com.vinsguru.payment.common.exception.InSufficientBalanceException;
import com.vinsguru.payment.common.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * This class is responsible for processing payments and refunds.
 * It implements the PaymentService interface and uses the CustomerRepository and PaymentRepository
 * to interact with the database.
 * for control and handle Race Condition in Payment Processing: <a href="https://claude.ai/chat/238f7186-4c81-4f24-ac4c-f67e1f58ca90">...</a>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;
    private static final Mono<Customer> CUSTOMER_NOT_FOUND = Mono.error(new CustomerNotFoundException());
    private static final Mono<Customer> INSUFFICIENT_BALANCE = Mono.error(new InSufficientBalanceException());

    @Override
    @Transactional
    public Mono<PaymentDto> process(PaymentProcessRequest request) {
        return DuplicateEventValidator.validate(
                        paymentRepository.existsByOrderId(request.orderId()),
                        Mono.defer(() -> customerRepository.findById(request.customerId())
                                .switchIfEmpty(CUSTOMER_NOT_FOUND)
                                .filter(c -> c.getBalance() >= request.amount())
                                .switchIfEmpty(INSUFFICIENT_BALANCE)
                                .flatMap(c -> this.deductPayment(c, request)))
                )
                .doOnNext(paymentDto -> log.info("payment processed for {}", paymentDto.orderId()));
    }

    @Override
    @Transactional
    public Mono<PaymentDto> refund(UUID orderId) {
        return paymentRepository.findByOrderIdAndStatus(orderId, PaymentStatus.DEDUCTED)
                .zipWhen(p -> customerRepository.findById(p.getCustomerId()))
                .flatMap(t -> this.refundPayment(t.getT1(), t.getT2()))
                .doOnNext(paymentDto -> log.info("refund processed for {}, amount {}", paymentDto.orderId(), paymentDto.amount()));
    }

    private Mono<PaymentDto> refundPayment(CustomerPayment customerPayment, Customer customer) {
        customer.setBalance(customer.getBalance() + customerPayment.getAmount());
        customerPayment.setStatus(PaymentStatus.REFUNDED);
        return customerRepository.save(customer)
                .then(paymentRepository.save(customerPayment))
                .map(EntityDtoMapper::toDto);
    }

    private Mono<PaymentDto> deductPayment(Customer customer, PaymentProcessRequest request) {
        var customerPayment = EntityDtoMapper.toCustomerPayment(request);
        customer.setBalance(customer.getBalance() - request.amount());
        customerPayment.setStatus(PaymentStatus.DEDUCTED);
        return customerRepository.save(customer)
                .then(paymentRepository.save(customerPayment))
                .map(EntityDtoMapper::toDto)
                .doOnNext(p -> log.info("Payment processed: {}", p));

    }
}
