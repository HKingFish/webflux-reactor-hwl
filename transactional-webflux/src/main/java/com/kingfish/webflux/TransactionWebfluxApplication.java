package com.kingfish.webflux;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.kingfish.webflux.infrastructure.mapper")
@SpringBootApplication
public class TransactionWebfluxApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransactionWebfluxApplication.class, args);
	}

}
