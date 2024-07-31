package org.programmingtechie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.programmingtechie.dto.request.CustomerRequest;
import org.programmingtechie.dto.response.CustomerExistingResponse;
import org.programmingtechie.dto.response.CustomerOrderList;
import org.programmingtechie.dto.response.CustomerResponse;
import org.programmingtechie.dto.response.OrderResponse;
import org.programmingtechie.model.Customer;
import org.programmingtechie.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;


import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerServiceV1 {
    final CustomerRepository customerRepository;
    final WebClient.Builder webClientBuilder;
    

    public void createCustomer(CustomerRequest customerRequest) {
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

    void validCheckCustomerRequest(CustomerRequest customerRequest) {
        if (customerRequest.getFullName() == null || customerRequest.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập tên khách hàng!");
        }

        if (customerRequest.getPhoneNumber() == null || customerRequest.getPhoneNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập số điện thoại!");
        } else if (!customerRequest.getPhoneNumber().matches("\\d{11}")) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ!");
        }

        // Kiểm tra email (không bắt buộc nhưng nếu có phải đúng định dạng)
        if (customerRequest.getEmail() != null
                && !customerRequest.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new IllegalArgumentException("Địa chỉ email không hợp lệ!");
        }

        if (customerRequest.getDateOfBirth() != null) {
            LocalDate now = LocalDate.now();
            if (customerRequest.getDateOfBirth().isAfter(now)) {
                throw new IllegalArgumentException("Ngày sinh không hợp lệ!");
            }
        }

        if (customerRequest.getGender() != null && !(customerRequest.getGender().equalsIgnoreCase("Nam")
                || customerRequest.getGender().equalsIgnoreCase("Nữ"))) {
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
        Optional<Customer> customer_phoneNumber = customerRepository
                .findByPhoneNumber(customerRequest.getPhoneNumber());
        if (customer_phoneNumber.isPresent() && !customer_phoneNumber.get().getId().equals(customerId)) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại trong hệ thống!");
        }

        Optional<Customer> customer_email = customerRepository.findByEmail(customerRequest.getEmail());
        if (customer_email.isPresent() && !customer_email.get().getId().equals(customerId)) {
            throw new IllegalArgumentException("Email đã tồn tại trong hệ thống!");
        }
    }

    public Customer getCustomerById(String id) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy thông tin khách hàng!");
        }
        return customer.get();
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public void updateCustomer(String id, CustomerRequest customerRequest) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isEmpty()) {
            throw new IllegalArgumentException("Khách hàng không tồn tại. Vui lòng kiểm tra lại!");
        }

        validCheckCustomerRequest(customerRequest);
        checkUniqueCustomerRequest(customerRequest, id);

        Customer customer1 = customer.get();
        customer1.setFullName(customerRequest.getFullName());
        customer1.setAddress(customerRequest.getAddress());
        customer1.setPhoneNumber(customerRequest.getPhoneNumber());
        customer1.setEmail(customerRequest.getEmail());
        customer1.setDateOfBirth(customerRequest.getDateOfBirth());
        customer1.setGender(customerRequest.getGender());

        customerRepository.save(customer1);
    }

    public void deleteCustomer(String id) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isEmpty())
            throw new IllegalArgumentException("Khách hàng không tồn tại. Vui lòng kiểm tra lại!");

        OrderResponse orderResponse;

        try {
            orderResponse = webClientBuilder.build().get()
                    .uri("http://order-service/api/v1/order/first-order",
                            uriBuilder -> uriBuilder.queryParam("id", id).build())
                    .retrieve()
                    .bodyToMono(OrderResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            String errorMessage = extractMessageFromResponse(e.getResponseBodyAsString(),
                    "quản lý đơn hàng (order-service)");
            log.error("ERROR - Xảy ra lỗi khi giao tiếp với order-service: Status code - {}, Body - {}",
                    e.getStatusCode(), errorMessage);
            throw new IllegalArgumentException("Không thể xóa thông tin khách hàng. Vui lòng thử lại sau!");
        } catch (Exception e) {
            log.error(
                    "ERROR: Dịch vụ quản lý đơn hàng (order-service) không khả dụng. Vui lòng kiểm tra và thử lại. "
                            + e.getMessage());
            throw new IllegalStateException("Không thể xóa thông tin khách hàng. Vui lòng thử lại sau!");
        }

        if (orderResponse != null)
            throw new IllegalArgumentException(
                    "Không thể xóa thông tin khách hàng vì đã có đơn hàng trong hệ thống!");

        customerRepository.delete(customer.get());
    }

    public Customer getCustomerByPhoneNumber(String phoneNumber) {
        Optional<Customer> customer = customerRepository.findByPhoneNumber(phoneNumber);
        if (customer.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy thông tin khách hàng! " + phoneNumber);
        }
        return customer.get();
    }

    public Customer getCustomerByEmail(String email) {
        Optional<Customer> customer = customerRepository.findByEmail(email);
        if (customer.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy thông tin khách hàng! " + email);
        }
        return customer.get();
    }

    public CustomerOrderList getOrderList(String id) {
        Optional<Customer> customer = customerRepository.findById(id);

        if (customer.isEmpty())
            throw new IllegalArgumentException("Không tìm thấy khách hàng có id là " + id + "!");

        OrderResponse[] orderResponses;
        try {
            orderResponses = webClientBuilder.build().get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/order")
                            .queryParam("list_product_id", String.join(",", id))
                            .build())
                    .retrieve()
                    .bodyToMono(OrderResponse[].class)
                    .block();
        } catch (WebClientException e) {
            log.info("ERROR - Xảy ra lỗi khi giao tiếp với order-service: {}", e.getMessage());

            throw new IllegalArgumentException(
                    "Dịch vụ quản lý hóa đơn (order-service) không khả dụng. Vui lòng kiểm tra hoặc thử lại sau!");
        } catch (Exception e) {
            log.info("ERROR - Xảy ra lỗi khi giao tiếp với order-service: {}", e.getMessage());

            throw new IllegalArgumentException("Có lỗi xảy ra khi kiểm tra đơn hàng. Vui lòng thử lại sau!");
        }

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

    @Transactional(readOnly = true)
    public CustomerExistingResponse isExisting(String customerPhone) {
        Optional<Customer> customer = customerRepository.findByPhoneNumber(customerPhone);
        if (customer.isEmpty()) {
            return CustomerExistingResponse.builder()
                    .isExisting(false)
                    .build();
        } else
            return CustomerExistingResponse.builder()
                    .isExisting(true)
                    .id(customer.get().getId())
                    .fullName(customer.get().getFullName())
                    .address(customer.get().getAddress())
                    .phoneNumber(customer.get().getPhoneNumber())
                    .email(customer.get().getEmail())
                    .dateOfBirth(customer.get().getDateOfBirth())
                    .gender(customer.get().getGender())
                    .build();
    }

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

    public CustomerOrderList getOrderListById(String id) {
        OrderResponse[] orderResponses;
        try {
            orderResponses = webClientBuilder.build().get()
                    .uri("http://order-service/api/v1/order/customer-id",
                            uriBuilder -> uriBuilder.queryParam("customerId", id).build())
                    .retrieve()
                    .bodyToMono(OrderResponse[].class)
                    .block();
        } catch (WebClientResponseException e) {
            String errorMessage = extractMessageFromResponse(e.getResponseBodyAsString(),
                    "quản lý đơn hàng (order-service)");
            log.error("ERROR - Xảy ra lỗi khi giao tiếp với order-service: Status code - {}, Body - {}",
                    e.getStatusCode(), errorMessage);
            throw new IllegalArgumentException(errorMessage);
        } catch (Exception e) {
            log.error(
                    "ERROR: Dịch vụ quản lý đơn hàng (order-service) không khả dụng. Vui lòng kiểm tra và thử lại. "
                            + e.getMessage());
            throw new IllegalStateException(
                    "Dịch vụ quản lý đơn hàng (order-service) không khả dụng. Vui lòng kiểm tra và thử lại. ");
        }
        Optional<Customer> customer = customerRepository.findById(id);

        if (customer.isEmpty())
            throw new IllegalArgumentException(
                    String.format("Không tìm thấy thông tin khách hàng. Vui lòng kiểm tra lại!"));

        CustomerResponse customerResponse = CustomerResponse.builder()
                .id(customer.get().getId())
                .fullName(customer.get().getFullName())
                .phoneNumber(customer.get().getPhoneNumber())
                .email(customer.get().getEmail())
                .dateOfBirth(customer.get().getDateOfBirth())
                .gender(customer.get().getGender())
                .address(customer.get().getAddress())
                .address(customer.get().getAddress())
                .build();

        // assert orderResponses != null;

        // Sắp xếp orderResponses theo date giảm dần
        Arrays.sort(orderResponses, Comparator.comparing(OrderResponse::getDate).reversed());

        return CustomerOrderList.builder()
                .customerResponse(customerResponse)
                .totalOrder(orderResponses.length)
                .orderResponses(Arrays.stream(orderResponses).toList())
                .build();
    }

    public CustomerOrderList getOrderListByPhoneNumber(String phoneNumber) {
        OrderResponse[] orderResponses;
        try {
            orderResponses = webClientBuilder.build().get()
                    .uri("http://order-service/api/v1/order/customer-phone",
                            uriBuilder -> uriBuilder.queryParam("customerPhoneNumber", phoneNumber).build())
                    .retrieve()
                    .bodyToMono(OrderResponse[].class)
                    .block();
        } catch (WebClientResponseException e) {
            String errorMessage = extractMessageFromResponse(e.getResponseBodyAsString(),
                    "quản lý đơn hàng (order-service)");
            log.error("ERROR - Xảy ra lỗi khi giao tiếp với order-service: Status code - {}, Body - {}",
                    e.getStatusCode(), errorMessage);
            throw new IllegalArgumentException(errorMessage);
        } catch (Exception e) {
            log.error(
                    "ERROR: Dịch vụ quản lý đơn hàng (order-service) không khả dụng. Vui lòng kiểm tra và thử lại. "
                            + e.getMessage());
            throw new IllegalStateException(
                    "Dịch vụ quản lý đơn hàng (order-service) không khả dụng. Vui lòng kiểm tra và thử lại. ");
        }
        Optional<Customer> customer = customerRepository.findByPhoneNumber(phoneNumber);

        if (customer.isEmpty())
            throw new IllegalArgumentException(
                    String.format("Không tìm thấy thông tin khách hàng. Vui lòng kiểm tra lại!"));

        CustomerResponse customerResponse = CustomerResponse.builder()
                .id(customer.get().getId())
                .fullName(customer.get().getFullName())
                .phoneNumber(customer.get().getPhoneNumber())
                .email(customer.get().getEmail())
                .dateOfBirth(customer.get().getDateOfBirth())
                .gender(customer.get().getGender())
                .address(customer.get().getAddress())
                .address(customer.get().getAddress())
                .build();

        // assert orderResponses != null;

        // Sắp xếp orderResponses theo date giảm dần
        Arrays.sort(orderResponses, Comparator.comparing(OrderResponse::getDate).reversed());

        return CustomerOrderList.builder()
                .customerResponse(customerResponse)
                .totalOrder(orderResponses.length)
                .orderResponses(Arrays.stream(orderResponses).toList())
                .build();
    }
}
