package com.zhengli.catning.config;

import com.zhengli.catning.product.Product;
import com.zhengli.catning.product.ProductRepository;
import com.zhengli.catning.user.Role;
import com.zhengli.catning.user.User;
import com.zhengli.catning.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@ConditionalOnProperty(
        name = "app.demo-data.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class DemoDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminPassword;
    private final String courierPassword;

    public DemoDataInitializer(
            UserRepository userRepository,
            ProductRepository productRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.demo-data.admin-password:Admin123!}") String adminPassword,
            @Value("${app.demo-data.courier-password:Courier123!}") String courierPassword
    ) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
        this.courierPassword = courierPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        createUserIfAbsent("系统管理员", "13800000000", adminPassword, Role.ADMIN);
        createUserIfAbsent("校园配送员", "13900000000", courierPassword, Role.COURIER);

        if (productRepository.count() == 0) {
            productRepository.saveAll(List.of(
                    new Product(
                            "鲜肉小笼包",
                            "每日现包，6 只装",
                            new BigDecimal("7.50"),
                            120
                    ),
                    new Product(
                            "豆浆",
                            "无糖热豆浆，350ml",
                            new BigDecimal("2.50"),
                            200
                    ),
                    new Product(
                            "鸡蛋灌饼",
                            "现做鸡蛋灌饼，可选甜酱或辣酱",
                            new BigDecimal("6.00"),
                            80
                    ),
                    new Product(
                            "皮蛋瘦肉粥",
                            "慢熬粥品，350ml",
                            new BigDecimal("8.00"),
                            60
                    )
            ));
        }
    }

    private void createUserIfAbsent(String name, String phone, String rawPassword, Role role) {
        if (!userRepository.existsByPhone(phone)) {
            userRepository.save(
                    new User(name, phone, passwordEncoder.encode(rawPassword), role)
            );
        }
    }
}
