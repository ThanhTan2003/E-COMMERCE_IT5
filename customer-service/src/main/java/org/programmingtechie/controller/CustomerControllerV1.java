package org.programmingtechie.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmingtechie.dto.request.CustomerRequest;
import org.programmingtechie.dto.response.CustomerExistingResponse;
import org.programmingtechie.dto.response.CustomerOrderList;
import org.programmingtechie.dto.response.CustomerResponse;
import org.programmingtechie.model.Customer;
import org.programmingtechie.service.CustomerServiceV1;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
@Slf4j
public class CustomerControllerV1
{
    final CustomerServiceV1 customerService_V1;

    // Tạo khách hàng mới
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String createCustomer(@RequestBody CustomerRequest productRequest)
    {
        customerService_V1.createCustomer(productRequest);
        return "Đã thêm thông tin khách hàng mới thành công!";
    }

    // Lấy thông tin khách hàng theo id trong URL
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Customer getCustomerById(@PathVariable String id) {
        return customerService_V1.getCustomerById(id);
    }

    //Lấy thông tin khách hàng theo id trong body
    @PostMapping("/id")
    @ResponseStatus(HttpStatus.OK)
    public Customer getCustomerById_1(@RequestBody String id) {
        return customerService_V1.getCustomerById(id);
    }


    @PostMapping("/phone")
    @ResponseStatus(HttpStatus.OK)
    public Customer getCustomerByPhoneNumber(@RequestBody String phoneNumber) {
        return customerService_V1.getCustomerByPhoneNumber(phoneNumber);
    }

    // Nhận yêu cầu kiểm tra khách hàng có tồn tại không theo phoneNumber
    @GetMapping("/existing/phone")
    @ResponseStatus(HttpStatus.OK)
    public CustomerExistingResponse isExisting(@RequestParam String customerPhone) {
        return customerService_V1.isExisting(customerPhone);
    }


    @PostMapping("/email")
    @ResponseStatus(HttpStatus.OK)
    public Customer getCustomerByEmail(@RequestBody String email) {
        return customerService_V1.getCustomerByEmail(email);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Customer> getAllCustomers() {
        return customerService_V1.getAllCustomers();
    }

    @PutMapping("update/{id}")
    @ResponseStatus(HttpStatus.OK)
    public String updateCustomer(@PathVariable String id, @RequestBody CustomerRequest customerRequest) {
        customerService_V1.updateCustomer(id, customerRequest);
        return "Cập nhật thông tin khách hàng thành công!";
    }

    @DeleteMapping("delete/{id}")
    @ResponseStatus(HttpStatus.OK)
    public String deleteCustomer(@PathVariable String id) {
        customerService_V1.deleteCustomer(id);
        return "Xóa thông tin khách hàng thành công!";
    }

    @GetMapping("/orders/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CustomerOrderList getOrderListById(@PathVariable String id)
    {
        return customerService_V1.getOrderListById(id);
    }
    @GetMapping("/orders/phone/{phoneNumber}")
    @ResponseStatus(HttpStatus.OK)
    public CustomerOrderList getOrderListByPhoneNumber(@PathVariable String phoneNumber)
    {
        return customerService_V1.getOrderListByPhoneNumber(phoneNumber);
    }
}
