package org.programmingtechie.controller;

import java.util.List;

import org.programmingtechie.dto.request.OrderRequest;
import org.programmingtechie.dto.response.OrderResponse;
import org.programmingtechie.model.Order;
import org.programmingtechie.service.OrderServiceV1;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import lombok.*;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderControllerV1 {

    private final OrderServiceV1 orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@RequestBody OrderRequest orderRequest) {
        return orderService.createOrder(orderRequest);
    }

    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.OK)
    public String updateOrder(@PathVariable String id, @RequestBody OrderRequest orderRequest) {
        orderService.updateOrder(id, orderRequest);
        return String.format("Order %s is updated", id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public String deleteOrder(@PathVariable String id) {
        orderService.deleteOrder(id);
        return "Xóa thông tin đơn hàng thành công!";
    }

    @PostMapping("/statusHandle")
    @ResponseStatus(HttpStatus.OK)
    public List<Order> getOrderByStatusBusiness(@RequestBody String statusHandle) {
        return orderService.getOrderByStatusHandle(statusHandle);
    }

    @PostMapping("/statusCheckout")
    @ResponseStatus(HttpStatus.OK)
    public List<Order> getOrderByStatusCheckout(@RequestBody String statusCheckout) {
        return orderService.getOrderByStatusCheckout(statusCheckout);
    }

    @PostMapping("/paymentMethod")
    @ResponseStatus(HttpStatus.OK)
    public List<Order> getOrderByPaymentMethod(@RequestBody String paymentMethod) {
        return orderService.getOrderByPaymentMethod(paymentMethod);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Order> getAllOrder() {
        return orderService.getAllOrder();
    }

    @GetMapping("/is-customer-existing")
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponse> isExisting(@RequestParam List<String> list_product_id) {
        return orderService.isCustomerExisting(list_product_id);
    }

    @GetMapping("/is-customer-existing/single")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponse isExisting(@RequestParam String list_product_id) {
        return orderService.isCustomerExisting(list_product_id);
    }

}
