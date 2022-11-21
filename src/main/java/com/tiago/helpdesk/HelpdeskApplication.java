package com.tiago.helpdesk;

import com.tiago.helpdesk.api.entity.User;
import com.tiago.helpdesk.api.enums.ProfileEnum;
import com.tiago.helpdesk.api.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class HelpdeskApplication {

	public static void main(String[] args) {
		SpringApplication.run(HelpdeskApplication.class, args);
	}

	@Bean
	CommandLineRunner init(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			initUser(userRepository, passwordEncoder);
		};
	}
	private void initUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		User admin = new User();
		admin.setEmail("teste@teste.com");
		admin.setPassword(passwordEncoder.encode("senha123"));
		admin.setProfile(ProfileEnum.ROLE_ADMIN);

		User find =  userRepository.findByEmail(admin.getEmail());

		if(find == null) {
			userRepository.save(admin);
		}
	}

}
