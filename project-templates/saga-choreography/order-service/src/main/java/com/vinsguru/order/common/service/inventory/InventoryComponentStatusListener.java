package com.vinsguru.order.common.service.inventory;

import com.vinsguru.order.common.dto.OrderInventoryDto;
import com.vinsguru.order.common.service.OrderComponentStatusListener;
import reactor.core.publisher.Mono;

public interface InventoryComponentStatusListener extends OrderComponentStatusListener<OrderInventoryDto> {

}
