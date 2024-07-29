package org.programmingtechie.service;

import java.rmi.AlreadyBoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.programmingtechie.dto.request.ExportProductRequest;
import org.programmingtechie.dto.request.OrderListDetailDto;
import org.programmingtechie.dto.request.OrderRequest;
import org.programmingtechie.dto.response.CustomerExistingResponse;
import org.programmingtechie.dto.response.OrderDetailResponse;
import org.programmingtechie.dto.response.OrderResponse;
import org.programmingtechie.dto.response.ProductExistingResponse;
import org.programmingtechie.model.Order;
import org.programmingtechie.model.OrderDetail;
import org.programmingtechie.repository.OrderDetailRepository;
import org.programmingtechie.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderServiceV1 {

    private final WebClient.Builder webClientBuilder;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    private String extractMessageFromResponse(String responseBody, String serviceName) {
        // Tìm chuỗi lỗi trong response
        try {
            int startIndex = responseBody.indexOf("\"message\":\"") + 11;
            int endIndex = responseBody.indexOf("\"", startIndex);
            return responseBody.substring(startIndex, endIndex);
        } catch (Exception e) {
            log.error("ERROR - Không thể trích xuất thông báo lỗi từ phản hồi: {}", responseBody, e);
            return "Dịch vụ " + serviceName + " không khả dụng. Vui lòng kiểm tra và thử lại.";
        }
    }

    // Kiểm tra thông tin khách hàng theo số điện thoại
    public CustomerExistingResponse isCustomerExist(String customerPhone) {
        if (customerPhone == null || customerPhone.trim().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại khách hàng không được để trống");
        }

        try {
            CustomerExistingResponse customerExistingResponse = webClientBuilder.build().get()
                    .uri("http://customer-service/api/v1/customer/existing/phone",
                            uriBuilder -> uriBuilder.queryParam("customerPhone", customerPhone).build())
                    .retrieve()
                    .bodyToMono(CustomerExistingResponse.class)
                    .block();

            if (customerExistingResponse == null || !customerExistingResponse.getIsExisting()) {
                throw new IllegalStateException(
                        String.format("khách hàng có số điện thoại %s không tồn tại. Vui lòng kiểm tra lại!",
                                customerPhone));
            }

            return customerExistingResponse;
        }
        catch (WebClientResponseException e) {
            String errorMessage = extractMessageFromResponse(e.getResponseBodyAsString(), "quản lý khách hàng (customer-service)");
            log.error("ERROR - Xảy ra lỗi khi giao tiếp với customer-service: Status code - {}, Body - {}", e.getStatusCode(), errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        catch (Exception e) {
            log.error("ERROR: Dịch vụ quản lý khách hàng (customer-service) không khả dụng. Vui lòng kiểm tra và thử lại. " + e.getMessage());
            throw new IllegalStateException("Dịch vụ quản lý khách hàng (customer-service) không khả dụng. Vui lòng kiểm tra và thử lại. ");
        }
    }

    // Kiểm tra các sản phẩm đặt hàng có trong product-service không?
    List<ProductExistingResponse> checkProductExisting(List<String> productIds)
    {
        ProductExistingResponse[] productResponses;
        try
        {
            productResponses = webClientBuilder.build().get()
                    .uri("http://product-service/api/v1/product/is-existing",
                            uriBuilder -> uriBuilder.queryParam("list_product_id",productIds).build())
                    .retrieve()
                    .bodyToMono(ProductExistingResponse[].class)
                    .block();
        }
        catch (WebClientResponseException e) {
            String errorMessage = extractMessageFromResponse(e.getResponseBodyAsString(), "quản lý sản phẩm (product-service)");
            log.error("ERROR - Xảy ra lỗi khi giao tiếp với product-service: Status code - {}, Body - {}", e.getStatusCode(), errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        catch (Exception e) {
            log.error("ERROR: Dịch vụ quản lý sản phẩm (product-service) không khả dụng. Vui lòng kiểm tra và thử lại. " + e.getMessage());
            throw new IllegalStateException("Dịch vụ quản lý sản phẩm (product-service) không khả dụng. Vui lòng kiểm tra và thử lại.");
        }

        if (productResponses == null) {
            throw new IllegalStateException("Không thể kiểm tra sản phẩm. Vui lòng thử lại sau.");
        }

        int index = 1;
        for (ProductExistingResponse productResponse : productResponses) {
            if (!productResponse.getIsExisting()) {
                throw new IllegalStateException(
                        String.format("Sản phẩm thứ %d có mã id %s không tồn tại. Vui lòng kiểm tra lại!", index, productResponse.getId()));
            }
            index++;
        }
        return Arrays.asList(productResponses);
    }

    // Gửi yêu cầu xuất kho đến inventory-service
    void exportProductRequest(List<ExportProductRequest> exportProductRequests)
    {
        try {
            if(exportProductRequests.isEmpty())
                throw new AlreadyBoundException("Vui lòng nhập thông tin sản phẩm!");
            Boolean export = webClientBuilder.build().post()
                    .uri("http://inventory-service/api/v1/inventory/export-product")
                    .bodyValue(exportProductRequests)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
        }
        catch (WebClientResponseException e) {
            String errorMessage = extractMessageFromResponse(e.getResponseBodyAsString(), "quản lý kho (inventory-service)");
            log.error("ERROR - Xảy ra lỗi khi giao tiếp với inventory-service: Status code - {}, Body - {}", e.getStatusCode(), errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        catch (Exception e) {
            log.error("ERROR: Dịch vụ quản lý kho (inventory-service) không khả dụng. Vui lòng kiểm tra và thử lại. " + e.getMessage());
            throw new IllegalStateException("Dịch vụ quản lý kho (inventory-service) không khả dụng. Vui lòng kiểm tra và thử lại.");
        }
    }

    // Đặt hàng
    public OrderResponse createOrder(OrderRequest orderRequest) {
        if (orderRequest.getOrderListDetailDto() == null) {
            orderRequest.setOrderListDetailDto(new ArrayList<>());
        }

        // Lấy danh sách mã Id sản phẩm đặt hàng
        List<String> productIds = orderRequest.getOrderListDetailDto().stream().map(
                OrderListDetailDto::getProductId
        ).toList();

        // Kiểm tra các sản phẩm đặt hàng có trong product-service không?
        List<ProductExistingResponse> productExistingResponses = checkProductExisting(productIds);

        //
        for(int i = 0; i < orderRequest.getOrderListDetailDto().size(); i++)
            orderRequest.getOrderListDetailDto().get(i).setProductName(productExistingResponses.get(i).getName());

        // Kiểm tra số điện thoại khách hàng có trong customer-service không?
        CustomerExistingResponse customerExistingResponse = isCustomerExist(orderRequest.getPhoneNumber());
        orderRequest.setCustomerId(customerExistingResponse.getId());

        // Lấy danh sách sản phẩm đặt hàng
        List<ExportProductRequest> exportProductRequests = orderRequest.getOrderListDetailDto().stream().map(item -> {
            return ExportProductRequest.builder()
                    .productId(item.getProductId())
                    .productName(item.getProductName())
                    .quantity(item.getQuantity())
                    .build();
        }).toList();

        // Yêu cầu inventory - service kiểm tra thông tin đặt hàng và xuất kho
        exportProductRequest(exportProductRequests);

        //
        int index = 0;
        orderRequest.setTotalAmount(0.0);
        orderRequest.setTotal(0.0);
        for (OrderListDetailDto orderListDetailDto : orderRequest.getOrderListDetailDto()) {
            Double price = productExistingResponses.get(index).getPrice();
            orderListDetailDto.setPrice(price);
            Integer quantity = orderListDetailDto.getQuantity();
            Double totalAmount_temp = price * quantity;
            orderListDetailDto.setTotalAmount(totalAmount_temp);
            orderRequest.setTotalAmount(orderRequest.getTotalAmount() + totalAmount_temp);
            orderListDetailDto.setProductName(productExistingResponses.get(index).getName());
            index++;
        }
        orderRequest.setTotal(orderRequest.getTotalAmount() - orderRequest.getDiscount());

        //Tạo hóa đơn
        Order order = Order.builder()
                .customerId(orderRequest.getCustomerId())
                .phoneNumber(orderRequest.getPhoneNumber())
                .statusCheckout(orderRequest.getStatusCheckout())
                .statusHandle(orderRequest.getStatusHandle())
                .paymentMethod(orderRequest.getPaymentMethod())
                .totalAmount(orderRequest.getTotalAmount())
                .discount(orderRequest.getDiscount())
                .total(orderRequest.getTotal())
                .note(orderRequest.getNote())
                .build();
        

        //Tạo chi tiết hóa đơn
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (OrderListDetailDto orderListDetailDto : orderRequest.getOrderListDetailDto()) {
            OrderDetail orderDetail = OrderDetail.builder()
                    .orderId(order.getId())
                    .productId(orderListDetailDto.getProductId())
                    .productName(orderListDetailDto.getProductName())
                    .price(orderListDetailDto.getPrice())
                    .quantity(orderListDetailDto.getQuantity())
                    .totalAmount(orderListDetailDto.getTotalAmount())
                    .build();
            orderDetails.add(orderDetail);
           // orderDetailRepository.save(orderDetail);
        }
        order.setOrderList(orderDetails);
        orderRepository.save(order);

        //Lấy danh sách chi tiết đơn hàng
        List<OrderDetailResponse> orderDetailResponses = new ArrayList<>();
        for(OrderDetail orderDetail : orderDetails)
        {
            OrderDetailResponse orderDetailResponse = OrderDetailResponse.builder()
                    .id(orderDetail.getId())
                    .productId(orderDetail.getProductId())
                    .productName(orderDetail.getProductName())
                    .quantity(orderDetail.getQuantity())
                    .price(orderDetail.getPrice())
                    .totalAmount(orderDetail.getTotalAmount())
                    .build();

            orderDetailResponses.add(orderDetailResponse);
        }

        //Hiển thị chi tiết đơn hàng sau khi đặt xong
        OrderResponse orderResponse = OrderResponse.builder()
                .id(order.getId())
                .customerId(customerExistingResponse.getId())
                .customerName(customerExistingResponse.getFullName())
                .phoneNumber(customerExistingResponse.getPhoneNumber())
                .statusHanle(order.getStatusHandle())
                .statusCheckout(order.getStatusCheckout())
                .paymentMethod(order.getPaymentMethod())
                .totalAmount(order.getTotalAmount())
                .discount(order.getDiscount())
                .total(order.getTotal())
                .date(order.getDate())
                .note(order.getNote())
                .orderDetailResponses(orderDetailResponses)
                .build();

        return orderResponse;
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
//        Optional<Order> optionalOrder = orderRepository.findByCustomerId(customerId);
//
//        if (optionalOrder.isEmpty()) {
//            return OrderResponse.builder()
//                    .customerId(null)
//                    .build();
//        } else {
//            Order order = optionalOrder.get();
//            List<OrderDetailResponse> orderListDetailDtos = order.getOrderList().stream()
//                    .map(this::convertToOrderListDetailDto)
//                    .collect(Collectors.toList());
//            return OrderResponse.builder()
//                    .id(order.getId())
//                    .customerId(order.getCustomerId())
//                    .phoneNumber(order.getPhoneNumber())
//                    .statusHanle(order.getStatusHandle())
//                    .statusCheckout(order.getStatusCheckout())
//                    .paymentMethod(order.getPaymentMethod())
//                    .date(order.getDate())
//                    .totalAmount(BigDecimal.valueOf(order.getTotalAmount()))
//                    .discount(BigDecimal.valueOf(order.getDiscount()))
//                    .total(BigDecimal.valueOf(order.getTotal()))
//                    .note(order.getNote())
//                    .orderDetailResponses(orderListDetailDtos)
//                    .build();
//        }
        return null;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> isCustomerExisting(List<String> customerId) {
//        List<Order> orders = orderRepository.findAllById(customerId);
//
//        List<OrderResponse> orderResponses = new ArrayList<>();
//
//        for (Order order : orders) {
//            if (order == null) {
//                OrderResponse orderResponse = OrderResponse.builder()
//                        .customerId(null).build();
//                orderResponses.add(orderResponse);
//            } else {
//                List<OrderListDetailDto> orderListDetailDtos = order.getOrderList().stream()
//                        .map(this::convertToOrderListDetailDto)
//                        .collect(Collectors.toList());
//                OrderResponse orderResponse = OrderResponse.builder()
//                        .id(order.getId())
//                        .customerId(order.getCustomerId())
//                        .phoneNumber(order.getPhoneNumber())
//                        .statusHanle(order.getStatusHandle())
//                        .statusCheckout(order.getStatusCheckout())
//                        .paymentMethod(order.getPaymentMethod())
//                        .date(order.getDate())
//                        .totalAmount(BigDecimal.valueOf(order.getTotalAmount()))
//                        .discount(BigDecimal.valueOf(order.getDiscount()))
//                        .total(BigDecimal.valueOf(order.getTotal()))
//                        .note(order.getNote())
//                        .orderDetailResponses(orderListDetailDtos)
//                        .build();
//                orderResponses.add(orderResponse);
//            }
//
//        }
//        return orderResponses;

        return null;
    }

    public List<OrderResponse> getOrderByCustomerId(String customerId)
    {
        List<Order> orders = orderRepository.findByCustomerId(customerId);

        List<OrderResponse> orderResponses = new ArrayList<>();
        for(int i = 0; i < orders.size(); i++)
        {
            OrderResponse orderResponse = OrderResponse.builder()
                    .id(orders.get(i).getId())
                    .customerId(orders.get(i).getCustomerId())
                    .customerName(orders.get(i).getCustomerName())
                    .phoneNumber(orders.get(i).getPhoneNumber())
                    .statusHanle(orders.get(i).getStatusHandle())
                    .statusCheckout(orders.get(i).getStatusCheckout())
                    .paymentMethod(orders.get(i).getPaymentMethod())
                    .date(orders.get(i).getDate())
                    .note(orders.get(i).getNote())
                    .totalAmount(orders.get(i).getTotalAmount())
                    .discount(orders.get(i).getDiscount())
                    .total(orders.get(i).getTotal())
                    .build();
            orderResponses.add(orderResponse);
        }
        return  orderResponses;
    }

    public List<OrderResponse> getOrderByCustomerPhoneNumber(String customerPhoneNumber)
    {
        List<Order> orders = orderRepository.findByPhoneNumber(customerPhoneNumber);

        List<OrderResponse> orderResponses = new ArrayList<>();
        for(int i = 0; i < orders.size(); i++)
        {
            OrderResponse orderResponse = OrderResponse.builder()
                    .id(orders.get(i).getId())
                    .customerId(orders.get(i).getCustomerId())
                    .customerName(orders.get(i).getCustomerName())
                    .phoneNumber(orders.get(i).getPhoneNumber())
                    .statusHanle(orders.get(i).getStatusHandle())
                    .statusCheckout(orders.get(i).getStatusCheckout())
                    .paymentMethod(orders.get(i).getPaymentMethod())
                    .date(orders.get(i).getDate())
                    .note(orders.get(i).getNote())
                    .totalAmount(orders.get(i).getTotalAmount())
                    .discount(orders.get(i).getDiscount())
                    .total(orders.get(i).getTotal())
                    .build();
            orderResponses.add(orderResponse);
        }
        return  orderResponses;
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
