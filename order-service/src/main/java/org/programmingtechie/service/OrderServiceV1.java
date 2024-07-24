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
