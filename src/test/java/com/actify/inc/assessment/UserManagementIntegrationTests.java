package com.actify.inc.assessment;

import com.actify.inc.assessment.dto.AuthRequest;
import com.actify.inc.assessment.dto.AuthResponse;
import com.actify.inc.assessment.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserManagementIntegrationTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AuthService authService;

    private MockMvc mockMvc;

    private String adminToken;
    private String managerToken;
    private String userToken;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        adminToken = authService.login(new AuthRequest("admin@example.com", "Admin@123")).getToken();
        managerToken = authService.login(new AuthRequest("manager@example.com", "Manager@123")).getToken();
        userToken = authService.login(new AuthRequest("user@example.com", "User@123")).getToken();

        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @org.junit.jupiter.api.AfterEach
    public void tearDown() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Login with valid credentials should return 200 and JWT token")
    public void testAuthLogin_Success() throws Exception {
        String json = "{\"email\":\"admin@example.com\",\"password\":\"Admin@123\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.email").value("admin@example.com"))
                .andExpect(jsonPath("$.data.roles", hasItem("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Login with invalid credentials should return 401 Unauthorized")
    public void testAuthLogin_BadCredentials() throws Exception {
        String json = "{\"email\":\"admin@example.com\",\"password\":\"WrongPassword\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Admin can perform full CRUD on users and assign roles")
    public void testAdminApis_WithAdminRole_Success() throws Exception {
        // 1. Create User
        String createJson = "{" +
                "\"name\":\"Bob Tester\"," +
                "\"email\":\"bob@example.com\"," +
                "\"password\":\"Bob@12345\"," +
                "\"roles\":[\"ROLE_USER\"]" +
                "}";

        String createResponse = mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("bob@example.com"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn().getResponse().getContentAsString();

        Integer createdUserId = com.jayway.jsonpath.JsonPath.read(createResponse, "$.data.id");

        // 2. Read all users
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(4))));

        // 3. Update User (update Bob Tester's details)
        String updateJson = "{" +
                "\"name\":\"Bob Updated\"," +
                "\"email\":\"bob.new@example.com\"" +
                "}";

        mockMvc.perform(put("/api/admin/users/" + createdUserId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Bob Updated"))
                .andExpect(jsonPath("$.data.email").value("bob.new@example.com"));

        // 4. Assign Roles to Bob
        String assignRolesJson = "{\"roles\":[\"ROLE_USER\",\"ROLE_MANAGER\"]}";

        mockMvc.perform(post("/api/admin/users/" + createdUserId + "/roles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(assignRolesJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roles", hasItems("ROLE_USER", "ROLE_MANAGER")));

        // 5. Delete created user
        mockMvc.perform(delete("/api/admin/users/" + createdUserId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("Manager accessing Admin APIs should return 403 Forbidden")
    public void testAdminApis_WithManagerRole_Forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("Regular User accessing Admin APIs should return 403 Forbidden")
    public void testAdminApis_WithUserRole_Forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("Manager can view all users and their assigned tasks, and assign tasks")
    public void testManagerApis_WithManagerRole_Success() throws Exception {
        // Manager views all users and tasks
        mockMvc.perform(get("/api/manager/users")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray());

        // Manager assigns a task to User (id: 3)
        String taskJson = "{\"userId\":3,\"title\":\"Implement integration tests\"}";

        mockMvc.perform(post("/api/manager/tasks")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Implement integration tests"))
                .andExpect(jsonPath("$.data.userId").value(3));
    }

    @Test
    @DisplayName("Regular User accessing Manager APIs should return 403 Forbidden")
    public void testManagerApis_WithUserRole_Forbidden() throws Exception {
        mockMvc.perform(get("/api/manager/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("User can view own profile and assigned tasks only")
    public void testUserApis_WithAuthenticatedUser_Success() throws Exception {
        // View profile
        mockMvc.perform(get("/api/user/profile")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.name").value("John Doe"));

        // View assigned tasks only
        mockMvc.perform(get("/api/user/tasks")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Creating user with invalid email should return 400 Bad Request")
    public void testValidation_InvalidEmail_BadRequest() throws Exception {
        String invalidEmailJson = "{" +
                "\"name\":\"Test User\"," +
                "\"email\":\"not-an-email\"," +
                "\"password\":\"Secret@123\"," +
                "\"roles\":[\"ROLE_USER\"]" +
                "}";

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidEmailJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    @Test
    @DisplayName("Creating user with weak password should return 400 Bad Request")
    public void testValidation_WeakPassword_BadRequest() throws Exception {
        String weakPasswordJson = "{" +
                "\"name\":\"Test User\"," +
                "\"email\":\"valid@example.com\"," +
                "\"password\":\"weakpass\"," +
                "\"roles\":[\"ROLE_USER\"]" +
                "}";

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(weakPasswordJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    @DisplayName("Creating user with duplicate email should return 409 Conflict")
    public void testValidation_DuplicateEmail_Conflict() throws Exception {
        String duplicateJson = "{" +
                "\"name\":\"Duplicate Admin\"," +
                "\"email\":\"admin@example.com\"," +
                "\"password\":\"Admin@1234\"," +
                "\"roles\":[\"ROLE_USER\"]" +
                "}";

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("Unauthenticated request to protected endpoint should return 401 Unauthorized")
    public void testUnauthenticatedAccess_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }
}
