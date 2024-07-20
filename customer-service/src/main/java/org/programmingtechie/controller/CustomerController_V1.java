package org.programmingtechie.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmingtechie.dto.CustomerRequest;
import org.programmingtechie.model.Customer;
import org.programmingtechie.service.CustomerService_V1;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
@Slf4j
public class CustomerController_V1
{
    final CustomerService_V1 customerService_V1;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String createCustomer(@RequestBody CustomerRequest productRequest)
    {
        customerService_V1.createProduct(productRequest);
        return "Đã thêm thông tin khách hàng mới thành công!";
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Customer getCustomerById(@PathVariable String id) {
        return customerService_V1.getCustomerById(id);
    }

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

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public String updateCustomer(@PathVariable String id, @RequestBody CustomerRequest customerRequest) {
        customerService_V1.updateCustomer(id, customerRequest);
        return "Cập nhật thông tin khách hàng thành công!";
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public String deleteCustomer(@PathVariable String id) {
        customerService_V1.deleteCustomer(id);
        return "Xóa thông tin khách hàng thành công!";
    }
}
