package com.sdan.middleware;

import com.sdan.middleware.Application.Ports.Messaging.IMessageQueuePort;
import com.sdan.middleware.Infraestructure.In.Messaging.RabbitMqTaskListener;
import com.sdan.middleware.Infraestructure.out.Messaging.RabbitMqTaskPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class MiddlewareApplicationTests {

	@Autowired
	private IMessageQueuePort messageQueuePort;

	@Autowired
	private RabbitMqTaskPublisher rabbitMqTaskPublisher;

	@Autowired
	private RabbitMqTaskListener rabbitMqTaskListener;

	@Test
	void contextLoads() {
		assertNotNull(messageQueuePort);
		assertNotNull(rabbitMqTaskPublisher);
		assertNotNull(rabbitMqTaskListener);
	}

}
