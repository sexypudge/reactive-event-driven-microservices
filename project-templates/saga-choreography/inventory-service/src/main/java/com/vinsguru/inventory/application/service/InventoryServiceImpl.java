package com.vinsguru.inventory.application.service;

import com.vinsguru.common.events.inventory.InventoryStatus;
import com.vinsguru.common.util.DuplicateEventValidator;
import com.vinsguru.inventory.application.entity.OrderInventory;
import com.vinsguru.inventory.application.entity.Product;
import com.vinsguru.inventory.application.mapper.EntityDtoMapper;
import com.vinsguru.inventory.application.repository.InventoryRepository;
import com.vinsguru.inventory.application.repository.ProductRepository;
import com.vinsguru.inventory.common.dto.InventoryDto;
import com.vinsguru.inventory.common.dto.InventoryProcessRequest;
import com.vinsguru.inventory.common.exception.OutOfStockException;
import com.vinsguru.inventory.common.exception.ProductNotFoundException;
import com.vinsguru.inventory.common.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private static final Mono<Product> PRODUCT_NOT_FOUND = Mono.error(new ProductNotFoundException());
    private static final Mono<Product> OUT_OF_STOCK = Mono.error(new OutOfStockException());


    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public Mono<InventoryDto> deduct(InventoryProcessRequest request) {
        return DuplicateEventValidator.validate(
                        this.inventoryRepository.existsByOrderId(request.orderId()),
                        this.productRepository.findById(request.productId())
                )
                .switchIfEmpty(PRODUCT_NOT_FOUND)
                .filter(p -> p.getAvailableQuantity() >= request.quantity())
                .switchIfEmpty(OUT_OF_STOCK)
                .flatMap(p -> this.deductInventory(p, request))
                .doOnNext(dto -> log.info("inventory deducted for {}", dto.orderId()));
    }

    @Override
    @Transactional
    public Mono<InventoryDto> restore(UUID orderId) {
        return this.inventoryRepository.findByOrderIdAndStatus(orderId, InventoryStatus.DEDUCTED)
                .zipWhen(i -> this.productRepository.findById(i.getProductId()))
                .flatMap(t -> this.restore(t.getT1(), t.getT2()))
                .doOnNext(dto -> log.info("restored quantity {} for {}", dto.quantity(), dto.orderId()));
    }

    private Mono<InventoryDto> deductInventory(Product product, InventoryProcessRequest request) {
        var inventory = EntityDtoMapper.toInventory(request);
        inventory.setStatus(InventoryStatus.DEDUCTED);
        product.setAvailableQuantity(product.getAvailableQuantity() - request.quantity());
        return productRepository.save(product)
                .then(inventoryRepository.save(inventory))
                .map(EntityDtoMapper::toInventoryDto)
                .doOnNext(i -> log.info("inventory processed for {}", i));
    }

    private Mono<InventoryDto> restore(OrderInventory orderInventory, Product product) {
        product.setAvailableQuantity(product.getAvailableQuantity() + orderInventory.getQuantity());
        orderInventory.setStatus(InventoryStatus.RESTORED);
        return this.productRepository.save(product)
                .then(this.inventoryRepository.save(orderInventory))
                .map(EntityDtoMapper::toInventoryDto);
    }
}
