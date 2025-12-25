package com.julioberina.vaultkeep;

import com.julioberina.vaultkeep.model.ERole;
import com.julioberina.vaultkeep.model.Role;
import com.julioberina.vaultkeep.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@Slf4j
public class VaultKeepApplication {

	public static void main(String[] args) {
		SpringApplication.run(VaultKeepApplication.class, args);
	}

	@Bean
	public CommandLineRunner init(RoleRepository roleRepository) {
		return args -> {
			// Create roles if they don't exist
			if (roleRepository.findByName(ERole.ROLE_USER).isEmpty()) {
				roleRepository.save(new Role(ERole.ROLE_USER));
				log.info("Created default role: ROLE_USER");
			}
			if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()) {
				roleRepository.save(new Role(ERole.ROLE_ADMIN));
				log.info("Created default role: ROLE_ADMIN");
			}

			log.info("Security roles initialization complete.");
		};
	}
}
