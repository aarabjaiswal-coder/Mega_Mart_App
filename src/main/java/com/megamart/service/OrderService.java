package com.megamart.service;

import com.megamart.dto.order.CartItemRequest;
import com.megamart.dto.order.SellerOrderView;
import com.megamart.dto.order.OrderResponse;
import com.megamart.exception.BusinessException;
import com.megamart.exception.NotFoundException;
import com.megamart.model.Customer;
import com.megamart.model.Order;
import com.megamart.model.OrderItem;
import com.megamart.model.OrderStatus;
import com.megamart.model.PaymentStatus;
import com.megamart.model.Product;
import com.megamart.repository.CustomerRepository;
import com.megamart.repository.OrderRepository;
import com.megamart.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class OrderService {
    private static final BigDecimal MAX_ORDER_TOTAL = new BigDecimal("99999999.99");
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository,
                        CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public OrderResponse placeOrder(String recipientName, String suppliedPhone, String deliveryAddress,
                                    List<CartItemRequest> requestedItems) {
        if (requestedItems == null || requestedItems.isEmpty()) {
            throw new BusinessException("Your cart is empty.");
        }
        String name = cleanRequired(recipientName, 120, "Enter the recipient's name.");
        String phone = normalizePhone(suppliedPhone);
        String address = cleanRequired(deliveryAddress, 1000, "Enter a delivery address.");

        Customer customer = customerRepository.findByPhone(phone).orElse(null);
        if (customer == null) {
            customerRepository.insertIfAbsent(phone);
            customer = customerRepository.findByPhone(phone)
                    .orElseThrow(() -> new IllegalStateException("Customer record could not be created."));
        }
        Map<Long, Integer> quantities = new TreeMap<>();
        for (CartItemRequest item : requestedItems) {
            if (item.productId() == null || item.quantity() == null || item.quantity() < 1 || item.quantity() > 99) {
                throw new BusinessException("Each cart item must have a valid product and quantity from 1 to 99.");
            }
            try {
                quantities.merge(item.productId(), item.quantity(), Math::addExact);
            } catch (ArithmeticException exception) {
                throw new BusinessException("Requested item quantity is too large.");
            }
        }

        Order order = new Order(customer, name, address);
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            Product product = productRepository.findActiveByIdForUpdate(entry.getKey())
                    .orElseThrow(() -> new BusinessException("A requested product is unavailable."));
            int quantity = entry.getValue();
            if (product.getStockQuantity() < quantity) {
                throw new BusinessException("There is not enough stock for " + product.getName() + ".");
            }
            product.setStockQuantity(product.getStockQuantity() - quantity);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
            if (total.compareTo(MAX_ORDER_TOTAL) > 0) {
                throw new BusinessException("The order total is above the maximum supported amount.");
            }
            order.addItem(new OrderItem(product.getId(), product.getName(), quantity, product.getPrice()));
        }
        order.setTotalAmount(total);
        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public Page<SellerOrderView> sellerOrders(Pageable pageable) {
        return orderRepository.findAllByOrderByCreatedAtDesc(pageable).map(SellerOrderView::from);
    }

    @Transactional(readOnly = true)
    public long countOrders(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> customerOrders(String phone, Pageable pageable) {
        if (phone == null || phone.isBlank()) return List.of();
        return orderRepository.findByCustomer_PhoneOrderByCreatedAtDesc(phone, pageable)
                .stream().map(OrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public SellerOrderView sellerOrder(Long orderId) {
        return orderRepository.findDetailsById(orderId)
                .map(SellerOrderView::from)
                .orElseThrow(() -> new NotFoundException("Order not found."));
    }

    @Transactional
    public void updateStatus(Long orderId, OrderStatus next) {
        Order order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found."));
        OrderStatus current = order.getStatus();
        if (current == next) {
            return;
        }
        boolean allowed = switch (current) {
            case PLACED -> next == OrderStatus.PROCESSING || next == OrderStatus.CANCELLED;
            case PROCESSING -> next == OrderStatus.OUT_FOR_DELIVERY || next == OrderStatus.CANCELLED;
            case OUT_FOR_DELIVERY -> next == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
        if (!allowed) {
            throw new BusinessException("That order status change is not allowed.");
        }
        if (next == OrderStatus.CANCELLED) {
            Map<Long, Integer> quantitiesToRestore = new TreeMap<>();
            for (OrderItem item : order.getItems()) {
                quantitiesToRestore.merge(item.getProductId(), item.getQuantity(), Math::addExact);
            }
            for (Map.Entry<Long, Integer> restore : quantitiesToRestore.entrySet()) {
                Product product = productRepository.findByIdForUpdate(restore.getKey())
                        .orElseThrow(() -> new BusinessException(
                                "An order product is unavailable, so stock could not be restored."));
                try {
                    product.setStockQuantity(Math.addExact(product.getStockQuantity(), restore.getValue()));
                } catch (ArithmeticException exception) {
                    throw new BusinessException("Restoring stock would exceed the product stock limit.");
                }
            }
        }
        order.setStatus(next);
    }

    @Transactional
    public void markCashCollected(Long orderId) {
        Order order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found."));
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessException("Cash can be recorded after the order is delivered.");
        }
        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            order.markCashCollected();
        }
    }

    private String cleanRequired(String value, int maxLength, String errorMessage) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.isEmpty() || cleaned.length() > maxLength) {
            throw new BusinessException(errorMessage);
        }
        return cleaned;
    }

    private String normalizePhone(String supplied) {
        if (supplied == null) {
            throw new BusinessException("Enter a valid phone number.");
        }
        String cleaned = supplied.trim().replaceAll("[\\s()\\-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else if (cleaned.matches("\\d{10}")) {
            digits = "91" + cleaned;
        } else if (cleaned.matches("91\\d{10}")) {
            digits = cleaned;
        } else {
            throw new BusinessException("Use a 10-digit Indian mobile number or an international number.");
        }
        if (!digits.matches("[1-9]\\d{7,14}")) {
            throw new BusinessException("Enter a valid phone number.");
        }
        return "+" + digits;
    }
}
