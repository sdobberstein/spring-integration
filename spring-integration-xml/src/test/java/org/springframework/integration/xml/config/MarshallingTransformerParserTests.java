/*
 * Copyright 2002-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.xml.config;

import java.util.List;

import javax.xml.transform.dom.DOMResult;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.support.SmartLifecycleRoleController;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.integration.xml.config.StubResultFactory.StubStringResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.util.MultiValueMap;
import org.springframework.xml.transform.StringResult;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Jonas Partner
 * @author Mark Fisher
 * @author Gary Russell
 */
public class MarshallingTransformerParserTests {

	private ApplicationContext appContext;

	private PollableChannel output;


	@Before
	public void setUp() {
		this.appContext = new ClassPathXmlApplicationContext("MarshallingTransformerParserTests-context.xml", getClass());
		this.output = (PollableChannel) appContext.getBean("output");
	}


	@Test
	public void testParse() throws Exception {
		EventDrivenConsumer consumer = (EventDrivenConsumer) appContext.getBean("parseOnly");
		assertThat(TestUtils.getPropertyValue(consumer, "handler.order")).isEqualTo(2);
		assertThat(TestUtils.getPropertyValue(consumer, "handler.messagingTemplate.sendTimeout")).isEqualTo(123L);
		assertThat(TestUtils.getPropertyValue(consumer, "phase")).isEqualTo(-1);
		assertThat(TestUtils.getPropertyValue(consumer, "autoStartup", Boolean.class)).isFalse();
		SmartLifecycleRoleController roleController = appContext.getBean(SmartLifecycleRoleController.class);
		@SuppressWarnings("unchecked")
		List<SmartLifecycle> list = (List<SmartLifecycle>) TestUtils.getPropertyValue(roleController, "lifecycles",
				MultiValueMap.class).get("foo");
		assertThat(list).containsExactly((SmartLifecycle) consumer);
	}

	@Test
	public void testDefault() throws Exception {
		MessageChannel input = (MessageChannel) appContext.getBean("marshallingTransformerNoResultFactory");
		GenericMessage<Object> message = new GenericMessage<Object>("hello");
		input.send(message);
		Message<?> result = output.receive(0);
		assertThat(result.getPayload() instanceof DOMResult).as("Wrong payload type").isTrue();
		Document doc = (Document) ((DOMResult) result.getPayload()).getNode();
		assertThat(doc.getDocumentElement().getTextContent()).as("Wrong payload").isEqualTo("hello");
	}

	@Test
	public void testDefaultWithResultTransformer() throws Exception {
		MessageChannel input = (MessageChannel) appContext.getBean("marshallingTransformerWithResultTransformer");
		GenericMessage<Object> message = new GenericMessage<Object>("hello");
		input.send(message);
		Message<?> result = output.receive(0);
		assertThat(result.getPayload() instanceof String).as("Wrong payload type").isTrue();
		String resultPayload = (String) result.getPayload();
		assertThat(resultPayload).as("Wrong payload").isEqualTo("testReturn");
	}

	@Test
	public void testDOMResult() throws Exception {
		MessageChannel input = (MessageChannel) appContext.getBean("marshallingTransformerDOMResultFactory");
		GenericMessage<Object> message = new GenericMessage<Object>("hello");
		input.send(message);
		Message<?> result = output.receive(0);
		assertThat(result.getPayload() instanceof DOMResult).as("Wrong payload type ").isTrue();
		Document doc = (Document) ((DOMResult) result.getPayload()).getNode();
		assertThat(doc.getDocumentElement().getTextContent()).as("Wrong payload").isEqualTo("hello");
	}

	@Test
	public void testStringResult() throws Exception {
		MessageChannel input = (MessageChannel) appContext.getBean("marshallingTransformerStringResultFactory");
		GenericMessage<Object> message = new GenericMessage<Object>("hello");
		input.send(message);
		Message<?> result = output.receive(0);
		assertThat(result.getPayload() instanceof StringResult).as("Wrong payload type").isTrue();
	}

	@Test
	public void testCustomResultFactory() throws Exception {
		MessageChannel input = (MessageChannel) appContext.getBean("marshallingTransformerCustomResultFactory");
		GenericMessage<Object> message = new GenericMessage<Object>("hello");
		input.send(message);
		Message<?> result = output.receive(0);
		assertThat(result.getPayload() instanceof StubStringResult).as("Wrong payload type").isTrue();
	}

	@Test
	public void testFullMessage() throws Exception {
		MessageChannel input = (MessageChannel) appContext.getBean("marshallingTransformerWithFullMessage");
		GenericMessage<Object> message = new GenericMessage<Object>("hello");
		input.send(message);
		Message<?> result = output.receive(0);
		assertThat(result.getPayload() instanceof DOMResult).as("Wrong payload type").isTrue();
		Document doc = (Document) ((DOMResult) result.getPayload()).getNode();
		String actual = doc.getDocumentElement().getTextContent();
		assertThat(actual).contains("[payload");
		assertThat(actual).contains("=hello,");
		assertThat(actual).contains(", headers=");
	}

}
