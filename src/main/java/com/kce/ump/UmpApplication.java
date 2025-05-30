package com.kce.ump;

import com.kce.ump.model.user.Role;
import com.kce.ump.model.user.User;
import com.kce.ump.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;


@SpringBootApplication
@RequiredArgsConstructor
public class UmpApplication implements CommandLineRunner {

	private final UserRepository userRepository;

	public static void main(String[] args) {
		SpringApplication.run(UmpApplication.class, args);
	}

	@Override
	public void run(String ...args){
		List<User> adminAccount = userRepository.findAllByRole(Role.ADMIN);

		if(adminAccount.isEmpty()) {
			User user = new User();
			user.setId("a123");
			user.setName("Admin");
			user.setEmail("admin@gmail.com");
			user.setRole(Role.ADMIN);
			user.setPassword(new BCryptPasswordEncoder().encode("admin123"));
			userRepository.save(user);
		}
	}
}
