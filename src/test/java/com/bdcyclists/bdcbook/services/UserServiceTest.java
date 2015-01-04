package com.bdcyclists.bdcbook.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.assertj.core.api.Assertions;

import com.bdcyclists.bdcbook.domain.User;
import com.bdcyclists.bdcbook.dto.RegistrationForm;
import com.bdcyclists.bdcbook.dto.RegistrationFormBuilder;
import com.bdcyclists.bdcbook.repository.UserRepository;
import com.bdcyclists.bdcbook.service.DuplicateEmailException;
import com.bdcyclists.bdcbook.service.UserService;
import com.bdcyclists.bdcbook.service.UserServiceImpl;
import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;

import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static com.bdcyclists.bdcbook.model.UserAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {
	private static final String EMAIL = "contact@bazlur.com";
	private static final String ENCODED_PASSWORD = "encodedPassword";
	private static final String FIRST_NAME = "Bazlur";
	private static final String LAST_NAME = "Rahman";
	private static final String PASSWORD = "password";

	private UserService userService;

	@Mock
	private PasswordEncoder passwordEncoderMock;

	@Mock
	private UserRepository repositoryMock;

	@Before
	public void setUp() {
		userService = new UserServiceImpl(repositoryMock, passwordEncoderMock);
	}

	@Test
	public void registerNewUserAccount_shouldCreateNewUserAccount()
			throws DuplicateEmailException {
		RegistrationForm registration = new RegistrationFormBuilder()
				.email(EMAIL).firstName(FIRST_NAME).lastName(LAST_NAME).build();

		when(repositoryMock.findByEmail(EMAIL)).thenReturn(null);
		when(repositoryMock.save(isA(User.class))).then(new Answer<User>() {

			@Override
			public User answer(InvocationOnMock invocation) throws Throwable {
				Object[] arguments = invocation.getArguments();
				return (User) arguments[0];
			}
		});

		User userRegistered = userService.registerNewUserAccount(registration);

		assertThat(userRegistered).hasEmail(EMAIL).hasFirstName(FIRST_NAME)
				.hasLastName(LAST_NAME).hasRole();

		verify(repositoryMock, times(1)).findByEmail(EMAIL);
		verify(repositoryMock, times(1)).save(userRegistered);
		verifyNoMoreInteractions(repositoryMock);
	}

	@Test
	public void registerNewUserAccount_ViaSocialSignInAndDuplicateEmailIsFound_ShouldThrowException()
			throws DuplicateEmailException {
		RegistrationForm registration = new RegistrationFormBuilder()
				.email(EMAIL).firstName(FIRST_NAME).lastName(LAST_NAME).build();
		when(repositoryMock.findByEmail(EMAIL)).thenReturn(new User());

		catchException(userService).registerNewUserAccount(registration);

		Assertions
				.assertThat(caughtException())
				.isExactlyInstanceOf(DuplicateEmailException.class)
				.hasMessage(
						"The email address: " + EMAIL + " is already in use.")
				.hasNoCause();

		verify(repositoryMock, times(1)).findByEmail(EMAIL);
		verifyNoMoreInteractions(repositoryMock);
		verifyZeroInteractions(passwordEncoderMock);
	}

}
