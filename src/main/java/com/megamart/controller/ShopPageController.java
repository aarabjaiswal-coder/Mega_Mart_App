package com.megamart.controller;

import com.megamart.dto.order.CartItemRequest;
import com.megamart.dto.order.CartSummaryResponse;
import com.megamart.dto.order.CheckoutForm;
import com.megamart.dto.product.ProductResponse;
import com.megamart.exception.BusinessException;
import com.megamart.service.OrderService;
import com.megamart.service.ProductService;
import com.megamart.service.SupabaseAuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.megamart.dto.order.OrderResponse;

@Controller
public class ShopPageController {
    private static final String CART_KEY = "megaMartCart";
    private static final int PAGE_SIZE = 48;
    private final ProductService productService;
    private final OrderService orderService;
    private final SupabaseAuthService authService;

    public ShopPageController(ProductService productService, OrderService orderService, SupabaseAuthService authService) {
        this.productService = productService;
        this.orderService = orderService;
        this.authService = authService;
    }

    @GetMapping("/")
    public String root() {
        return "index";
    }

    @GetMapping("/shop")
    public String shop(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String category,
                       HttpSession session, Model model) {
        int safePage = Math.max(0, page);
        String selectedCategory = category == null || category.isBlank() ? null : category.trim();
        Page<ProductResponse> products = productService.list(selectedCategory,
                PageRequest.of(safePage, PAGE_SIZE, Sort.by(Sort.Direction.ASC, "name")));
        List<CartLine> cartLines = cartLines(session);
        model.addAttribute("products", products);
        model.addAttribute("categories", productService.categories());
        model.addAttribute("selectedCategory", selectedCategory == null ? "" : selectedCategory);
        model.addAttribute("cartLines", cartLines);
        model.addAttribute("cartCount", cartLines.stream().mapToInt(CartLine::quantity).sum());
        model.addAttribute("cartTotal", total(cartLines));
        model.addAttribute("customerOrders", List.<OrderResponse>of());
        return "shop";
    }

    @PostMapping("/shop/cart/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        CartMutationResult result = addToCartState(productId, quantity, session);
        redirectAttributes.addFlashAttribute(result.success() ? "shopMessage" : "shopError", result.message());
        return "redirect:/shop";
    }

    @PostMapping(value = "/shop/cart/add", headers = "X-Requested-With=XMLHttpRequest")
    @ResponseBody
    public ResponseEntity<CartSummaryResponse> addToCartAjax(@RequestParam Long productId,
                                                              @RequestParam(defaultValue = "1") int quantity,
                                                              HttpSession session) {
        CartMutationResult result = addToCartState(productId, quantity, session);
        return ResponseEntity.status(result.success() ? 200 : 400)
                .body(cartSummary(session, result.message(), result.success()));
    }

    @PostMapping("/shop/cart/remove")
    public String removeFromCart(@RequestParam Long productId, HttpSession session) {
        cart(session).remove(productId);
        return "redirect:/shop";
    }

    @PostMapping(value = "/shop/cart/remove", headers = "X-Requested-With=XMLHttpRequest")
    @ResponseBody
    public ResponseEntity<CartSummaryResponse> removeFromCartAjax(@RequestParam Long productId, HttpSession session) {
        cart(session).remove(productId);
        return ResponseEntity.ok(cartSummary(session, "Item removed from your basket.", true));
    }

    @GetMapping("/shop/checkout")
    public String checkout(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        List<CartLine> cartLines = cartLines(session);
        if (cartLines.isEmpty()) {
            redirectAttributes.addFlashAttribute("shopError", "Add an item before checking out.");
            return "redirect:/shop";
        }
        model.addAttribute("form", new CheckoutForm());
        addCheckoutCart(model, cartLines);
        return "checkout";
    }

