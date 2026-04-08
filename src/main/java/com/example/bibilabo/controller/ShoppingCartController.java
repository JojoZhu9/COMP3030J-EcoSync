package com.example.bibilabo.controller;

import com.example.bibilabo.entity.ShoppingCart;
import com.example.bibilabo.service.ShoppingCartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "4. 购物车模块", description = "管理消费者添加至购物车的商品集合")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "查看购物车", description = "获取当前用户购物车内的所有商品清单")
    public List<ShoppingCart> getByUserId(@Parameter(description = "用户ID") @PathVariable("userId") Integer userId) {
        return shoppingCartService.getCartByUserId(userId);
    }

    @PostMapping
    @Operation(summary = "加入购物车", description = "将选中的临期商品加入购物车。若已存在，则累加数量")
    public String add(@RequestBody ShoppingCart cart) {
        return shoppingCartService.addToCart(cart);
    }

    @PutMapping("/{cartItemId}")
    @Operation(summary = "修改商品数量", description = "在购物车中直接修改购买数量（传参如：?quantity=3）")
    public String updateQuantity(
            @Parameter(description = "购物车记录项ID") @PathVariable("cartItemId") Integer cartItemId,
            @Parameter(description = "最新数量") @RequestParam("quantity") Integer quantity) {
        return shoppingCartService.updateCartItemQuantity(cartItemId, quantity);
    }

    @DeleteMapping("/{cartItemId}")
    @Operation(summary = "移除单项商品", description = "从购物车中删除指定的某一条加购记录")
    public String delete(@Parameter(description = "购物车记录项ID") @PathVariable("cartItemId") Integer cartItemId) {
        return shoppingCartService.removeCartItem(cartItemId);
    }

    @DeleteMapping("/user/{userId}")
    @Operation(summary = "清空购物车", description = "一键删除该用户的所有购物车数据")
    public String clear(@Parameter(description = "用户ID") @PathVariable("userId") Integer userId) {
        return shoppingCartService.clearCart(userId);
    }
}