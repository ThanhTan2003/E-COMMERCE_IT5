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
import org.programmingtechie.dto.ProductResponse;
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
    private List<OrderDetail> orderDetail;

    // Kiểm tra khách hàng
    public List<CustomerExistingResponse> isCustomerExist(String customerPhone) {
        CustomerExistingResponse[] customerExistingResponses = webClientBuilder.build().get()
                .uri("http://customer-service/api/v1/customer/existing/phone",
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

    public List<ProductResponse> checkProductExisting(List<String> productIds) {
        try {
            ProductResponse[] productResponses = webClientBuilder.build().get()
                    .uri("http://product-service/api/v1/product/is-existing",
                            uriBuilder -> uriBuilder.queryParam("list_product_id", productIds).build())
                    .retrieve()
                    .bodyToMono(ProductResponse[].class)
                    .block();

            if (productResponses == null) {
                throw new IllegalStateException("Không thể kiểm tra sản phẩm. Vui lòng thử lại sau.");
            }

            for (ProductResponse productResponse : productResponses) {
                if (!productResponse.getIsExisting()) {
                    throw new IllegalStateException(
                            String.format("Sản phẩm có mã id %s không tồn tại. Vui lòng kiểm tra lại!",
                                    productResponse.getId()));
                }
            }
            return Arrays.stream(productResponses).toList();
        } catch (WebClientException e) {
            log.info("ERROR - Xảy ra lỗi khi giao tiếp với product-service: {}", e.getMessage());

            throw new IllegalArgumentException(
                    "Dịch vụ quản lý sản phẩm (product-service) không khả dụng. Vui lòng kiểm tra hoặc thử lại sau!");
        } catch (Exception e) {
            log.info("ERROR - Xảy ra lỗi khi giao tiếp với product-service: {}", e.getMessage());

            throw new IllegalArgumentException("Có lỗi xảy ra khi kiểm tra sản phẩm. Vui lòng thử lại sau!");
        }
    }

    // Đặt hàng
    public OrderResponse createOrder(OrderRequest orderRequest) {
        Order order = new Order();

        List<CustomerExistingResponse> customerExistingResponses = isCustomerExist(orderRequest.getPhoneNumber());
        List<ProductResponse> productResponses = new ArrayList<>();
       
        if (customerExistingResponses.isEmpty()) {
            throw new IllegalStateException("Không tìm thấy số điện thoại khách hàng phù hợp!");
        } else {
            for (CustomerExistingResponse customerExistingResponse : customerExistingResponses) {
                order.setPhoneNumber(orderRequest.getPhoneNumber());
                order.setCustomerId(customerExistingResponse.getId());

                order.setDiscount(orderRequest.getDiscount());
                order.setNote(orderRequest.getNote());

                for (ProductResponse productResponse : productResponses) {
                    orderDetail = orderRequest.getOrderListDetailDto().stream()
                            .map(item -> OrderDetail.builder()
                                    .orderId(order.getId())
                                    .productId(item.getProductId())
                                    .price(productResponse.getPrice())
                                    .quantity(item.getQuantity())
                                    .totalAmount(productResponse.getPrice() * item.getQuantity())
                                    .build())
                            .toList();
                }
                Double totalAmountList = orderDetail.stream()
                        .mapToDouble(OrderDetail::getTotalAmount)
                        .sum();
                order.setTotalAmount(totalAmountList);

                Double total = orderRequest.getTotalAmount() - orderRequest.getDiscount();
                order.setTotal(total);

                order.setOrderList(orderDetail);
                List<String> productId = order.getOrderList()
                        .stream()
                        .map(OrderDetail::getProductId)
                        .toList();
                productResponses = checkProductExisting(productId);
                if (productResponses.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Dịch vụ danh mục sản phẩm không khả dụng. Vui lòng kiểm tra hoặc thử lại sau!");
                } else {
                    orderDetail = orderRequest.getOrderListDetailDto().stream()
                            .map(item -> {
                                return OrderDetail.builder()
                                        .productId(item.getProductId())
                                        .quantity(item.getQuantity())
                                        .build();
                            }).toList();
                    try {
                        // Kiểm tra sản phẩm tồn tại trong kho
                        Boolean export = webClientBuilder.build().get()
                                .uri("http://inventory-service/api/v1/inventory/export-product",
                                        uriBuilder -> uriBuilder.queryParam("exportProductRequest", orderDetail)
                                                .build())
                                .retrieve()
                                .bodyToMono(Boolean.class)
                                .block();

                        // Xuất kho không thành công
                        if (!export) {
                            throw new IllegalArgumentException(
                                    "Dịch vụ quản lý kho (inventory-service) không khả dụng. Vui lòng kiểm tra hoặc thử lại sau!");
                        } else {
                            orderRepository.save(order);
                        }
                    } catch (WebClientException e) {
                        log.info("ERROR - Xảy ra lỗi khi giao tiếp với inventory-service: {}", e.getMessage());

                        throw new IllegalArgumentException(
                                "Dịch vụ quản lý kho (inventory-service) không khả dụng. Vui lòng kiểm tra hoặc thử lại sau!");
                    } catch (Exception e) {
                        log.info("ERROR - Xảy ra lỗi khi giao tiếp với inventory-service: {}", e.getMessage());

                        throw new IllegalArgumentException("Có lỗi xảy ra khi kiểm tra kho. Vui lòng thử lại sau!");
                    }
                }
            }
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
