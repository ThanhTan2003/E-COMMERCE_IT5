//package org.programmingtechie.service;
//
//import java.math.BigDecimal;
//import java.util.stream.Collectors;
//
//import org.programmingtechie.dto.OrderRequest;
//
//public class OrderServiceV1 {
//
//    public OrderResponse placeOrder(OrderRequest orderRequest) {
//        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtos()
//                .stream()
//                .map(orderLineItemsDto -> OrderLineItems.builder()
//                        .skuCode(orderLineItemsDto.getSkuCode())
//                        .quantity(orderLineItemsDto.getQuantity())
//                        .note(orderLineItemsDto.getNote())
//                        .build())
//                .toList();
//
//        List<String> skuCodes = orderLineItems.stream()
//                .map(OrderLineItems::getSkuCode)
//                .toList();
//
//        InventoryResponse[] inventoryResponseArray;
//
//        try {
//            inventoryResponseArray = webClientBuilder.build().get()
//                    .uri("http://inventory-service/api/inventory/is_in_stock",
//                            uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
//                    .retrieve()
//                    .bodyToMono(InventoryResponse[].class)
//                    .block();
//        } catch (Exception e) {
//            throw new IllegalStateException("Dịch vụ kiểm tra kho hàng không khả dụng. Vui lòng thử lại sau.");
//        }
//
//        if (inventoryResponseArray == null) {
//            throw new IllegalArgumentException("Không thể lấy thông tin kho hàng.");
//        }
//
//        for (OrderLineItems item : orderLineItems) {
//            InventoryResponse response = Arrays.stream(inventoryResponseArray)
//                    .filter(r -> r.getSkuCode().equals(item.getSkuCode()))
//                    .findFirst()
//                    .orElseThrow(() -> new IllegalArgumentException(
//                            String.format("Không tìm thấy sản phẩm có skuCode %s trong kho!", item.getSkuCode())));
//
//            if (!response.isInStock() || response.getQuantity() < item.getQuantity()) {
//                throw new IllegalArgumentException(
//                        "Sản phẩm có mã skuCode " + item.getSkuCode() + " không đủ số lượng tồn kho.");
//            }
//        }
//
//        BigDecimal totalAmount = BigDecimal.valueOf(0);
//
//        List<ProductResponse> productResponses = isProductExist(skuCodes);
//
//        Integer index = 0;
//
//        for (OrderLineItems orderLineItems1 : orderLineItems) {
//            BigDecimal unitPrice = productResponses.get(index).getPrice();
//            Integer quantity = orderLineItems1.getQuantity();
//            BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(quantity));
//            orderLineItems1.setUnitPrice(unitPrice);
//            orderLineItems1.setTotalAmount(total);
//
//            totalAmount = totalAmount.add(total);
//        }
//
//        BigDecimal discount = orderRequest.getDiscount();
//        if (discount == null) {
//            discount = BigDecimal.valueOf(0);
//        }
//
//        BigDecimal total = totalAmount.subtract(discount);
//
//        Order order = Order.builder()
//                .orderLineItems(orderLineItems)
//                .customerId(orderRequest.getCustomer_id())
//                .totalAmount(totalAmount)
//                .discount(discount)
//                .total(total)
//                .status(orderRequest.getStatus())
//                .note(orderRequest.getNote())
//                .build();
//
//        orderRepository.save(order);
//
//        updateInventory(orderLineItems);
//
//        OrderResponse orderResponse = OrderResponse.builder()
//                .id(order.getId())
//                .orderLineItemsDtos(order.getOrderLineItems().stream()
//                        .map(orderLineItem -> OrderLineItemsDto.builder()
//                                .id(orderLineItem.getId())
//                                .skuCode(orderLineItem.getSkuCode())
//                                .customer_id(orderLineItem.getCustomer_id())
//                                .unitPrice(orderLineItem.getUnitPrice())
//                                .quantity(orderLineItem.getQuantity())
//                                .totalAmount(orderLineItem.getTotalAmount())
//                                .note(orderLineItem.getNote())
//                                .build())
//                        .collect(Collectors.toList()))
//                .customer_id(order.getCustomerId())
//                .totalAmount(order.getTotalAmount())
//                .discount(order.getDiscount())
//                .total(order.getTotal())
//                .status(order.getStatus())
//                .note(order.getNote())
//                .build();
//        return orderResponse;
//    }
//}