    @PostMapping("/shop/orders")
    public String placeOrder(@Valid @ModelAttribute("form") CheckoutForm form,
                             BindingResult bindingResult,
                             @RequestParam(required = false) String customerAccessToken,
                             HttpSession session,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        List<CartLine> cartLines = cartLines(session);
        if (cartLines.isEmpty()) {
            redirectAttributes.addFlashAttribute("shopError", "Your cart is empty.");
            return "redirect:/shop";
        }
        if (bindingResult.hasErrors()) {
            addCheckoutCart(model, cartLines);
            return "checkout";
        }
        try {
            SupabaseAuthService.CustomerIdentity identity = authService.verifyBearerToken(
                    customerAccessToken == null ? null : "Bearer " + customerAccessToken);
            List<CartItemRequest> items = cartLines.stream()
                    .map(line -> new CartItemRequest(line.product().id(), line.quantity()))
                    .toList();
            OrderResponse placedOrder = orderService.placeOrder(form.getRecipientName(), form.getPhone(),
                    form.getDeliveryAddress(), identity.userId(), items);
            Long orderId = placedOrder.orderId();
            session.removeAttribute(CART_KEY);
            redirectAttributes.addFlashAttribute("orderPlaced",
                    "Order #" + orderId + " placed. Payment is Cash on Delivery.");
            return "redirect:/shop#customer-orders";
        } catch (BusinessException exception) {
            model.addAttribute("checkoutError", exception.getMessage());
            addCheckoutCart(model, cartLines);
            return "checkout";
        } catch (ResponseStatusException exception) {
            model.addAttribute("checkoutError", "Sign in with Google to place your order.");
            addCheckoutCart(model, cartLines);
            return "checkout";
        }
    }

    private void addCheckoutCart(Model model, List<CartLine> lines) {
        model.addAttribute("cartLines", lines);
        model.addAttribute("cartTotal", total(lines));
    }

    private BigDecimal total(List<CartLine> lines) {
        return lines.stream().map(CartLine::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private CartSummaryResponse cartSummary(HttpSession session, String message, boolean success) {
        List<CartLine> lines = cartLines(session);
        List<CartSummaryResponse.Item> items = lines.stream()
                .map(line -> new CartSummaryResponse.Item(line.product().id(), line.product().name(),
                        line.quantity(), line.product().offerPrice() == null ? line.product().price() : line.product().offerPrice(),
                        line.lineTotal()))
                .toList();
        return new CartSummaryResponse(lines.stream().mapToInt(CartLine::quantity).sum(), total(lines), items, message, success);
    }

    private CartMutationResult addToCartState(Long productId, int quantity, HttpSession session) {
        try {
            if (quantity < 1 || quantity > 99) throw new BusinessException("Choose a quantity from 1 to 99.");
            ProductResponse product = productService.get(productId);
            Map<Long, Integer> cart = cart(session);
            int requested = Math.addExact(cart.getOrDefault(productId, 0), quantity);
            if (requested > product.stockQuantity()) {
                throw new BusinessException("There is not enough stock for " + product.name() + ".");
            }
            cart.put(productId, requested);
            session.setAttribute(CART_KEY, cart);
            return new CartMutationResult(product.name() + " added to your basket.", true);
        } catch (BusinessException exception) {
            return new CartMutationResult(exception.getMessage(), false);
        } catch (ArithmeticException exception) {
            return new CartMutationResult("That quantity is too large.", false);
        }
    }

    private List<CartLine> cartLines(HttpSession session) {
        Map<Long, Integer> cart = cart(session);
        List<CartLine> lines = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : new ArrayList<>(cart.entrySet())) {
            try {
                ProductResponse product = productService.get(entry.getKey());
                int quantity = Math.min(entry.getValue(), product.stockQuantity());
                if (quantity < 1) {
                    cart.remove(entry.getKey());
                } else {
                    if (quantity != entry.getValue()) cart.put(entry.getKey(), quantity);
                    BigDecimal unitPrice = product.offerPrice() == null ? product.price() : product.offerPrice();
                    BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
                    lines.add(new CartLine(product, quantity, lineTotal));
                }
            } catch (BusinessException exception) {
                cart.remove(entry.getKey());
            }
        }
        session.setAttribute(CART_KEY, cart);
        return lines;
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> cart(HttpSession session) {
        Object stored = session.getAttribute(CART_KEY);
        if (stored instanceof Map<?, ?>) {
            return (Map<Long, Integer>) stored;
        }
        Map<Long, Integer> cart = new LinkedHashMap<>();
        session.setAttribute(CART_KEY, cart);
        return cart;
    }

    public record CartLine(ProductResponse product, int quantity, BigDecimal lineTotal) { }
    private record CartMutationResult(String message, boolean success) { }
}
