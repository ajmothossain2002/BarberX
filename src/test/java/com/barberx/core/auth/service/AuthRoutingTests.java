package com.barberx.core.auth.service;

import com.barberx.common.constants.AppConstants;
import com.barberx.core.auth.dto.request.LoginRequest;
import com.barberx.core.auth.dto.request.RegisterRequest;
import com.barberx.core.auth.dto.response.AuthResponse;
import com.barberx.core.shop.entity.Shop;
import com.barberx.core.shop.repository.ShopRepository;
import com.barberx.core.user.entity.CustomerProfile;
import com.barberx.core.user.entity.ShopOwnerProfile;
import com.barberx.core.user.entity.User;
import com.barberx.core.user.repository.CustomerProfileRepository;
import com.barberx.core.user.repository.ShopOwnerProfileRepository;
import com.barberx.core.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthRoutingTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopOwnerProfileRepository shopOwnerProfileRepository;

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private MockMvc mockMvc;

    private String uniqueEmail(String prefix) {
        return prefix + "_" + System.nanoTime() + "@example.com";
    }

    private String uniquePhone() {
        // Generates a 10-digit number starting with 9
        return "9" + String.format("%09d", (long) (Math.random() * 1000000000L));
    }

    @BeforeEach
    void setUp() {
    }

    @Test
    void testCustomerRegistrationAndLoginRedirectsToCustomerHome() {
        String email = uniqueEmail("customer");
        String phone = uniquePhone();

        // 1. Customer registration scenario
        RegisterRequest registerReq = RegisterRequest.builder()
                .fullName("Test Customer")
                .email(email)
                .phoneNumber(phone)
                .password("password123")
                .roleName(AppConstants.ROLE_USER)
                .build();

        AuthResponse registerRes = authService.register(registerReq);

        assertThat(registerRes.getRole()).isEqualTo(AppConstants.ROLE_USER);
        assertThat(registerRes.getProfileCompleted()).isTrue();
        assertThat(registerRes.getNextRoute()).isEqualTo("/customer/home");

        // 2. Customer login scenario
        LoginRequest loginReq = LoginRequest.builder()
                .email(email)
                .password("password123")
                .build();

        AuthResponse loginRes = authService.login(loginReq);

        assertThat(loginRes.getRole()).isEqualTo(AppConstants.ROLE_USER);
        assertThat(loginRes.getProfileCompleted()).isTrue();
        assertThat(loginRes.getNextRoute()).isEqualTo("/customer/home");
    }

    @Test
    void testNewShopOwnerLoginRedirectsToOwnerSetup() {
        String email = uniqueEmail("newowner");
        String phone = uniquePhone();

        // 1. New Owner Registration scenario (wizard not started, no profile complete, no shop)
        RegisterRequest registerReq = RegisterRequest.builder()
                .fullName("Test New Owner")
                .email(email)
                .phoneNumber(phone)
                .password("password123")
                .roleName(AppConstants.ROLE_OWNER)
                .build();

        AuthResponse registerRes = authService.register(registerReq);

        assertThat(registerRes.getRole()).isEqualTo(AppConstants.ROLE_OWNER);
        assertThat(registerRes.getProfileCompleted()).isFalse();
        assertThat(registerRes.getNextRoute()).isEqualTo("/owner/setup");

        // 2. Owner login scenario before completing onboarding
        LoginRequest loginReq = LoginRequest.builder()
                .email(email)
                .password("password123")
                .build();

        AuthResponse loginRes = authService.login(loginReq);

        assertThat(loginRes.getRole()).isEqualTo(AppConstants.ROLE_OWNER);
        assertThat(loginRes.getProfileCompleted()).isFalse();
        assertThat(loginRes.getNextRoute()).isEqualTo("/owner/setup");
    }

    @Test
    void testCompletedShopOwnerLoginRedirectsToOwnerDashboard() {
        String email = uniqueEmail("activeowner");
        String phone = uniquePhone();

        // Register an owner
        RegisterRequest registerReq = RegisterRequest.builder()
                .fullName("Test Active Owner")
                .email(email)
                .phoneNumber(phone)
                .password("password123")
                .roleName(AppConstants.ROLE_OWNER)
                .build();

        AuthResponse registerRes = authService.register(registerReq);
        User ownerUser = userRepository.findById(registerRes.getUser().getId()).orElseThrow();

        // Retrieve profile
        ShopOwnerProfile ownerProfile = shopOwnerProfileRepository.findByUserIdAndDeletedFalse(ownerUser.getId()).orElseThrow();

        // Create a shop linked to this profile
        Shop shop = Shop.builder()
                .owner(ownerUser)
                .ownerProfile(ownerProfile)
                .name("Active Barber Salon")
                .build();
        shopRepository.save(shop);

        // Update profile as completed
        ownerProfile.setProfileCompleted(true);
        shopOwnerProfileRepository.save(ownerProfile);

        // Test login behavior for completed owner
        LoginRequest loginReq = LoginRequest.builder()
                .email(email)
                .password("password123")
                .build();

        AuthResponse loginRes = authService.login(loginReq);

        assertThat(loginRes.getRole()).isEqualTo(AppConstants.ROLE_OWNER);
        assertThat(loginRes.getProfileCompleted()).isTrue();
        assertThat(loginRes.getShopId()).isEqualTo(shop.getId());
        assertThat(loginRes.getNextRoute()).isEqualTo("/owner/dashboard");
    }

    @Test
    void testExistingOwnerSyncsShopProfileOnLogin() {
        String email = uniqueEmail("legacyowner");
        String phone = uniquePhone();

        // Register an owner
        RegisterRequest registerReq = RegisterRequest.builder()
                .fullName("Legacy Owner")
                .email(email)
                .phoneNumber(phone)
                .password("password123")
                .roleName(AppConstants.ROLE_OWNER)
                .build();

        AuthResponse registerRes = authService.register(registerReq);
        User ownerUser = userRepository.findById(registerRes.getUser().getId()).orElseThrow();
        ShopOwnerProfile ownerProfile = shopOwnerProfileRepository.findByUserIdAndDeletedFalse(ownerUser.getId()).orElseThrow();

        // Create a shop NOT linked to the profile (legacy state)
        Shop shop = Shop.builder()
                .owner(ownerUser)
                .ownerProfile(null)
                .name("Legacy Barber Shop")
                .build();
        shopRepository.save(shop);

        // Verify it was saved with null profile link
        assertThat(shop.getOwnerProfile()).isNull();

        // Test login - should sync the shop to the profile and redirect to dashboard
        LoginRequest loginReq = LoginRequest.builder()
                .email(email)
                .password("password123")
                .build();

        AuthResponse loginRes = authService.login(loginReq);

        assertThat(loginRes.getRole()).isEqualTo(AppConstants.ROLE_OWNER);
        assertThat(loginRes.getProfileCompleted()).isTrue();
        assertThat(loginRes.getShopId()).isEqualTo(shop.getId());
        assertThat(loginRes.getNextRoute()).isEqualTo("/owner/dashboard");

        // Verify the database record has been synced
        Shop updatedShop = shopRepository.findById(shop.getId()).orElseThrow();
        assertThat(updatedShop.getOwnerProfile()).isNotNull();
        assertThat(updatedShop.getOwnerProfile().getId()).isEqualTo(ownerProfile.getId());
    }

    @Test
    void testJwtTokenAuthenticationFilterExecution() throws Exception {
        String email = uniqueEmail("filtertest");
        String phone = uniquePhone();

        RegisterRequest registerReq = RegisterRequest.builder()
                .fullName("Filter Test User")
                .email(email)
                .phoneNumber(phone)
                .password("password123")
                .roleName(AppConstants.ROLE_USER)
                .build();

        AuthResponse registerRes = authService.register(registerReq);
        String token = registerRes.getAccessToken();

        // Perform request to public marketplace shops endpoint with Bearer token
        mockMvc.perform(get("/api/v1/marketplace/shops")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
