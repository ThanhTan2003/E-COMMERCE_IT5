package org.programmingtechie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmingtechie.dto.request.CustomerRequest;
import org.programmingtechie.dto.response.CustomerOrderList;
import org.programmingtechie.dto.response.CustomerResponse;
import org.programmingtechie.dto.response.OrderResponse;
import org.programmingtechie.model.Customer;
import org.programmingtechie.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerServiceV1 {
    final CustomerRepository customerRepository;
    final WebClient.Builder webClientBuilder;


    public void createProduct(CustomerRequest customerRequest) {
        validCheckCustomerRequest(customerRequest);

        checkUniqueCustomerRequest(customerRequest);

        Customer customer = Customer.builder()
                .fullName(customerRequest.getFullName())
                .phoneNumber(customerRequest.getPhoneNumber())
                .address(customerRequest.getAddress())
                .email(customerRequest.getEmail())
                .dateOfBirth(customerRequest.getDateOfBirth())
                .gender(customerRequest.getGender())
                .build();
        customerRepository.save(customer);
    }

    void validCheckCustomerRequest (CustomerRequest customerRequest)
    {
        if (customerRequest.getFullName() == null || customerRequest.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập tên khách hàng!");
        }

        if (customerRequest.getPhoneNumber() == null || customerRequest.getPhoneNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập số điện thoại!");
        } else if (!customerRequest.getPhoneNumber().matches("\\d{11}")) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ!");
        }

        // Kiểm tra email (không bắt buộc nhưng nếu có phải đúng định dạng)
        if (customerRequest.getEmail() != null && !customerRequest.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new IllegalArgumentException("Địa chỉ email không hợp lệ!");
        }

        if (customerRequest.getDateOfBirth() != null) {
            LocalDate now = LocalDate.now();
            if (customerRequest.getDateOfBirth().isAfter(now)) {
                throw new IllegalArgumentException("Ngày sinh không hợp lệ!");
            }
        }

        if (customerRequest.getGender() != null && !(customerRequest.getGender().equalsIgnoreCase("Nam") || customerRequest.getGender().equalsIgnoreCase("Nữ"))) {
            throw new IllegalArgumentException("Giới tính không hợp lệ!");
        }
    }

    void checkUniqueCustomerRequest(CustomerRequest customerRequest) {
        customerRepository.findByPhoneNumber(customerRequest.getPhoneNumber())
                .ifPresent(customer -> {
                    throw new IllegalArgumentException("Số điện thoại đã tồn tại!");
                });

        if (customerRequest.getEmail() != null) {
            customerRepository.findByEmail(customerRequest.getEmail())
                    .ifPresent(customer -> {
                        throw new IllegalArgumentException("Địa chỉ email đã tồn tại!");
                    });
        }
    }

    void checkUniqueCustomerRequest(CustomerRequest customerRequest, String customerId) {
        Optional<Customer> customer_phoneNumber = customerRepository.findByPhoneNumber(customerRequest.getPhoneNumber());
        if (customer_phoneNumber.isPresent() && !customer_phoneNumber.get().getId().equals(customerId)) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại trong hệ thống!");
        }

        Optional<Customer> customer_email = customerRepository.findByEmail(customerRequest.getEmail());
        if (customer_email.isPresent() && !customer_email.get().getId().equals(customerId)) {
            throw new IllegalArgumentException("Email đã tồn tại trong hệ thống!");
        }
    }

    public Customer getCustomerById(String id)
    {
        Optional<Customer> customer = customerRepository.findById(id);
        if(customer.isEmpty())
        {
            throw new IllegalArgumentException("Không tìm thấy thông tin khách hàng!");
        }
        return customer.get();
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public void updateCustomer(String id, CustomerRequest customerRequest)
    {
        Customer customer = customerRepository.findById(id).get();
        if(customer == null)
        {
            throw new IllegalArgumentException("Khách hàng không tồn tại. Vui lòng kiểm tra lại!");
        }

        validCheckCustomerRequest(customerRequest);
        checkUniqueCustomerRequest(customerRequest, id);

        customer.setFullName(customerRequest.getFullName());
        customer.setAddress(customerRequest.getAddress());
        customer.setPhoneNumber(customerRequest.getPhoneNumber());
        customer.setEmail(customerRequest.getEmail());
        customer.setDateOfBirth(customerRequest.getDateOfBirth());
        customer.setGender(customerRequest.getGender());

        customerRepository.save(customer);
    }

    public void deleteCustomer(String id)
    {
        Customer customer = customerRepository.findById(id).get();
        if(customer == null)
        {
            throw new IllegalArgumentException("Khách hàng không tồn tại. Vui lòng kiểm tra lại!");
        }
        customerRepository.delete(customer);
    }

    public Customer getCustomerByPhoneNumber(String phoneNumber)
    {
        Optional<Customer> customer = customerRepository.findByPhoneNumber(phoneNumber);
        if(customer.isEmpty())
        {
            throw new IllegalArgumentException("Không tìm thấy thông tin khách hàng! " + phoneNumber);
        }
        return customer.get();
    }

    public Customer getCustomerByEmail(String email)
    {
        Optional<Customer> customer = customerRepository.findByEmail(email);
        if(customer.isEmpty())
        {
            throw new IllegalArgumentException("Không tìm thấy thông tin khách hàng! " + email);
        }
        return customer.get();
    }

    public CustomerOrderList getOrderList(String id)
    {
        Optional<Customer> customer = customerRepository.findById(id);

        if(customer.isEmpty())
            throw new IllegalArgumentException("Không tìm thấy khách hàng có id là " + id + "!");

        try
        {
            OrderResponse[] orderResponses = webClientBuilder.build().get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/order")
                            .queryParam("list_product_id", String.join(",", id))
                            .build())
                    .retrieve()
                    .bodyToMono(OrderResponse[].class)
                    .block();

            if (orderResponses == null) {
                throw new IllegalStateException("Không có đơn hàng!");
            }
            CustomerResponse customerResponse = CustomerResponse.builder()
                    .id(customer.get().getId())
                    .fullName(customer.get().getFullName())
                    .address(customer.get().getAddress())
                    .phoneNumber(customer.get().getPhoneNumber())
                    .email(customer.get().getEmail())
                    .dateOfBirth(customer.get().getDateOfBirth())
                    .gender(customer.get().getGender())
                    .build();

            return CustomerOrderList.builder()
                    .customerResponse(customerResponse)
                    .orderResponses(Arrays.stream(orderResponses).toList())
                    .build();
        }
        catch (WebClientException e) {
            log.info("ERROR - Xảy ra lỗi khi giao tiếp với order-service: {}", e.getMessage());

            throw new IllegalArgumentException("Dịch vụ quản lý hóa đơn (order-service) không khả dụng. Vui lòng kiểm tra hoặc thử lại sau!");
        } catch (Exception e) {
            log.info("ERROR - Xảy ra lỗi khi giao tiếp với order-service: {}", e.getMessage());

            throw new IllegalArgumentException("Có lỗi xảy ra khi kiểm tra đơn hàng. Vui lòng thử lại sau!");
        }
    }
}
