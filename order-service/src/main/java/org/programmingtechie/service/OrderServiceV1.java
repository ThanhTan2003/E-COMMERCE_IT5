<<<<<<< HEAD
package org.programmingtechie.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.programmingtechie.dto.CustomerExistingResponse;
import org.programmingtechie.dto.OrderListDetailDto;
import org.programmingtechie.dto.OrderRequest;
import org.programmingtechie.dto.OrderResponse;
import org.programmingtechie.model.Order;
import org.programmingtechie.model.OrderDetail;
import org.programmingtechie.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderServiceV1 {

    private final WebClient.Builder webClientBuilder;
    private final OrderRepository orderRepository;

    // Kiểm tra khách hàng
    public List<CustomerExistingResponse> isCustomerExist(String customerPhone) {
        CustomerExistingResponse[] customerExistingResponses = webClientBuilder.build().get()
                .uri("http://customer-service/api/existing/phone",
                        uriBuilder -> uriBuilder.queryParam("customerPhone", customerPhone).build())
                .retrieve()
                .bodyToMono(CustomerExistingResponse[].class)
                .block();

        for (CustomerExistingResponse customerExistingResponse : customerExistingResponses) {
            if (!customerExistingResponse.getIsExisting()) {
                throw new IllegalStateException(
                        String.format("khách hàng có số điện thoại %s không tồn tại. Vui lòng kiểm tra lại!",
                                customerExistingResponse.getPhoneNumber()));
            }
        }
        return Arrays.stream(customerExistingResponses).toList();
    }

    // Đặt hàng
    public OrderResponse createOrder(OrderRequest orderRequest) {

        // Kiểm tra sản phẩm tồn tại trong kho
        List<OrderListDetailDto> orderDetailRequests = orderRequest.getOrderListDetailDto().stream().map(item -> {
            return OrderListDetailDto.builder()
                    .id(item.getId())
                    .quantity(item.getQuantity())
                    .build();
        }).toList();

        try {
            Boolean export = webClientBuilder.build().get()
                    .uri("http://inventory-service/api/v1/inventory/export-product",
                            uriBuilder -> uriBuilder.queryParam("exportProductRequest", orderDetailRequests).build())
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();

            // Sản phẩm tồn tại
            if (export) {
                Order order = new Order();
                List<OrderDetail> orderDetail = orderRequest.getOrderListDetailDto().stream()
                        .map(item -> OrderDetail.builder()
                                .id(item.getId())
                                .orderId(order.getId())
                                .productId(item.getProductId())
                                .productName(item.getProductName())
                                .price(item.getPrice())
                                .quantity(item.getQuantity())
                                .totalAmount(item.getTotalAmount())
                                .build())
                        .toList();

                Double totalAmountList = orderDetail.stream()
                        .mapToDouble(OrderDetail::getTotalAmount)
                        .sum();

                order.setTotalAmount(totalAmountList);
                order.setPhoneNumber(orderRequest.getPhoneNumber());
                order.setCustomerId(orderRequest.getCustomerId());

                order.setDiscount(orderRequest.getDiscount());
                order.setNote(orderRequest.getNote());

                Double total = orderRequest.getTotalAmount() - orderRequest.getDiscount();
                order.setTotal(total);

                order.setOrderList(orderDetail);

                List<CustomerExistingResponse> customerPhone = isCustomerExist(orderRequest.getPhoneNumber());
                if (!customerPhone.isEmpty()) {
                    orderRepository.save(order);
                } else {
                    throw new IllegalStateException("Không tìm thấy số điện thoại khách hàng phù hợp!");
                }
            } else {
                throw new IllegalArgumentException(
                        "Dịch vụ quản lý kho (inventory-service) không khả dụng. Vui lòng kiểm tra hoặc thử lại sau!");
            }
        } catch (WebClientException e) {
            log.info("ERROR - Xảy ra lỗi khi giao tiếp với inventory-service: {}", e.getMessage());

            throw new IllegalArgumentException(
                    "Dịch vụ quản lý kho (inventory-service) không khả dụng. Vui lòng kiểm tra hoặc thử lại sau!");
        } catch (Exception e) {
            log.info("ERROR - Xảy ra lỗi khi giao tiếp với inventory-service: {}", e.getMessage());

            throw new IllegalArgumentException("Có lỗi xảy ra khi kiểm tra kho. Vui lòng thử lại sau!");
        }

        return null;
    }

    public void updateOrder(String id, OrderRequest orderRequest) {
        Optional<Order> optionalOrder = orderRepository.findById(id);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setStatusHandle(orderRequest.getStatusHandle());
            order.setStatusCheckout(orderRequest.getStatusCheckout());
            order.setPaymentMethod(orderRequest.getPaymentMethod());
            orderRepository.save(order);

            log.info("Order {} is updated", order.getId());
        } else {
            log.error("Order with ID {} not found", id);
            throw new IllegalArgumentException("Order with ID " + id + " not found");
        }
    }

    public void deleteOrder(String id) {
        Optional<Order> optionalOrder = orderRepository.findById(id);
        if (optionalOrder.isPresent()) {

            orderRepository.deleteById(id);

            log.info("Order {} is deleted", id);
        } else {
            log.error("Order with ID {} not found", id);
            throw new IllegalArgumentException("Order with ID " + id + " not found");
        }
    }

    // Tìm tất cả đơn hàng và chi tiết đơn hàng có liên quan
    public List<Order> getAllOrder() {
        return orderRepository.findAll();
    }

    // Tìm một đơn hàng và chi tiết đơn hàng có liên quan
    public Order getOrderById(String id) {
        Optional<Order> optionalOrder = orderRepository.findById(id);
        return optionalOrder.get();
    }

    public List<Order> getOrderByStatusCheckout(String statusCheckout) {
        List<Order> optionalOrder = orderRepository.findByStatusCheckout(statusCheckout);
        if (optionalOrder.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy thông tin đơn hàng! ");
        }
        return optionalOrder;
    }

    public List<Order> getOrderByStatusHandle(String statusHandle) {
        List<Order> optionalOrder = orderRepository.findByStatusHandle(statusHandle);
        if (optionalOrder.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy thông tin đơn hàng! ");
        }
        return optionalOrder;
    }

    public List<Order> getOrderByPaymentMethod(String paymentMethod) {
        List<Order> optionalOrder = orderRepository.findByPaymentMethod(paymentMethod);
        if (optionalOrder.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy thông tin đơn hàng! ");
        }
        return optionalOrder;
    }

    @Transactional(readOnly = true)
    public OrderResponse isCustomerExisting(String customerId) {
        Optional<Order> optionalOrder = orderRepository.findByCustomerId(customerId);

        if (optionalOrder.isEmpty()) {
            return OrderResponse.builder()
                    .customerId(null)
                    .build();
        } else {
            Order order = optionalOrder.get();
            List<OrderListDetailDto> orderListDetailDtos = order.getOrderList().stream()
                    .map(this::convertToOrderListDetailDto)
                    .collect(Collectors.toList());
            return OrderResponse.builder()
                    .id(order.getId())
                    .customerId(order.getCustomerId())
                    .phoneNumber(order.getPhoneNumber())
                    .statusHanle(order.getStatusHandle())
                    .statusCheckout(order.getStatusCheckout())
                    .paymentMethod(order.getPaymentMethod())
                    .date(order.getDate())
                    .totalAmount(order.getTotalAmount())
                    .discount(order.getDiscount())
                    .total(order.getTotal())
                    .note(order.getNote())
                    .orderListDetailDto(orderListDetailDtos)
                    .build();
        }
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> isCustomerExisting(List<String> customerId) {
        List<Order> orders = orderRepository.findAllById(customerId);

        List<OrderResponse> orderResponses = new ArrayList<>();

        for (Order order : orders) {
            if (order == null) {
                OrderResponse orderResponse = OrderResponse.builder()
                        .customerId(null).build();
                orderResponses.add(orderResponse);
            } else {
                List<OrderListDetailDto> orderListDetailDtos = order.getOrderList().stream()
                        .map(this::convertToOrderListDetailDto)
                        .collect(Collectors.toList());
                OrderResponse orderResponse = OrderResponse.builder()
                        .id(order.getId())
                        .customerId(order.getCustomerId())
                        .phoneNumber(order.getPhoneNumber())
                        .statusHanle(order.getStatusHandle())
                        .statusCheckout(order.getStatusCheckout())
                        .paymentMethod(order.getPaymentMethod())
                        .date(order.getDate())
                        .totalAmount(order.getTotalAmount())
                        .discount(order.getDiscount())
                        .total(order.getTotal())
                        .note(order.getNote())
                        .orderListDetailDto(orderListDetailDtos)
                        .build();
                orderResponses.add(orderResponse);
            }

        }
        return orderResponses;
    }

    private OrderListDetailDto convertToOrderListDetailDto(OrderDetail orderDetail) {
        OrderListDetailDto orderListDetailDto = new OrderListDetailDto();
        orderListDetailDto.setId(orderDetail.getId());
        orderListDetailDto.setOrderId(orderDetail.getOrderId());
        orderListDetailDto.setProductId(orderDetail.getProductId());
        orderListDetailDto.setProductName(orderDetail.getProductName());
        orderListDetailDto.setPrice(orderDetail.getPrice());
        orderListDetailDto.setQuantity(orderDetail.getQuantity());
        orderListDetailDto.setTotalAmount(orderDetail.getTotalAmount());
        return orderListDetailDto;
    }
}
=======
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
>>>>>>> 6d16af63fcfbd17bd0fee3fa7e3369f9c20f6cfc
