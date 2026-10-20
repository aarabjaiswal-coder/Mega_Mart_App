package com.megamart.controller;

import com.megamart.dto.order.SellerOrderView;
import com.megamart.dto.product.ProductForm;
import com.megamart.dto.product.ProductResponse;
import com.megamart.exception.BusinessException;
import com.megamart.model.OrderStatus;
import com.megamart.service.OrderService;
import com.megamart.service.ProductService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/seller")
public class SellerPageController {
    private static final int PAGE_SIZE = 50;
    private static final String LAST_SEEN_ORDER_KEY = "sellerLastSeenOrderId";
    private final OrderService orderService;
    private final ProductService productService;

    public SellerPageController(OrderService orderService, ProductService productService) {
        this.orderService = orderService;
        this.productService = productService;
    }

    @GetMapping("/login")
    public String login() {
        return "seller-login";
    }

    @GetMapping({"", "/orders"})
    public String orders(@RequestParam(defaultValue = "0") int page,
                         HttpSession session, Model model) {
        Page<SellerOrderView> orders = orderService.sellerOrders(PageRequest.of(
                Math.max(0, page), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt")));
        Long previousNewest = (Long) session.getAttribute(LAST_SEEN_ORDER_KEY);
        List<SellerOrderView> newOrders;
        if (previousNewest == null) {
            newOrders = List.of();
            session.setAttribute(LAST_SEEN_ORDER_KEY, orders.getContent().stream()
                    .map(SellerOrderView::orderId).max(Long::compareTo).orElse(0L));
        } else {
            newOrders = orders.getContent().stream()
                    .filter(order -> order.orderId() > previousNewest)
                    .toList();
            orders.getContent().stream().map(SellerOrderView::orderId).max(Long::compareTo)
                    .ifPresent(newest -> session.setAttribute(LAST_SEEN_ORDER_KEY,
                            Math.max(previousNewest, newest)));
        }
        model.addAttribute("orders", orders);
        model.addAttribute("newOrders", newOrders);
        model.addAttribute("placedCount", orderService.countOrders(OrderStatus.PLACED));
        model.addAttribute("processingCount", orderService.countOrders(OrderStatus.PROCESSING));
        model.addAttribute("deliveryCount", orderService.countOrders(OrderStatus.OUT_FOR_DELIVERY));
        model.addAttribute("deliveredCount", orderService.countOrders(OrderStatus.DELIVERED));
        return "seller-orders";
    }

    @PostMapping("/orders/{id}/status")
    public String changeStatus(@PathVariable Long id,
                               @RequestParam OrderStatus status,
                               RedirectAttributes redirectAttributes) {
        try {
            orderService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("sellerMessage", "Order status updated.");
        } catch (BusinessException exception) {
            redirectAttributes.addFlashAttribute("sellerError", exception.getMessage());
        }
        return "redirect:/seller/orders";
    }

    @PostMapping("/orders/{id}/cash-collected")
    public String markCashCollected(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.markCashCollected(id);
            redirectAttributes.addFlashAttribute("sellerMessage", "Cash recorded as collected.");
        } catch (BusinessException exception) {
            redirectAttributes.addFlashAttribute("sellerError", exception.getMessage());
        }
        return "redirect:/seller/orders";
    }

    @GetMapping("/products")
    public String products(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<ProductResponse> products = productService.listAll(PageRequest.of(
                Math.max(0, page), PAGE_SIZE, Sort.by(Sort.Direction.ASC, "name")));
        model.addAttribute("products", products);
        return "seller-products";
    }

    @GetMapping("/products/new")
    public String newProduct(Model model) {
        prepareProductForm(model, new ProductForm(), "Add product", "/seller/products");
        return "product-form";
    }

    @GetMapping("/products/{id}/edit")
    public String editProduct(@PathVariable Long id, Model model) {
        ProductResponse product = productService.get(id);
        prepareProductForm(model, new ProductForm(product), "Edit product", "/seller/products/" + id);
        return "product-form";
    }

    @PostMapping("/products")
    public String createProduct(@Valid @ModelAttribute("form") ProductForm form,
                                BindingResult bindingResult, Model model,
                                @RequestParam(required = false) MultipartFile imageFile,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareProductForm(model, form, "Add product", "/seller/products");
            return "product-form";
        }
        if (imageError(imageFile) != null) {
            model.addAttribute("formError", imageError(imageFile));
            prepareProductForm(model, form, "Add product", "/seller/products");
            return "product-form";
        }
        try {
            ProductResponse created = productService.create(form.toRequest());
            saveImageIfPresent(created.id(), imageFile);
            redirectAttributes.addFlashAttribute("sellerMessage", "Product added.");
            return "redirect:/seller/products";
        } catch (DataIntegrityViolationException exception) {
            model.addAttribute("formError", "That SKU is already in use.");
            prepareProductForm(model, form, "Add product", "/seller/products");
            return "product-form";
        }
    }

    @PostMapping("/products/{id}")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute("form") ProductForm form,
                                BindingResult bindingResult, Model model,
                                @RequestParam(required = false) MultipartFile imageFile,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareProductForm(model, form, "Edit product", "/seller/products/" + id);
            return "product-form";
        }
        if (imageError(imageFile) != null) {
            model.addAttribute("formError", imageError(imageFile));
            prepareProductForm(model, form, "Edit product", "/seller/products/" + id);
            return "product-form";
        }
        try {
            productService.update(id, form.toRequest());
            saveImageIfPresent(id, imageFile);
            redirectAttributes.addFlashAttribute("sellerMessage", "Product updated.");
            return "redirect:/seller/products";
        } catch (DataIntegrityViolationException exception) {
            model.addAttribute("formError", "That SKU is already in use.");
            prepareProductForm(model, form, "Edit product", "/seller/products/" + id);
            return "product-form";
        } catch (BusinessException exception) {
            redirectAttributes.addFlashAttribute("sellerError", exception.getMessage());
            return "redirect:/seller/products";
        }
    }

    private void prepareProductForm(Model model, ProductForm form, String title, String action) {
        model.addAttribute("form", form);
        model.addAttribute("formTitle", title);
        model.addAttribute("formAction", action);
    }

    private String imageError(MultipartFile image) {
        if (image == null || image.isEmpty()) return null;
        if (image.getSize() > 5 * 1024 * 1024) return "Choose an image smaller than 5 MB.";
        String type = image.getContentType();
        if (type == null || !List.of("image/jpeg", "image/png", "image/webp", "image/gif").contains(type))
            return "Choose a JPG, PNG, WEBP, or GIF image.";
        return null;
    }

    private void saveImageIfPresent(Long productId, MultipartFile image) {
        if (image == null || image.isEmpty()) return;
        try {
            productService.saveImage(productId, image.getContentType(), image.getBytes());
        } catch (java.io.IOException exception) {
            throw new BusinessException("The image could not be read. Please choose it again.");
        }
    }
}
