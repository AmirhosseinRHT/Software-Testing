package mizdooni.controllers;

import static mizdooni.controllers.ControllerUtils.PARAMS_MISSING;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.stream.Stream;
import mizdooni.model.Address;
import mizdooni.model.User;
import mizdooni.response.Response;
import mizdooni.response.ResponseException;
import mizdooni.service.ServiceUtils;
import mizdooni.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
public class AuthenticationControllerTest {

    @InjectMocks
    private AuthenticationController authController;

    @Mock
    private UserService userService;

//    @BeforeEach
//    public void setUp() {
//    }

    private static Stream<Arguments> provideLoginArgs() {
        return Stream.of(
            Arguments.of(Map.of("username", "person","password1", "123")),
            Arguments.of(Map.of("username2", "person","password", "123")),
            Arguments.of(Map.of("username", "person")),
            Arguments.of(Map.of("password", "123")),
            Arguments.of(Map.of())
        );
    }

    @Test
    @DisplayName("Test fetching current user when logged in")
    public void testGetCurrentUserWhenLoggedIn() {
        Address mockAddress = new Address("Iran","Tehran", "Kargar");
        User mockUser = new User("username", "password", "email@example.com", mockAddress, User.Role.client);
        when(userService.getCurrentUser()).thenReturn(mockUser);
        Response response = authController.user();
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("current user", response.getMessage());
        assertEquals(mockUser, response.getData());
    }

    @Test
    @DisplayName("Test fetching current user when no user is logged in")
    public void testGetCurrentUserWhenNotLoggedIn() {
        when(userService.getCurrentUser()).thenReturn(null);
        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authController.user();
        });
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("no user logged in", exception.getMessage());
    }

    @Test
    @DisplayName("Test correct login credentials")
    public void testLogin() {
        Map<String, String> input = Map.of("username", "person", "password", "123");
        doReturn(true).when(userService).login("person", "123");
        Response response = authController.login(input);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("login successful", response.getMessage());
    }

    @Test
    @DisplayName("Test wrong login credentials")
    public void testWrongLogin() {
        Map<String, String> input = Map.of("username", "person", "password", "123");
        doReturn(false).when(userService).login("person", "123");
        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authController.login(input);
        });
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("invalid username or password", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideLoginArgs")
    @DisplayName("Test missing or incorrect arguments for login")
    public void testMissedArgsForLogin(Map<String, String> input) {
        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authController.login(input);
        });
        assertEquals(PARAMS_MISSING, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    @DisplayName("Test successful logout")
    public void testLogoutSuccessful() {
        when(userService.logout()).thenReturn(true);
        Response response = authController.logout();
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("logout successful", response.getMessage());
    }

    @Test
    @DisplayName("Test unsuccessful logout")
    public void testLogoutUnsuccessful() {
        when(userService.logout()).thenReturn(false);
        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authController.logout();
        });
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("no user logged in", exception.getMessage());
    }


    @Test
    @DisplayName("Test invalid username format")
    public void testInvalidUsernameFormat() {
        // Arrange
        String invalidUsername = "invalid username!";  // Assuming this format is invalid
        when(ServiceUtils.validateUsername(invalidUsername)).thenReturn(false);

        // Act & Assert
        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authController.validateUsername(invalidUsername);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("invalid username format", exception.getMessage());
    }

    @Test
    @DisplayName("Test username already exists")
    public void testUsernameAlreadyExists() {
        String existingUsername = "username";
        when(userService.usernameExists(existingUsername)).thenReturn(true);
        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authController.validateUsername(existingUsername);
        });
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("username already exists", exception.getMessage());
    }

    @Test
    @DisplayName("Test username available")
    public void testUsernameAvailable() {
        String availableUsername = "newUser";
        when(userService.usernameExists(availableUsername)).thenReturn(false);
        Response response = authController.validateUsername(availableUsername);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("username is available", response.getMessage());
    }

    @Test
    @DisplayName("Test invalid email format")
    public void testInvalidEmailFormat() {
        String invalidEmail = "invalid-email";
        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authController.validateEmail(invalidEmail);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("invalid email format", exception.getMessage());
    }

    @Test
    @DisplayName("Test email already registered")
    public void testEmailAlreadyRegistered() {
        String existingEmail = "existing@gmail.com";
        when(userService.emailExists(existingEmail)).thenReturn(true);
        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authController.validateEmail(existingEmail);
        });
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("email already registered", exception.getMessage());
    }

    @Test
    @DisplayName("Test email not registered")
    public void testEmailNotRegistered() {
        String newEmail = "new@example.com";
        when(userService.emailExists(newEmail)).thenReturn(false);
        Response response = authController.validateEmail(newEmail);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("email not registered", response.getMessage());
    }
}
