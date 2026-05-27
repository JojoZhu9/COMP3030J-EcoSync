package com.example.bibilabo.service.impl;

import com.example.bibilabo.entity.*;
import com.example.bibilabo.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderMapper orderMapper;
    @Mock private OrderItemMapper orderItemMapper;
    @Mock private ShoppingCartMapper cartMapper;
    @Mock private ExpiringProductMapper expiringProductMapper;
    @Mock private StandardProductMapper standardProductMapper;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Captor
    private ArgumentCaptor<BigDecimal> amountCaptor;

    @BeforeEach
    void setUpOrderId() {
        // 模拟 @Options(useGeneratedKeys=true)：insert 时自动设置 orderId
        // lenient: 不是所有测试都会调用 insert
        lenient().doAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setOrderId(99);
            return null;
        }).when(orderMapper).insert(any(Order.class));
    }

    // ========== checkout 异常场景 ==========

    @Test
    void checkout_emptyCart_throws() {
        when(cartMapper.findByUserId(12)).thenReturn(List.of());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.checkout(12, 1, null));
        assertTrue(ex.getMessage().contains("Cart is empty"));
    }

    @Test
    void checkout_nullCart_throws() {
        when(cartMapper.findByUserId(12)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.checkout(12, 1, null));
        assertTrue(ex.getMessage().contains("Cart is empty"));
    }

    @Test
    void checkout_productNotAvailable_throws() {
        ShoppingCart cartItem = new ShoppingCart();
        cartItem.setProductId(1);
        cartItem.setQuantity(1);

        ExpiringProduct product = new ExpiringProduct();
        product.setProductId(1);
        product.setStatus("SOLD_OUT");

        when(cartMapper.findByUserId(12)).thenReturn(List.of(cartItem));
        when(expiringProductMapper.findByIdForUpdate(1)).thenReturn(product);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.checkout(12, 1, null));
        assertTrue(ex.getMessage().contains("Product is unavailable or sold out"));
    }

    @Test
    void checkout_productNull_throws() {
        ShoppingCart cartItem = new ShoppingCart();
        cartItem.setProductId(1);
        cartItem.setQuantity(1);

        when(cartMapper.findByUserId(12)).thenReturn(List.of(cartItem));
        when(expiringProductMapper.findByIdForUpdate(1)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.checkout(12, 1, null));
        assertTrue(ex.getMessage().contains("Product is unavailable or sold out"));
    }

    @Test
    void checkout_stockInsufficient_throws() {
        ShoppingCart cartItem = new ShoppingCart();
        cartItem.setProductId(1);
        cartItem.setQuantity(5);

        ExpiringProduct product = new ExpiringProduct();
        product.setProductId(1);
        product.setBarcode("6901234560001");
        product.setStatus("AVAILABLE");
        product.setRemainingStock(3);

        when(cartMapper.findByUserId(12)).thenReturn(List.of(cartItem));
        when(expiringProductMapper.findByIdForUpdate(1)).thenReturn(product);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.checkout(12, 1, null));
        assertTrue(ex.getMessage().contains("Insufficient stock"));
    }

    @Test
    void checkout_insufficientBalance_throws() {
        ShoppingCart cartItem = new ShoppingCart();
        cartItem.setProductId(1);
        cartItem.setQuantity(1);

        ExpiringProduct product = new ExpiringProduct();
        product.setProductId(1);
        product.setBarcode("6901234560001");
        product.setStatus("AVAILABLE");
        product.setRemainingStock(10);
        product.setStoreId(1);
        product.setExpirationTime(new Date(System.currentTimeMillis() + 24 * 3600 * 1000));

        StandardProduct stdProduct = new StandardProduct();
        stdProduct.setBarcode("6901234560001");
        stdProduct.setNormalPrice(new BigDecimal("10.00"));
        stdProduct.setDiscountRates("[1.0,0.9,0.8,0.7,0.6,0.5,0.4,0.3,0.2,0.1,0.1,0.1]");

        User user = new User();
        user.setUserId(12);
        user.setBalance(new BigDecimal("1.00"));

        when(cartMapper.findByUserId(12)).thenReturn(List.of(cartItem));
        when(expiringProductMapper.findByIdForUpdate(1)).thenReturn(product);
        when(standardProductMapper.findByBarcode("6901234560001")).thenReturn(stdProduct);
        when(userMapper.findByIdForUpdate(12)).thenReturn(user);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.checkout(12, 1, null));
        assertTrue(ex.getMessage().contains("Insufficient balance"));
    }

    // ========== checkout 折扣计算（核心逻辑） ==========

    private void setupCheckoutMocks(ExpiringProduct product, StandardProduct stdProduct) {
        ShoppingCart cartItem = new ShoppingCart();
        cartItem.setProductId(1);
        cartItem.setQuantity(1);

        User user = new User();
        user.setUserId(12);
        user.setBalance(new BigDecimal("9999.00"));

        when(cartMapper.findByUserId(12)).thenReturn(List.of(cartItem));
        when(expiringProductMapper.findByIdForUpdate(1)).thenReturn(product);
        when(expiringProductMapper.decreaseStock(1, 1)).thenReturn(1);
        when(standardProductMapper.findByBarcode("6901234560001")).thenReturn(stdProduct);
        when(userMapper.findByIdForUpdate(12)).thenReturn(user);
        when(userMapper.decreaseBalance(eq(12), any(BigDecimal.class))).thenReturn(1);
    }

    private StandardProduct createStandardProduct(BigDecimal price, String discountRates) {
        StandardProduct p = new StandardProduct();
        p.setBarcode("6901234560001");
        p.setNormalPrice(price);
        p.setDiscountRates(discountRates);
        return p;
    }

    private ExpiringProduct createExpiringProduct(long hoursFromNow) {
        ExpiringProduct p = new ExpiringProduct();
        p.setProductId(1);
        p.setBarcode("6901234560001");
        p.setStatus("AVAILABLE");
        p.setRemainingStock(10);
        // 加30分钟缓冲，避免整数除法截断：diffMillis/3600000 在边界处会少1
        p.setExpirationTime(new Date(System.currentTimeMillis() + hoursFromNow * 3600 * 1000 + 30 * 60 * 1000));
        return p;
    }

    @Test
    void checkout_discountCalculation_5hoursLeft() {
        // 5小时剩余 → hoursLeft < 12 → index = 11 - 5 = 6 → discount = 0.4
        // 价格 = 10.00 * 0.4 = 4.000
        setupCheckoutMocks(
                createExpiringProduct(5),
                createStandardProduct(new BigDecimal("10.00"),
                        "[1.0,0.9,0.8,0.7,0.6,0.5,0.4,0.3,0.2,0.1,0.1,0.1]")
        );

        CheckoutResult result = orderService.checkout(12, 1, null);

        assertTrue(result.getMessage().contains("Order placed successfully"));
        assertFalse(result.getOrders().isEmpty());
        assertTrue(result.getOrders().get(0).getPickupCode().contains("PICKUP-"));

        verify(orderMapper).updateTotalAmount(eq(99), amountCaptor.capture());
        BigDecimal actualAmount = amountCaptor.getValue().setScale(2, RoundingMode.HALF_UP);
        assertEquals(0, new BigDecimal("4.00").compareTo(actualAmount),
                "5小时剩余折扣后价格应为4.00，实际: " + actualAmount);
        verify(cartMapper).clearCartByUserId(12);
    }

    @Test
    void checkout_noDiscount_over12Hours() {
        setupCheckoutMocks(
                createExpiringProduct(24),
                createStandardProduct(new BigDecimal("10.00"),
                        "[1.0,0.9,0.8,0.7,0.6,0.5,0.4,0.3,0.2,0.1,0.1,0.1]")
        );

        orderService.checkout(12, 1, null);

        verify(orderMapper).updateTotalAmount(eq(99), amountCaptor.capture());
        assertEquals(0, new BigDecimal("10.00").compareTo(amountCaptor.getValue()),
                "超过12小时不应打折");
    }

    @Test
    void checkout_discountBoundary_11hoursLeft() {
        // 11小时 → hoursLeft < 12 → index = 11 - 11 = 0 → discount = 1.0
        // 价格 = 10.00 * 1.0 = 10.000
        setupCheckoutMocks(
                createExpiringProduct(11),
                createStandardProduct(new BigDecimal("10.00"),
                        "[1.0,0.9,0.8,0.7,0.6,0.5,0.4,0.3,0.2,0.1,0.1,0.1]")
        );

        orderService.checkout(12, 1, null);

        verify(orderMapper).updateTotalAmount(eq(99), amountCaptor.capture());
        assertEquals(0, new BigDecimal("10.00").compareTo(amountCaptor.getValue()),
                "11小时折扣应为1.0");
    }

    @Test
    void checkout_discountBoundary_1hourLeft() {
        // 1小时 → hoursLeft < 12 → index = 11 - 1 = 10 → discount = 0.1
        // 价格 = 10.00 * 0.1 = 1.000
        setupCheckoutMocks(
                createExpiringProduct(1),
                createStandardProduct(new BigDecimal("10.00"),
                        "[1.0,0.9,0.8,0.7,0.6,0.5,0.4,0.3,0.2,0.1,0.1,0.1]")
        );

        orderService.checkout(12, 1, null);

        verify(orderMapper).updateTotalAmount(eq(99), amountCaptor.capture());
        assertEquals(0, new BigDecimal("1.00").compareTo(amountCaptor.getValue()),
                "1小时折扣应为0.1，价格应为1.00");
    }

    @Test
    void checkout_jsonParseError_fallbackToFullPrice() {
        setupCheckoutMocks(
                createExpiringProduct(5),
                createStandardProduct(new BigDecimal("10.00"), "invalid-json")
        );

        orderService.checkout(12, 1, null);

        // JSON 解析失败，回退到原价
        verify(orderMapper).updateTotalAmount(eq(99), amountCaptor.capture());
        assertEquals(0, new BigDecimal("10.00").compareTo(amountCaptor.getValue()),
                "JSON解析失败应回退到原价");
    }

    // ========== confirmPickup ==========

    @Test
    void confirmPickup_validCode_success() {
        when(orderMapper.completeOrderByPickupCode("PICKUP-ABC123")).thenReturn(1);

        String result = orderService.confirmPickup("PICKUP-ABC123");
        assertTrue(result.contains("Pickup verified successfully"));
    }

    @Test
    void confirmPickup_invalidCode_failure() {
        when(orderMapper.completeOrderByPickupCode("INVALID")).thenReturn(0);

        String result = orderService.confirmPickup("INVALID");
        assertTrue(result.contains("Invalid"));
    }

    // ========== getOrderDetails ==========

    @Test
    void getOrderDetails_nonexistent_throws() {
        when(orderMapper.findById(999)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.getOrderDetails(999));
        assertEquals("Order does not exist", ex.getMessage());
    }

    @Test
    void getOrderDetails_existing_returnsVO() {
        Order order = new Order();
        order.setOrderId(1);
        order.setUserId(12);
        order.setStoreId(1);
        order.setTotalAmount(new BigDecimal("15.00"));
        order.setPickupCode("PICKUP-ABC");
        order.setStatus("COMPLETED");

        OrderItem item = new OrderItem();
        item.setItemId(1);
        item.setOrderId(1);
        item.setProductId(1);
        item.setQuantity(1);
        item.setActualPrice(new BigDecimal("15.00"));

        when(orderMapper.findById(1)).thenReturn(order);
        when(orderItemMapper.findByOrderId(1)).thenReturn(List.of(item));

        OrderVO vo = orderService.getOrderDetails(1);

        assertEquals(1, vo.getOrderId());
        assertEquals(12, vo.getUserId());
        assertEquals(1, vo.getItems().size());
        assertEquals(new BigDecimal("15.00"), vo.getItems().get(0).getActualPrice());
    }
}
